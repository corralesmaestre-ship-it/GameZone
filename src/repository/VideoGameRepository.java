package repository;

import com.google.gson.*;
import entities.DigitalVideoGame;
import entities.PhysicalVideoGame;
import entities.VideoGame;
import exceptions.VideoGameAlreadyExistsException;
import exceptions.VideoGameNotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VideoGameRepository {
    private final Gson gson;
    private final String filePath = "games.json";
    private List<VideoGame> games;

    public VideoGameRepository() {
        // Simple default Gson instance
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.games = loadAll();
    }

    private List<VideoGame> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        List<VideoGame> loadedGames = new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            // Manual parsing of the JSON array
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    // If it has 'sizeGB' or the 'gameType' is digital, it's digital
                    boolean isDigital = obj.has("sizeGB") || obj.has("downloadPlatform");
                    if (obj.has("gameType")) {
                        isDigital = "digital".equalsIgnoreCase(obj.get("gameType").getAsString());
                    }

                    if (isDigital) {
                        loadedGames.add(gson.fromJson(obj, DigitalVideoGame.class));
                    } else {
                        loadedGames.add(gson.fromJson(obj, PhysicalVideoGame.class));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedGames;
    }

    private synchronized void saveAll() {
        try (Writer writer = new FileWriter(filePath)) {
            JsonArray array = new JsonArray();
            for (VideoGame g : games) {
                JsonObject obj;
                if (g instanceof DigitalVideoGame) {
                    obj = gson.toJsonTree(g, DigitalVideoGame.class).getAsJsonObject();
                    obj.addProperty("gameType", "digital");
                } else {
                    obj = gson.toJsonTree(g, PhysicalVideoGame.class).getAsJsonObject();
                    obj.addProperty("gameType", "physical");
                }
                array.add(obj);
            }
            gson.toJson(array, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<VideoGame> getAll() {
        this.games = loadAll();
        return new ArrayList<>(games);
    }

    public synchronized VideoGame getByTitle(String title) {
        if (title == null) return null;
        this.games = loadAll();
        for (VideoGame g : games) {
            if (g.getTitle().equalsIgnoreCase(title.trim())) {
                return g;
            }
        }
        return null;
    }

    public synchronized void add(VideoGame game) throws VideoGameAlreadyExistsException {
        if (getByTitle(game.getTitle()) != null) {
            throw new VideoGameAlreadyExistsException("El videojuego ya existe en el catálogo.");
        }
        games.add(game);
        saveAll();
    }

    public synchronized void update(String title, VideoGame newGame) throws VideoGameNotFoundException {
        this.games = loadAll();
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getTitle().equalsIgnoreCase(title.trim())) {
                games.set(i, newGame);
                saveAll();
                return;
            }
        }
        throw new VideoGameNotFoundException("El videojuego no se encontró en el catálogo.");
    }

    public synchronized void delete(String title) throws VideoGameNotFoundException {
        this.games = loadAll();
        games.removeIf(g -> g.getTitle().equalsIgnoreCase(title.trim()));
        saveAll();
    }
}
