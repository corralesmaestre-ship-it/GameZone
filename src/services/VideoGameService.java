package services;

import entities.VideoGame;
import exceptions.ValidationException;
import exceptions.VideoGameAlreadyExistsException;
import exceptions.VideoGameNotFoundException;
import repository.VideoGameRepository;

import java.util.ArrayList;
import java.util.List;

public class VideoGameService {
    private final VideoGameRepository repository;

    public VideoGameService(VideoGameRepository repository) {
        this.repository = repository;
    }

    public void addVideoGame(VideoGame game) throws ValidationException, VideoGameAlreadyExistsException {
        validate(game);
        repository.add(game);
    }

    public void updateVideoGame(String title, VideoGame newGame) throws ValidationException, VideoGameNotFoundException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("El título de búsqueda para actualizar no puede estar vacío.");
        }
        validate(newGame);
        repository.update(title, newGame);
    }

    public void deleteVideoGame(String title) throws VideoGameNotFoundException {
        if (title == null || title.trim().isEmpty()) {
            throw new VideoGameNotFoundException("El título para eliminar no puede estar vacío.");
        }
        repository.delete(title);
    }

    public List<VideoGame> getAllVideoGames() {
        return repository.getAll();
    }

    public VideoGame getByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        return repository.getByTitle(title);
    }

    public List<VideoGame> searchByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.trim().isEmpty()) {
            return null;
        }
        String query = titleQuery.trim().toLowerCase();
        List<VideoGame> results = new ArrayList<>();
        for (VideoGame g : repository.getAll()) {
            if (g.getTitle().toLowerCase().contains(query)) {
                results.add(g);
            }
        }
        return results.isEmpty() ? null : results;
    }

    public List<VideoGame> searchByPlatform(String platformQuery) {
        if (platformQuery == null || platformQuery.trim().isEmpty()) {
            return null;
        }
        String query = platformQuery.trim().toLowerCase();
        List<VideoGame> results = new ArrayList<>();
        for (VideoGame g : repository.getAll()) {
            if (g.getPlatform().toLowerCase().contains(query)) {
                results.add(g);
            }
        }
        return results.isEmpty() ? null : results;
    }

    private void validate(VideoGame game) throws ValidationException {
        if (game == null) {
            throw new ValidationException("El videojuego no puede ser nulo.");
        }
        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            throw new ValidationException("El título del videojuego no puede estar vacío.");
        }
        if (game.getPrice() <= 0) {
            throw new ValidationException("El precio debe ser mayor a 0.");
        }
        if (game.getStock() < 0) {
            throw new ValidationException("El stock no puede ser negativo.");
        }
    }
}
