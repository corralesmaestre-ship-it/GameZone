package entities;

public abstract class VideoGame {
    protected String title;
    protected double price;
    protected String platform;
    protected int stock;
    protected String genre;


    public VideoGame(String title, double price, String platform, int stock, String genre) {
        this.title = title;
        this.price = price;
        this.platform = platform;
        this.stock = stock;
        this.genre = genre;
    }


    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getPlatform() {
        return platform;
    }

    public int getStock() {
        return stock;
    }

    public String getGenre() {
        return genre;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }


    public abstract double calculateFinalPrice();

    @Override
    public String toString() {
        return "VideoGame{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", platform='" + platform + '\'' +
                ", stock=" + stock +
                ", genre='" + genre + '\'' +
                '}';
    }
}
