import entities.DigitalVideoGame;
import entities.PhysicalVideoGame;
import entities.VideoGame;
import entities.Sale;
import exceptions.InsufficientStockException;
import exceptions.ValidationException;
import exceptions.VideoGameAlreadyExistsException;
import exceptions.VideoGameNotFoundException;
import repository.SaleRepository;
import repository.VideoGameRepository;
import services.SaleService;
import services.VideoGameService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private VideoGameService gameService;
    private SaleService saleService;

    private ObservableList<VideoGame> gameList;
    private ObservableList<Sale> saleList;

    // GUI Tabs
    private TabPane tabPane;
    private Tab tabDashboard;
    private Tab tabCatalog;
    private Tab tabSell;
    private Tab tabHistory;

    // Controls
    private TableView<VideoGame> gameTable;
    private TableView<Sale> saleTable;

    private ComboBox<String> comboSelectGame;
    private Label lblSalePrice;
    private Label lblSaleFinalPrice;
    private Label lblSalePlatform;
    private Label lblSaleStock;
    private TextField txtSaleQty;
    private Label lblSaleTotal;

    // Statistics Labels (Dashboard)
    private Label lblTotalStock;
    private Label lblTotalTitles;
    private Label lblTotalSales;
    private Label lblTotalRevenue;

    // Sales History Total
    private Label lblHistoryTotalRevenue;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize layers
        VideoGameRepository gameRepo = new VideoGameRepository();
        SaleRepository saleRepo = new SaleRepository();
        gameService = new VideoGameService(gameRepo);
        saleService = new SaleService(gameRepo, saleRepo);

        gameList = FXCollections.observableArrayList(gameService.getAllVideoGames());
        saleList = FXCollections.observableArrayList(saleService.getAllSales());

        primaryStage.setTitle("Sistema de Gestión GameZone - Universidad Popular del Cesar");

        // Create TabPane (typical student design)
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Build screens as tabs
        createDashboardTab();
        createCatalogTab();
        createSellTab();
        createHistoryTab();

        tabPane.getTabs().addAll(tabDashboard, tabCatalog, tabSell, tabHistory);

        // Listen for tab switching to reload data
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabDashboard) {
                updateDashboardStats();
            } else if (newTab == tabCatalog) {
                refreshGameTable();
            } else if (newTab == tabSell) {
                refreshSellDropdown();
            } else if (newTab == tabHistory) {
                refreshSalesHistoryTable();
            }
        });

        // Main Layout HBox/VBox
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Simple header bar
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(5, 10, 5, 10));
        header.setStyle("-fx-background-color: #E9ECEF; -fx-background-radius: 4;");
        
        Label lblHeaderTitle = new Label("GAMEZONE STORE - SISTEMA DE CONTROL DE INVENTARIO Y VENTAS");
        lblHeaderTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnExit = new Button("Cerrar Programa");
        btnExit.getStyleClass().addAll("btn", "btn-secondary");
        btnExit.setStyle("-fx-padding: 4 8 4 8;");
        btnExit.setOnAction(e -> Platform.exit());
        
        header.getChildren().addAll(lblHeaderTitle, spacer, btnExit);

        root.getChildren().addAll(header, tabPane);

        Scene scene = new Scene(root, 1050, 680);

        // Load stylesheet
        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            try {
                java.io.File cssFile = new java.io.File("src/style.css");
                if (cssFile.exists()) {
                    scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar style.css: " + e.getMessage());
            }
        }

        primaryStage.setScene(scene);
        updateDashboardStats(); // Initial load
        primaryStage.show();
    }

    // ==========================================
    // 1. DASHBOARD TAB
    // ==========================================
    private void createDashboardTab() {
        tabDashboard = new Tab("Dashboard");

        VBox content = new VBox(20);
        content.getStyleClass().add("content-pane");

        Label title = new Label("Resumen General de la Tienda");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Estadísticas del catálogo de videojuegos y registros de ventas.");
        subtitle.getStyleClass().add("subtitle-label");

        // Stat Cards Layout
        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        // Card 1
        VBox card1 = new VBox(5);
        card1.getStyleClass().add("stat-card");
        Label c1Title = new Label("Copias en Inventario");
        c1Title.getStyleClass().add("stat-title");
        lblTotalStock = new Label("0");
        lblTotalStock.getStyleClass().add("stat-value");
        card1.getChildren().addAll(c1Title, lblTotalStock);

        // Card 2
        VBox card2 = new VBox(5);
        card2.getStyleClass().add("stat-card");
        Label c2Title = new Label("Títulos Distintos");
        c2Title.getStyleClass().add("stat-title");
        lblTotalTitles = new Label("0");
        lblTotalTitles.getStyleClass().add("stat-value");
        card2.getChildren().addAll(c2Title, lblTotalTitles);

        // Card 3
        VBox card3 = new VBox(5);
        card3.getStyleClass().add("stat-card");
        Label c3Title = new Label("Ventas Registradas");
        c3Title.getStyleClass().add("stat-title");
        lblTotalSales = new Label("0");
        lblTotalSales.getStyleClass().add("stat-value");
        card3.getChildren().addAll(c3Title, lblTotalSales);

        // Card 4
        VBox card4 = new VBox(5);
        card4.getStyleClass().add("stat-card");
        Label c4Title = new Label("Ingresos Acumulados");
        c4Title.getStyleClass().add("stat-title");
        lblTotalRevenue = new Label("$0.00");
        lblTotalRevenue.getStyleClass().add("stat-value");
        card4.getChildren().addAll(c4Title, lblTotalRevenue);

        cardsBox.getChildren().addAll(card1, card2, card3, card4);

        // Welcome Box
        VBox infoPanel = new VBox(10);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setStyle("-fx-background-color: #E9ECEF; -fx-background-radius: 4; -fx-border-color: #CED4DA; -fx-border-width: 1;");
        
        Label infoTitle = new Label("Proyecto Final de Programación II - U.P.C.");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #343A40;");
        Label infoText = new Label("Este sistema permite gestionar el catálogo (Agregar, Listar, Buscar, Modificar y Eliminar) " +
                "de videojuegos digitales y físicos, reduciendo el stock automáticamente ante cada venta simulada.\n" +
                "Toda la persistencia se realiza localmente en los archivos JSON de la carpeta raíz del proyecto.");
        infoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057; -fx-line-spacing: 4;");
        
        infoPanel.getChildren().addAll(infoTitle, infoText);

        content.getChildren().addAll(title, subtitle, cardsBox, infoPanel);
        tabDashboard.setContent(content);
    }

    private void updateDashboardStats() {
        List<VideoGame> games = gameService.getAllVideoGames();
        List<Sale> sales = saleService.getAllSales();

        int totalStock = 0;
        for (VideoGame g : games) {
            totalStock += g.getStock();
        }

        double totalRevenue = 0.0;
        for (Sale s : sales) {
            totalRevenue += s.getTotal();
        }

        lblTotalStock.setText(String.valueOf(totalStock));
        lblTotalTitles.setText(String.valueOf(games.size()));
        lblTotalSales.setText(String.valueOf(sales.size()));
        lblTotalRevenue.setText(String.format("$%.2f", totalRevenue));
    }

    // ==========================================
    // 2. CATALOG TAB (CRUD)
    // ==========================================
    private void createCatalogTab() {
        tabCatalog = new Tab("Catálogo");

        VBox content = new VBox(15);
        content.getStyleClass().add("content-pane");

        Label title = new Label("Gestión de Inventario de Videojuegos");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Añada, modifique, elimine y busque videojuegos en el catálogo.");
        subtitle.getStyleClass().add("subtitle-label");

        // Filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearchTitle = new TextField();
        txtSearchTitle.setPromptText("Buscar por título...");
        txtSearchTitle.setPrefWidth(200);

        TextField txtSearchPlatform = new TextField();
        txtSearchPlatform.setPromptText("Buscar por plataforma...");
        txtSearchPlatform.setPrefWidth(200);

        Button btnSearch = new Button("Filtrar");
        btnSearch.getStyleClass().addAll("btn", "btn-primary");
        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");

        txtSearchTitle.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(newVal, txtSearchPlatform.getText()));
        txtSearchPlatform.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(txtSearchTitle.getText(), newVal));

        btnSearch.setOnAction(e -> applyFilters(txtSearchTitle.getText(), txtSearchPlatform.getText()));
        btnClear.setOnAction(e -> {
            txtSearchTitle.clear();
            txtSearchPlatform.clear();
            applyFilters("", "");
        });

        filterBar.getChildren().addAll(new Label("Buscar:"), txtSearchTitle, txtSearchPlatform, btnSearch, btnClear);

        // Table
        gameTable = new TableView<>();
        gameTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<VideoGame, String> colTitle = new TableColumn<>("Título");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(180);

        TableColumn<VideoGame, String> colType = new TableColumn<>("Tipo");
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue() instanceof DigitalVideoGame ? "Digital" : "Físico"
        ));
        colType.setPrefWidth(80);

        TableColumn<VideoGame, String> colPlatform = new TableColumn<>("Plataforma");
        colPlatform.setCellValueFactory(new PropertyValueFactory<>("platform"));
        colPlatform.setPrefWidth(100);

        TableColumn<VideoGame, String> colGenre = new TableColumn<>("Género");
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colGenre.setPrefWidth(100);

        TableColumn<VideoGame, Double> colBasePrice = new TableColumn<>("Precio Base");
        colBasePrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colBasePrice.setPrefWidth(90);

        TableColumn<VideoGame, Double> colFinalPrice = new TableColumn<>("Precio Final");
        colFinalPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(
                cellData.getValue().calculateFinalPrice()
        ).asObject());
        colFinalPrice.setPrefWidth(90);

        TableColumn<VideoGame, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(70);

        TableColumn<VideoGame, String> colDetails = new TableColumn<>("Detalles");
        colDetails.setCellValueFactory(cellData -> {
            VideoGame g = cellData.getValue();
            if (g instanceof DigitalVideoGame) {
                DigitalVideoGame d = (DigitalVideoGame) g;
                return new SimpleStringProperty(String.format("Tamaño: %.1f GB | %s", d.getSizeGB(), d.getDownloadPlatform()));
            } else if (g instanceof PhysicalVideoGame) {
                PhysicalVideoGame p = (PhysicalVideoGame) g;
                return new SimpleStringProperty(String.format("Estado: %s | Distribuidor: %s", p.getCondition(), p.getDistributor()));
            }
            return new SimpleStringProperty("-");
        });
        colDetails.setPrefWidth(220);

        gameTable.getColumns().addAll(colTitle, colType, colPlatform, colGenre, colBasePrice, colFinalPrice, colStock, colDetails);
        gameTable.setItems(gameList);

        // Buttons
        HBox buttonBar = new HBox(15);
        
        Button btnAdd = new Button("+ Registrar Juego");
        btnAdd.getStyleClass().addAll("btn", "btn-success");
        
        Button btnEdit = new Button("✎ Modificar");
        btnEdit.getStyleClass().addAll("btn", "btn-warning");
        
        Button btnDelete = new Button("🗑 Eliminar");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");

        btnAdd.setOnAction(e -> openDialog(null));
        btnEdit.setOnAction(e -> {
            VideoGame sel = gameTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(Alert.AlertType.WARNING, "Alerta", "Seleccione un juego", "Debe seleccionar un juego para modificar.");
                return;
            }
            openDialog(sel);
        });
        btnDelete.setOnAction(e -> {
            VideoGame sel = gameTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showSimpleAlert(Alert.AlertType.WARNING, "Alerta", "Seleccione un juego", "Debe seleccionar un juego para eliminar.");
                return;
            }
            deleteGame(sel);
        });

        buttonBar.getChildren().addAll(btnAdd, btnEdit, btnDelete);

        content.getChildren().addAll(title, subtitle, filterBar, gameTable, buttonBar);
        tabCatalog.setContent(content);
    }

    private void applyFilters(String titleQuery, String platformQuery) {
        List<VideoGame> filtered = new ArrayList<>();
        for (VideoGame g : gameService.getAllVideoGames()) {
            boolean titleMatch = titleQuery == null || titleQuery.trim().isEmpty() || g.getTitle().toLowerCase().contains(titleQuery.toLowerCase().trim());
            boolean platformMatch = platformQuery == null || platformQuery.trim().isEmpty() || g.getPlatform().toLowerCase().contains(platformQuery.toLowerCase().trim());
            if (titleMatch && platformMatch) {
                filtered.add(g);
            }
        }
        gameList.setAll(filtered);
    }

    private void refreshGameTable() {
        gameList.setAll(gameService.getAllVideoGames());
        gameTable.refresh();
    }

    private void openDialog(VideoGame gameToEdit) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(gameToEdit == null ? "Registrar Videojuego" : "Modificar Videojuego");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtTitle = new TextField();
        if (gameToEdit != null) {
            txtTitle.setText(gameToEdit.getTitle());
            txtTitle.setDisable(true);
        }

        TextField txtPrice = new TextField();
        if (gameToEdit != null) txtPrice.setText(String.valueOf(gameToEdit.getPrice()));

        TextField txtPlatform = new TextField();
        if (gameToEdit != null) txtPlatform.setText(gameToEdit.getPlatform());

        TextField txtStock = new TextField();
        if (gameToEdit != null) txtStock.setText(String.valueOf(gameToEdit.getStock()));

        TextField txtGenre = new TextField();
        if (gameToEdit != null) txtGenre.setText(gameToEdit.getGenre());

        ComboBox<String> comboType = new ComboBox<>(FXCollections.observableArrayList("Digital", "Físico"));
        if (gameToEdit != null) {
            comboType.setValue(gameToEdit instanceof DigitalVideoGame ? "Digital" : "Físico");
            comboType.setDisable(true);
        } else {
            comboType.setValue("Digital");
        }

        grid.add(new Label("Título:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Precio ($):"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new Label("Plataforma:"), 0, 2);
        grid.add(txtPlatform, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(txtStock, 1, 3);
        grid.add(new Label("Género:"), 0, 4);
        grid.add(txtGenre, 1, 4);
        grid.add(new Label("Tipo:"), 0, 5);
        grid.add(comboType, 1, 5);

        // Specific fields
        VBox digitalBox = new VBox(5);
        TextField txtSize = new TextField();
        txtSize.setPromptText("Tamaño en GB");
        TextField txtDlPlatform = new TextField();
        txtDlPlatform.setPromptText("Tienda de descarga");
        digitalBox.getChildren().addAll(new Label("Tamaño (GB):"), txtSize, new Label("Plataforma de descarga:"), txtDlPlatform);

        VBox physicalBox = new VBox(5);
        ComboBox<String> comboCondition = new ComboBox<>(FXCollections.observableArrayList("nuevo", "usado"));
        comboCondition.setValue("nuevo");
        TextField txtDistributor = new TextField();
        txtDistributor.setPromptText("Distribuidora");
        physicalBox.getChildren().addAll(new Label("Estado:"), comboCondition, new Label("Distribuidora:"), txtDistributor);

        grid.add(digitalBox, 0, 6, 2, 1);
        grid.add(physicalBox, 0, 7, 2, 1);

        Runnable updateFieldsVisibility = () -> {
            boolean isDigital = "Digital".equals(comboType.getValue());
            digitalBox.setVisible(isDigital);
            digitalBox.setManaged(isDigital);
            physicalBox.setVisible(!isDigital);
            physicalBox.setManaged(!isDigital);
            dialog.sizeToScene();
        };

        comboType.setOnAction(e -> updateFieldsVisibility.run());

        if (gameToEdit instanceof DigitalVideoGame) {
            DigitalVideoGame d = (DigitalVideoGame) gameToEdit;
            txtSize.setText(String.valueOf(d.getSizeGB()));
            txtDlPlatform.setText(d.getDownloadPlatform());
        } else if (gameToEdit instanceof PhysicalVideoGame) {
            PhysicalVideoGame p = (PhysicalVideoGame) gameToEdit;
            comboCondition.setValue(p.getCondition());
            txtDistributor.setText(p.getDistributor());
        }

        updateFieldsVisibility.run();

        HBox dialogButtons = new HBox(10);
        dialogButtons.setAlignment(Pos.CENTER_RIGHT);
        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().addAll("btn", "btn-primary");
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().addAll("btn", "btn-secondary");

        btnCancel.setOnAction(e -> dialog.close());
        btnSave.setOnAction(e -> {
            try {
                String title = txtTitle.getText();
                double price = Double.parseDouble(txtPrice.getText());
                String platform = txtPlatform.getText();
                int stock = Integer.parseInt(txtStock.getText());
                String genre = txtGenre.getText();

                VideoGame game;
                if ("Digital".equals(comboType.getValue())) {
                    double size = Double.parseDouble(txtSize.getText());
                    String dl = txtDlPlatform.getText();
                    game = new DigitalVideoGame(title, price, platform, stock, genre, size, dl);
                } else {
                    String cond = comboCondition.getValue();
                    String dist = txtDistributor.getText();
                    game = new PhysicalVideoGame(title, price, platform, stock, genre, cond, dist);
                }

                if (gameToEdit == null) {
                    gameService.addVideoGame(game);
                } else {
                    gameService.updateVideoGame(gameToEdit.getTitle(), game);
                }
                
                dialog.close();
                refreshGameTable();
                updateDashboardStats();
            } catch (NumberFormatException ex) {
                showSimpleAlert(Alert.AlertType.ERROR, "Error", "Campos inválidos", "Ingrese valores numéricos válidos en Precio, Stock y Tamaño.");
            } catch (ValidationException | VideoGameAlreadyExistsException | VideoGameNotFoundException ex) {
                showSimpleAlert(Alert.AlertType.ERROR, "Error", "Error al guardar", ex.getMessage());
            }
        });

        dialogButtons.getChildren().addAll(btnCancel, btnSave);
        grid.add(dialogButtons, 1, 8);

        Scene scene = new Scene(grid);
        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void deleteGame(VideoGame game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar videojuego");
        alert.setContentText("¿Está seguro de que desea eliminar a '" + game.getTitle() + "' del catálogo?");
        
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    gameService.deleteVideoGame(game.getTitle());
                    refreshGameTable();
                    updateDashboardStats();
                } catch (VideoGameNotFoundException ex) {
                    showSimpleAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar", ex.getMessage());
                }
            }
        });
    }

    // ==========================================
    // 3. SELL TAB (TRANSACTIONS)
    // ==========================================
    private void createSellTab() {
        tabSell = new Tab("Registrar Venta");

        VBox content = new VBox(20);
        content.getStyleClass().add("content-pane");

        Label title = new Label("Registrar Nueva Transacción");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Seleccione un juego en inventario, defina la cantidad y proceda a guardar la venta.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox panelLayout = new HBox(40);
        panelLayout.setAlignment(Pos.TOP_LEFT);

        // Form
        VBox form = new VBox(15);
        form.setPrefWidth(350);

        comboSelectGame = new ComboBox<>();
        comboSelectGame.setPromptText("Seleccione un videojuego...");
        comboSelectGame.setPrefWidth(320);

        txtSaleQty = new TextField();
        txtSaleQty.setPromptText("Cantidad a vender");
        txtSaleQty.setPrefWidth(320);

        lblSaleTotal = new Label("Monto Total: $0.00");
        lblSaleTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #28A745; -fx-padding: 10 0 10 0;");

        Button btnSaveSale = new Button("Completar Venta");
        btnSaveSale.getStyleClass().addAll("btn", "btn-success");
        btnSaveSale.setPrefWidth(180);

        form.getChildren().addAll(
            new Label("Videojuego:"), comboSelectGame,
            new Label("Cantidad:"), txtSaleQty,
            lblSaleTotal, btnSaveSale
        );

        // Info Card
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #CED4DA; -fx-border-radius: 4; -fx-padding: 15;");
        infoBox.setPrefWidth(350);
        infoBox.setPrefHeight(180);

        Label infoTitle = new Label("Información del Videojuego");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #007BFF;");

        lblSalePrice = new Label("Precio base: -");
        lblSaleFinalPrice = new Label("Precio final: -");
        lblSalePlatform = new Label("Plataforma: -");
        lblSaleStock = new Label("Stock disponible: -");

        infoBox.getChildren().addAll(infoTitle, new Separator(), lblSalePrice, lblSaleFinalPrice, lblSalePlatform, lblSaleStock);

        panelLayout.getChildren().addAll(form, infoBox);

        // Listeners
        comboSelectGame.setOnAction(e -> {
            updateSelectedGameInfo();
            calculateTotalAmount();
        });
        txtSaleQty.textProperty().addListener((obs, oldV, newV) -> calculateTotalAmount());

        btnSaveSale.setOnAction(e -> processSale());

        content.getChildren().addAll(title, subtitle, panelLayout);
        tabSell.setContent(content);
    }

    private void refreshSellDropdown() {
        List<VideoGame> games = gameService.getAllVideoGames();
        List<String> titles = new ArrayList<>();
        for (VideoGame g : games) {
            if (g.getStock() > 0) {
                titles.add(g.getTitle());
            }
        }
        comboSelectGame.setItems(FXCollections.observableArrayList(titles));
        
        comboSelectGame.setValue(null);
        txtSaleQty.clear();
        lblSaleTotal.setText("Monto Total: $0.00");
        updateSelectedGameInfo();
    }

    private void updateSelectedGameInfo() {
        String title = comboSelectGame.getValue();
        if (title == null) {
            lblSalePrice.setText("Precio base: -");
            lblSaleFinalPrice.setText("Precio final: -");
            lblSalePlatform.setText("Plataforma: -");
            lblSaleStock.setText("Stock disponible: -");
            return;
        }

        VideoGame g = gameService.getByTitle(title);
        if (g != null) {
            lblSalePrice.setText(String.format("Precio base: $%.2f", g.getPrice()));
            lblSaleFinalPrice.setText(String.format("Precio final: $%.2f", g.calculateFinalPrice()));
            lblSalePlatform.setText("Plataforma: " + g.getPlatform());
            lblSaleStock.setText("Stock disponible: " + g.getStock());
        }
    }

    private void calculateTotalAmount() {
        String title = comboSelectGame.getValue();
        if (title == null) {
            lblSaleTotal.setText("Monto Total: $0.00");
            return;
        }

        VideoGame g = gameService.getByTitle(title);
        if (g == null) {
            lblSaleTotal.setText("Monto Total: $0.00");
            return;
        }

        try {
            int qty = Integer.parseInt(txtSaleQty.getText().trim());
            if (qty > 0) {
                double total = g.calculateFinalPrice() * qty;
                lblSaleTotal.setText(String.format("Monto Total: $%.2f", total));
            } else {
                lblSaleTotal.setText("Monto Total: $0.00");
            }
        } catch (NumberFormatException e) {
            lblSaleTotal.setText("Monto Total: $0.00");
        }
    }

    private void processSale() {
        String title = comboSelectGame.getValue();
        if (title == null) {
            showSimpleAlert(Alert.AlertType.WARNING, "Alerta", "Sin videojuego", "Seleccione un videojuego para la venta.");
            return;
        }

        try {
            int qty = Integer.parseInt(txtSaleQty.getText().trim());

            Sale sale = saleService.venderVideojuego(title, qty);
            showSimpleAlert(Alert.AlertType.INFORMATION, "Éxito", "Venta realizada", 
                    String.format("Venta procesada correctamente.\nCódigo: %s\nTotal: $%.2f", sale.getId(), sale.getTotal()));

            txtSaleQty.clear();
            refreshSellDropdown();
            updateDashboardStats();
        } catch (NumberFormatException ex) {
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Cantidad inválida", "La cantidad ingresada debe ser un número entero.");
        } catch (VideoGameNotFoundException | InsufficientStockException | ValidationException ex) {
            showSimpleAlert(Alert.AlertType.ERROR, "Venta fallida", "No se completó la venta", ex.getMessage());
        }
    }

    // ==========================================
    // 4. HISTORY TAB
    // ==========================================
    private void createHistoryTab() {
        tabHistory = new Tab("Historial de Ventas");

        VBox content = new VBox(15);
        content.getStyleClass().add("content-pane");

        Label title = new Label("Registro Histórico de Ventas");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Lista de todas las ventas guardadas en el sistema.");
        subtitle.getStyleClass().add("subtitle-label");

        // Sales table
        saleTable = new TableView<>();
        saleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Sale, String> colSaleId = new TableColumn<>("ID");
        colSaleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSaleId.setPrefWidth(60);

        TableColumn<Sale, String> colGame = new TableColumn<>("Videojuego");
        colGame.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVideoGame() != null ? cellData.getValue().getVideoGame().getTitle() : "-"
        ));
        colGame.setPrefWidth(250);

        TableColumn<Sale, Integer> colQty = new TableColumn<>("Cant.");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(60);

        TableColumn<Sale, Double> colPrice = new TableColumn<>("Precio Unit.");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colPrice.setPrefWidth(100);

        TableColumn<Sale, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(100);

        TableColumn<Sale, String> colDate = new TableColumn<>("Fecha");
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSaleDate() != null ? cellData.getValue().getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "-"
        ));
        colDate.setPrefWidth(180);

        saleTable.getColumns().addAll(colSaleId, colGame, colQty, colPrice, colTotal, colDate);
        saleTable.setItems(saleList);

        HBox summary = new HBox();
        summary.setAlignment(Pos.CENTER_RIGHT);
        lblHistoryTotalRevenue = new Label("Total Acumulado: $0.00");
        lblHistoryTotalRevenue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #007BFF;");
        summary.getChildren().add(lblHistoryTotalRevenue);

        content.getChildren().addAll(title, subtitle, saleTable, summary);
        tabHistory.setContent(content);
    }

    private void refreshSalesHistoryTable() {
        List<Sale> allSales = saleService.getAllSales();
        saleList.setAll(allSales);
        saleTable.refresh();

        double total = 0.0;
        for (Sale s : allSales) {
            total += s.getTotal();
        }
        lblHistoryTotalRevenue.setText(String.format("Total Acumulado: $%.2f", total));
    }

    // Helper
    private void showSimpleAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}