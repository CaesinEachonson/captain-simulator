package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class CommandCenterPage extends VBox {

    public CommandCenterPage() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #1a1a2e;");

        // ===== Header =====
        VBox header = new VBox(5);
        header.setMinHeight(60);
        header.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        // ===== Stats Cards Row =====
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        cards.setMinHeight(120);
        for (int i = 0; i < 4; i++) {
            VBox placeholder = new VBox();
            placeholder.setMinSize(160, 100);
            placeholder.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8;");
            cards.getChildren().add(placeholder);
        }

        // ===== Equipment Overview =====
        VBox equipSection = new VBox(10);
        equipSection.setMinHeight(200);
        equipSection.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
        equipSection.setPadding(new Insets(15));

        // ===== Recent Battles =====
        VBox recentSection = new VBox(10);
        recentSection.setMinHeight(150);
        recentSection.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
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
