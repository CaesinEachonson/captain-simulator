package org.captainsim;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.captainsim.company.Company;
import org.captainsim.squad.Squad;
import org.captainsim.ui.page.*;

public class CaptainSimulatorApp extends Application {

    private StackPane mainContent;

    @Override
    public void start(Stage primaryStage) {
        try {
            GameData.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load game data", e);
        }
        try {
            Company company = CompanyGenerator.generate();
            GameData.getInstance().setCompany(company);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate company", e);
        }

        // ===== Top Bar =====
        Label titleLabel = new Label("CAPTAIN SIMULATOR");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        Label infoLabel = new Label("3rd Company  |  100 Brothers  |  Supply: Adequate");
        infoLabel.setStyle("-fx-text-fill: #cccccc;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(20, titleLabel, spacer, infoLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #1a1a2e;");

        // ===== Sidebar =====
        Button btnHome    = new Button("Home");
        Button btnCommand = new Button("Command");
        Button btnSquads  = new Button("Squads");
        Button btnArmory  = new Button("Armory");
        Button btnBattle  = new Button("Battle");

        String btnStyle = "-fx-font-size: 14px; -fx-padding: 12px 20px; " +
                "-fx-background-color: #16213e; -fx-text-fill: #e0e0e0; " +
                "-fx-border-color: #0f3460; -fx-border-width: 0 0 1 0;";
        for (var btn : new Button[]{btnHome, btnCommand, btnSquads, btnArmory, btnBattle}) {
            btn.setStyle(btnStyle);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        VBox sideBar = new VBox(btnHome, btnCommand, btnSquads, btnArmory, btnBattle);
        sideBar.setStyle("-fx-background-color: #16213e;");
        sideBar.setPrefWidth(160);
        sideBar.setFillWidth(true);

        // ===== Main Content Area =====
        mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: #1a1a2e;");

        // ===== Page Switching =====
        btnHome.setOnAction(e -> showPage(new HomePage()));
        btnCommand.setOnAction(e -> showPage(new CommandCenterPage()));
        btnSquads.setOnAction(e -> showPage(new CompanyPage(this::openSquadPage)));
        btnArmory.setOnAction(e -> showPage(new ArmoryPage()));
        btnBattle.setOnAction(e -> showPage(new BattleOverviewPage()));

        // Default to Home
        showPage(new HomePage());

        // ===== Bottom Bar =====
        Label statusLabel = new Label("▸ All brothers standing by");
        statusLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        HBox bottomBar = new HBox(statusLabel);
        bottomBar.setPadding(new Insets(8, 20, 8, 20));
        bottomBar.setStyle("-fx-background-color: #0f3460;");

        // ===== Assemble =====
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Captain Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // ===== Navigation Helper =====
    private void showPage(Node page) {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(page);
    }

    // Called when a squad card is clicked in CompanyPage
    private void openSquadPage(Squad squad) {
        showPage(new SquadPage(squad));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
