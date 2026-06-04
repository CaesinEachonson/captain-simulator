package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.captainsim.squad.Squad;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.unit.marine.MarineUnit;

public class SquadPage extends VBox {

    private final Squad squad;

    public SquadPage(Squad squad) {
        this.squad = squad;
        setSpacing(10);
        setPadding(new Insets(20));
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        // Header
        Label title = new Label(squad.getDisplayName());
        title.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        title.setStyle(ThemeConst.FONT_XL);

        Label subtitle = new Label(squad.getSize() + " brothers");
        subtitle.getStyleClass().add(ThemeConst.CSS_PAGE_SUBTITLE);
        subtitle.setStyle(ThemeConst.FONT_S);

        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(0, 0, 10, 0));

        // Marine cards — 2 per row, 5 rows
        GridPane marineGrid = new GridPane();
        marineGrid.setHgap(15);
        marineGrid.setVgap(15);
        marineGrid.setPadding(new Insets(0));
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        marineGrid.getColumnConstraints().addAll(col1, col2);

        int col = 0;
        int row = 0;
        for (MarineUnit m : squad.getAllMarines()) {
            marineGrid.add(createMarineCard(m), col, row);
            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(marineGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("scroll-pane");

        getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createMarineCard(MarineUnit m) {
        // Portrait placeholder
        StackPane portraitPlaceholder = new StackPane();
        portraitPlaceholder.setMinSize(180, 240);
        portraitPlaceholder.setMaxSize(180, 240);
        portraitPlaceholder.getStyleClass().add(ThemeConst.CSS_PORTRAIT_FRAME);
        // Name
        Label nameLabel = new Label(m.getName());
        nameLabel.getStyleClass().add(ThemeConst.CSS_TEXT_PRIMARY);
        nameLabel.setStyle(ThemeConst.FONT_XL);   // 22px
        // Role
        Label roleLabel = new Label(m.getRole().name().replace('_', ' '));
        roleLabel.getStyleClass().add(ThemeConst.CSS_TEXT_SECONDARY);
        roleLabel.setStyle(ThemeConst.FONT_M);
        // Equipment
        String rh = m.getRightHand() != null ? m.getRightHand().getName() : "—";
        String lh = m.getLeftHand() != null ? m.getLeftHand().getName() : "—";
        String arm = m.getArmorKit() != null ? m.getArmorKit().getName() : "—";
        Label equipLabel = new Label("RH: " + rh + "\nLH: " + lh + "\nArmour: " + arm);
        equipLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        equipLabel.setStyle(ThemeConst.FONT_S);
        // Attributes
        String attrs = String.format("WS:%d BS:%d S:%d T:%d Ag:%d",
                m.getWs(), m.getBs(), m.getS(), m.getT(), m.getAg());
        Label attrLabel = new Label(attrs);
        attrLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        attrLabel.setStyle(ThemeConst.FONT_S);
        // Health
        String hpClass = getHpClass(m);
        Label hpLabel = new Label(m.getCurrentWounds() + "/" + m.getWounds());
        hpLabel.getStyleClass().add(hpClass);
        hpLabel.setStyle(ThemeConst.FONT_M);
        VBox leftSection = new VBox(5, portraitPlaceholder);
        leftSection.setAlignment(javafx.geometry.Pos.CENTER);
        leftSection.setPadding(new Insets(0, 15, 0, 0));
        // Right: info
        VBox infoSection = new VBox(6, nameLabel, roleLabel, equipLabel, attrLabel, hpLabel);
        HBox cardContent = new HBox(10, leftSection, infoSection);
        cardContent.setPadding(new Insets(15));
        cardContent.getStyleClass().add(ThemeConst.CSS_CARD);
//        cardContent.setPrefWidth(500);
//        cardContent.setMinHeight(240);
//        cardContent.setMaxHeight(240);
        cardContent.setPrefWidth(Region.USE_COMPUTED_SIZE);
        cardContent.setPrefHeight(Region.USE_COMPUTED_SIZE);
        cardContent.setMaxWidth(Double.MAX_VALUE);
        return new VBox(cardContent);
    }

    private static String getHpClass(MarineUnit m) {
        int hpPercent = m.getCurrentWounds() > 0
                ? Math.round((float) m.getCurrentWounds() / m.getWounds() * 100)
                : 0;
        String hpClass;
        if (hpPercent > 50)      hpClass = ThemeConst.CSS_HP_GOOD;
        else if (hpPercent > 20) hpClass = ThemeConst.CSS_HP_WOUNDED;
        else                     hpClass = ThemeConst.CSS_HP_CRITICAL;
        return hpClass;
    }
}
