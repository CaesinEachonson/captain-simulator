package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;

public class BattleOverviewPage extends VBox {

    public BattleOverviewPage() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #1a1a2e;");

        VBox header = new VBox(5);
        header.setMinHeight(60);
        header.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        HBox battleCards = new HBox(15);
        battleCards.setMinHeight(180);
        for (int i = 0; i < 3; i++) {
            VBox card = new VBox();
            card.setMinSize(250, 160);
            card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-border-color: #0f3460;");
            battleCards.getChildren().add(card);
        }

        VBox battleLog = new VBox(10);
        battleLog.setMinHeight(300);
        battleLog.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
        battleLog.setPadding(new Insets(15));

        getChildren().addAll(header, battleCards, battleLog);

        for (var child : getChildren()) {
            if (child instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        }
    }
}

