package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.MarineUnit;

import java.util.function.Consumer;

public class CompanyPage extends VBox {

    private final Consumer<Squad> onSquadClick;

    public CompanyPage(Consumer<Squad> onSquadClick) {
        this.onSquadClick = onSquadClick;
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #1a1a2e;");

        // Header
        Label header = new Label("Company Squads");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        int total = GameData.getInstance().getTotalMarines();
        Label subtext = new Label("10 Squads · " + total + " Brothers");
        subtext.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");

        VBox headerBox = new VBox(5, header, subtext);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // Squad cards in a scrollable grid
        FlowPane squadGrid = new FlowPane();
        squadGrid.setHgap(15);
        squadGrid.setVgap(15);

        for (Squad squad : GameData.getInstance().getSquads()) {
            squadGrid.getChildren().add(createSquadCard(squad));
        }

        ScrollPane scrollPane = new ScrollPane(squadGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        getChildren().addAll(headerBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createSquadCard(Squad squad) {
        String typeLabel = squad.getSquadType().name().replace('_', ' ').toLowerCase();
        typeLabel = typeLabel.substring(0, 1).toUpperCase() + typeLabel.substring(1);

        int alive = (int) squad.getAllMarines().stream()
                .filter(MarineUnit::isAvailable).count();

        Label title = new Label(squad.getDisplayName());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        Label count = new Label(alive + "/" + squad.getSize() + " brothers available");
        count.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        VBox card = new VBox(8, title, count);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-border-color: #0f3460; -fx-border-radius: 8;");
        card.setPrefWidth(280);
        card.setMinHeight(80);
        card.setMaxHeight(80);

        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && onSquadClick != null) {
                onSquadClick.accept(squad);
            }
        });
        card.setCursor(Cursor.HAND);

        return card;
    }
}
