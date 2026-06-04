package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import org.captainsim.ui.consts.ThemeConst;

public class CommandCenterPage extends VBox {

    public CommandCenterPage() {
        setSpacing(20);
        setPadding(new Insets(30));
//        setStyle("-fx-background-color: #1a1a2e;");
        getStyleClass().add(ThemeConst.CSS_BG_DARK);


        // ===== Header =====
        VBox header = new VBox(5);
        header.setMinHeight(60);

        // ===== Stats Cards Row =====
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        cards.setMinHeight(120);
        for (int i = 0; i < 4; i++) {
            VBox placeholder = new VBox();
            placeholder.setMinSize(160, 100);
            placeholder.getStyleClass().add(ThemeConst.CSS_CARD);
            cards.getChildren().add(placeholder);
        }

        // ===== Equipment Overview =====
        VBox equipSection = new VBox(10);
        equipSection.setMinHeight(200);
        equipSection.getStyleClass().add(ThemeConst.CSS_CARD);
        equipSection.setPadding(new Insets(15));

        // ===== Recent Battles =====
        VBox recentSection = new VBox(10);
        recentSection.setMinHeight(150);
        recentSection.getStyleClass().add(ThemeConst.CSS_CARD);
        recentSection.setPadding(new Insets(15));

        // ===== Assemble =====
        getChildren().addAll(header, cards, equipSection, recentSection);

        for (var child : getChildren()) {
            if (child instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
            }
        }
    }
}
