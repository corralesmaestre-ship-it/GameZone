package entities;

import interfaces.Sellable;
import interfaces.Displayable;

public class DigitalVideoGame extends VideoGame implements Sellable, Displayable {
    private double sizeGB;
    private String downloadPlatform;

    public DigitalVideoGame(String title, double price, String platform, int stock, String genre, double sizeGB, String downloadPlatform) {
        super(title, price, platform, stock, genre);
        this.sizeGB = sizeGB;
        this.downloadPlatform = downloadPlatform;
    }


    public double getSizeGB() {
        return sizeGB;
    }

    public void setSizeGB(double sizeGB) {
        this.sizeGB = sizeGB;
    }

    public String getDownloadPlatform() {
        return downloadPlatform;
    }

    public void setDownloadPlatform(String downloadPlatform) {
        this.downloadPlatform = downloadPlatform;
    }

    @Override
    public double calculateFinalPrice() {
        if (sizeGB > 50) {
            return price + 5000;
        }
        return price;
    }

    @Override
    public double sell(int qty) {
        if (this.stock < qty) {
            throw new RuntimeException("Stock insuficiente para realizar la venta del juego digital: " + title);
        }
        this.stock -= qty;
        return calculateFinalPrice() * qty;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Digital - %s [%s] | Precio Base: $%.2f | Final: $%.2f | Stock: %d | Tamaño: %.1f GB | Plataforma: %s",
                title, platform, price, calculateFinalPrice(), stock, sizeGB, downloadPlatform);
    }

    @Override
    public Object[] toTableRow() {
        return new Object[] {
                title,
                "Digital",
                price,
                calculateFinalPrice(),
                platform,
                stock,
                genre,
                String.format("Tamaño: %.1f GB, Plataforma: %s", sizeGB, downloadPlatform)
        };
    }

    @Override
    public String toString() {
        return "DigitalVideoGame{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", platform='" + platform + '\'' +
                ", stock=" + stock +
                ", genre='" + genre + '\'' +
                ", sizeGB=" + sizeGB +
                ", downloadPlatform='" + downloadPlatform + '\'' +
                '}';
    }
}
