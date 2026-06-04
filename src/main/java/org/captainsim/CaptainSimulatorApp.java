package org.captainsim;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.captainsim.company.Company;
import org.captainsim.squad.Squad;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.ui.page.*;
import org.captainsim.util.CompanyGenerator;

import java.util.Objects;

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
        titleLabel.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        titleLabel.setStyle(ThemeConst.FONT_XL);

        Label infoLabel = new Label("3rd Company  |  100 Brothers  |  Supply: Adequate");
        infoLabel.getStyleClass().add(ThemeConst.CSS_TEXT_SECONDARY);
        infoLabel.setStyle(ThemeConst.FONT_M);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(20, titleLabel, spacer, infoLabel);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // ===== Sidebar =====
        Button btnHome    = new Button("Home");
        Button btnCommand = new Button("Command");
        Button btnSquads  = new Button("Squads");
        Button btnArmory  = new Button("Armory");
        Button btnBattle  = new Button("Battle");

        for (var btn : new Button[]{btnHome, btnCommand, btnSquads, btnArmory, btnBattle}) {
            btn.getStyleClass().add(ThemeConst.CSS_SIDEBAR_BTN);
            btn.setStyle(ThemeConst.FONT_M);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        VBox sideBar = new VBox(btnHome, btnCommand, btnSquads, btnArmory, btnBattle);
        sideBar.getStyleClass().add("sidebar");
        sideBar.setFillWidth(true);

        // ===== Main Content Area =====
        mainContent = new StackPane();
        mainContent.getStyleClass().add("bg-dark");

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
        statusLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        statusLabel.setStyle(ThemeConst.FONT_S);

        HBox bottomBar = new HBox(statusLabel);
        bottomBar.getStyleClass().add("bottom-bar");

        // ===== Assemble =====
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/theme.css")).toExternalForm());
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

    private void openSquadPage(Squad squad) {
        showPage(new SquadPage(squad));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
