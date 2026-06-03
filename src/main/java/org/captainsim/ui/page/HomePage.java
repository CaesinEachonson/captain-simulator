package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;

public class HomePage extends VBox {

    public HomePage() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #1a1a2e;");

        VBox header = new VBox(5);
        header.setMinHeight(80);
        header.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        HBox summaryCards = new HBox(20);
        summaryCards.setMinHeight(120);
        for (int i = 0; i < 4; i++) {
            VBox card = new VBox();
            card.setMinSize(180, 100);
            card.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8;");
            summaryCards.getChildren().add(card);
        }

        HBox quickActions = new HBox(20);
        quickActions.setMinHeight(150);
        for (int i = 0; i < 3; i++) {
            VBox action = new VBox();
            action.setMinSize(200, 120);
            action.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-border-color: #0f3460;");
            quickActions.getChildren().add(action);
        }

        VBox recentActivity = new VBox(10);
        recentActivity.setMinHeight(200);
        recentActivity.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
        recentActivity.setPadding(new Insets(15));

        getChildren().addAll(header, summaryCards, quickActions, recentActivity);

        for (var child : getChildren()) {
            if (child instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        }
    }
}
