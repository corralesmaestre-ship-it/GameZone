package entities;

import interfaces.Sellable;
import interfaces.Displayable;

public class PhysicalVideoGame extends VideoGame implements Sellable, Displayable {
    private String condition;
    private String distributor;


    public PhysicalVideoGame(String title, double price, String platform, int stock, String genre, String condition, String distributor) {
        super(title, price, platform, stock, genre);
        this.condition = condition;
        this.distributor = distributor;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    @Override
    public double calculateFinalPrice() {
        if (condition != null && condition.equalsIgnoreCase("usado")) {
            return price * 0.75;
        }
        return price;
    }

    @Override
    public double sell(int qty) {
        if (this.stock < qty) {
            throw new RuntimeException("Stock insuficiente para realizar la venta del juego físico: " + title);
        }
        this.stock -= qty;
        return calculateFinalPrice() * qty;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Físico - %s [%s] | Precio Base: $%.2f | Final: $%.2f | Stock: %d | Estado: %s | Distribuidor: %s",
                title, platform, price, calculateFinalPrice(), stock, condition, distributor);
    }

    @Override
    public Object[] toTableRow() {
        return new Object[] {
                title,
                "Físico",
                price,
                calculateFinalPrice(),
                platform,
                stock,
                genre,
                String.format("Estado: %s, Distribuidor: %s", condition, distributor)
        };
    }

    @Override
    public String toString() {
        return "PhysicalVideoGame{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", platform='" + platform + '\'' +
                ", stock=" + stock +
                ", genre='" + genre + '\'' +
                ", condition='" + condition + '\'' +
                ", distributor='" + distributor + '\'' +
                '}';
    }
}
