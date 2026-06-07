package repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import entities.DigitalVideoGame;
import entities.PhysicalVideoGame;
import entities.Sale;
import entities.VideoGame;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleRepository {
    private final Gson gson;
    private final String filePath = "sales.json";
    private List<Sale> sales;

    public SaleRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.sales = loadAll();
    }

    private List<Sale> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        List<Sale> loadedSales = new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                for (JsonElement element : array) {
                    JsonObject saleObj = element.getAsJsonObject();
                    
                    // Extract fields
                    String id = saleObj.get("id").getAsString();
                    int quantity = saleObj.get("quantity").getAsInt();
                    double unitPrice = saleObj.get("unitPrice").getAsDouble();
                    double total = saleObj.get("total").getAsDouble();
                    String dateStr = saleObj.get("saleDate").getAsString();
                    LocalDateTime date = LocalDateTime.parse(dateStr);
                    
                    // Decode inner VideoGame object
                    JsonObject gameObj = saleObj.getAsJsonObject("videoGame");
                    VideoGame game = null;
                    if (gameObj != null) {
                        boolean isDigital = gameObj.has("sizeGB") || gameObj.has("downloadPlatform");
                        if (gameObj.has("gameType")) {
                            isDigital = "digital".equalsIgnoreCase(gameObj.get("gameType").getAsString());
                        }

                        if (isDigital) {
                            game = gson.fromJson(gameObj, DigitalVideoGame.class);
                        } else {
                            game = gson.fromJson(gameObj, PhysicalVideoGame.class);
                        }
                    }

                    Sale sale = new Sale(id, game, quantity, unitPrice);
                    sale.setTotal(total);
                    sale.setSaleDate(date);
                    loadedSales.add(sale);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedSales;
    }

    private synchronized void saveAll() {
        try (Writer writer = new FileWriter(filePath)) {
            JsonArray array = new JsonArray();
            for (Sale s : sales) {
                JsonObject saleObj = new JsonObject();
                saleObj.addProperty("id", s.getId());
                saleObj.addProperty("quantity", s.getQuantity());
                saleObj.addProperty("unitPrice", s.getUnitPrice());
                saleObj.addProperty("total", s.getTotal());
                saleObj.addProperty("saleDate", s.getSaleDate().toString());
                
                JsonObject gameObj = null;
                if (s.getVideoGame() != null) {
                    VideoGame g = s.getVideoGame();
                    if (g instanceof DigitalVideoGame) {
                        gameObj = gson.toJsonTree(g, DigitalVideoGame.class).getAsJsonObject();
                        gameObj.addProperty("gameType", "digital");
                    } else {
                        gameObj = gson.toJsonTree(g, PhysicalVideoGame.class).getAsJsonObject();
                        gameObj.addProperty("gameType", "physical");
                    }
                }
                saleObj.add("videoGame", gameObj);
                
                array.add(saleObj);
            }
            gson.toJson(array, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Sale> getAll() {
        this.sales = loadAll();
        return new ArrayList<>(sales);
    }

    public synchronized void add(Sale sale) {
        this.sales = loadAll();
        sales.add(sale);
        saveAll();
    }
}
