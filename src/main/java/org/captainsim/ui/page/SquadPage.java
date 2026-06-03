package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.MarineUnit;

public class SquadPage extends VBox {

    private final Squad squad;

    public SquadPage(Squad squad) {
        this.squad = squad;
        setSpacing(10);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #1a1a2e;");

        // Header
        Label title = new Label(squad.getDisplayName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        Label subtitle = new Label(squad.getSize() + " brothers");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");

        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(0, 0, 10, 0));

        // Marine cards in a scrollable grid
        FlowPane marineGrid = new FlowPane();
        marineGrid.setHgap(12);
        marineGrid.setVgap(12);

        for (MarineUnit m : squad.getAllMarines()) {
            marineGrid.getChildren().add(createMarineCard(m));
        }

        ScrollPane scrollPane = new ScrollPane(marineGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createMarineCard(MarineUnit m) {
        // Name and role
        Label nameLabel = new Label(m.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label roleLabel = new Label(m.getRole().name().replace('_', ' '));
        roleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #d4af37;");

        // Equipment
        String rh = m.getRightHand() != null ? m.getRightHand().getName() : "—";
        String lh = m.getLeftHand() != null ? m.getLeftHand().getName() : "—";
        String arm = m.getArmorKit() != null ? m.getArmorKit().getName() : "—";

        Label equipLabel = new Label("RH: " + rh + "\nLH: " + lh + "\nArmour: " + arm);
        equipLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa; -fx-line-spacing: 2;");

        // Attributes
        String attrs = String.format("WS:%d BS:%d S:%d T:%d Ag:%d",
                m.getWs(), m.getBs(), m.getS(), m.getT(), m.getAg());
        Label attrLabel = new Label(attrs);
        attrLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");

        // Health
        int hpPercent = m.getCurrentWounds() > 0
                ? Math.round((float) m.getCurrentWounds() / m.getWounds() * 100)
                : 0;
        String hpColor = hpPercent > 50 ? "#4a7f4a" : (hpPercent > 20 ? "#8b8b00" : "#8b0000");
        Label hpLabel = new Label(m.getCurrentWounds() + "/" + m.getWounds());
        hpLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + hpColor + ";");

        VBox card = new VBox(6, nameLabel, roleLabel, equipLabel, attrLabel, hpLabel);
        card.setPadding(new Insets(12));
        card.setPrefWidth(220);
        card.setMinHeight(160);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 6; -fx-border-color: #0f3460; -fx-border-radius: 6;");

        return card;
    }
}
