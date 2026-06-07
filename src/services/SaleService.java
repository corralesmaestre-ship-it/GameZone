package services;

import entities.Sale;
import entities.VideoGame;
import exceptions.InsufficientStockException;
import exceptions.ValidationException;
import exceptions.VideoGameNotFoundException;
import interfaces.Sellable;
import repository.SaleRepository;
import repository.VideoGameRepository;

import java.util.List;

public class SaleService {
    private final VideoGameRepository videoGameRepository;
    private final SaleRepository saleRepository;

    public SaleService(VideoGameRepository videoGameRepository, SaleRepository saleRepository) {
        this.videoGameRepository = videoGameRepository;
        this.saleRepository = saleRepository;
    }

    public synchronized Sale venderVideojuego(String title, int quantity)
            throws VideoGameNotFoundException, InsufficientStockException, ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("El título del videojuego no puede estar vacío.");
        }
        if (quantity <= 0) {
            throw new ValidationException("La cantidad a vender debe ser mayor a 0.");
        }

        // Search game (case-insensitive)
        VideoGame game = videoGameRepository.getByTitle(title);
        if (game == null) {
            throw new VideoGameNotFoundException("El videojuego '" + title + "' no existe en el catálogo.");
        }

        // Verify stock
        if (game.getStock() < quantity) {
            throw new InsufficientStockException("Stock insuficiente para '" + game.getTitle() + "'. Stock disponible: " + game.getStock());
        }

        double unitPrice = game.calculateFinalPrice();

        // Reduce stock using the sell method of the subclass
        if (game instanceof Sellable) {
            ((Sellable) game).sell(quantity);
        } else {
            game.setStock(game.getStock() - quantity);
        }

        // Update the game in the repository
        try {
            videoGameRepository.update(game.getTitle(), game);
        } catch (VideoGameNotFoundException e) {
            throw new VideoGameNotFoundException("Error al actualizar el stock del videojuego: " + e.getMessage());
        }

        // Generate sale ID
        String saleId = "V-" + String.format("%03d", saleRepository.getAll().size() + 1);

        // Create sale
        Sale sale = new Sale(saleId, game, quantity, unitPrice);
        saleRepository.add(sale);

        return sale;
    }

    public List<Sale> getAllSales() {
        return saleRepository.getAll();
    }
}
