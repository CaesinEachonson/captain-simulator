package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.squad.Squad;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.unit.marine.MarineUnit;

import java.util.function.Consumer;

public class CompanyPage extends VBox {

    private final Consumer<Squad> onSquadClick;

    public CompanyPage(Consumer<Squad> onSquadClick) {
        this.onSquadClick = onSquadClick;
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        // Header
        Label header = new Label("Company Squads");
        header.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        header.setStyle(ThemeConst.FONT_XL);

        int total = GameData.getInstance().getTotalMarines();
        Label subtext = new Label("10 Squads · " + total + " Brothers");
        subtext.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        subtext.setStyle(ThemeConst.FONT_S);

        VBox headerBox = new VBox(5, header, subtext);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        // Squad list — one per row
        VBox squadList = new VBox(8);
        squadList.setPadding(new Insets(0));

        for (Squad squad : GameData.getInstance().getSquads()) {
            squadList.getChildren().add(createSquadRow(squad));
        }

        ScrollPane scrollPane = new ScrollPane(squadList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        getChildren().addAll(headerBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private HBox createSquadRow(Squad squad) {
        String typeLabel = squad.getSquadType().name().replace('_', ' ').toLowerCase();
        typeLabel = typeLabel.substring(0, 1).toUpperCase() + typeLabel.substring(1);

        int alive = (int) squad.getAllMarines().stream()
                .filter(MarineUnit::isAvailable).count();

        // Squad name + honor title
        Label nameLabel = new Label(squad.getDisplayName());
        nameLabel.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        nameLabel.setStyle(ThemeConst.FONT_M);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Type badge
        Label typeBadge = new Label(typeLabel);
        typeBadge.getStyleClass().add(ThemeConst.CSS_BADGE);

        // Status
        Label statusLabel = new Label(alive + "/" + squad.getSize());
        statusLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        statusLabel.setStyle(ThemeConst.FONT_M);

        HBox row = new HBox(15, nameLabel, typeBadge, statusLabel);
        row.setPadding(new Insets(12, 15, 12, 15));
        row.getStyleClass().add(ThemeConst.CSS_CARD);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(50);

        row.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && onSquadClick != null) {
                onSquadClick.accept(squad);
            }
        });
        row.setCursor(Cursor.HAND);

        return row;
    }
}
