package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.captainsim.ui.consts.ThemeConst;

public class BattleOverviewPage extends VBox {

    public BattleOverviewPage() {
        setSpacing(20);
        setPadding(new Insets(30));
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        VBox header = new VBox(5);
        header.setMinHeight(60);
        header.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);

        HBox battleCards = new HBox(15);
        battleCards.setMinHeight(180);
        for (int i = 0; i < 3; i++) {
            VBox card = new VBox();
            card.setMinSize(250, 160);
            card.getStyleClass().add(ThemeConst.CSS_CARD);
            battleCards.getChildren().add(card);
        }

        VBox battleLog = new VBox(10);
        battleLog.setMinHeight(300);
        battleLog.getStyleClass().add(ThemeConst.CSS_TEXT);

        getChildren().addAll(header, battleCards, battleLog);

        for (var child : getChildren()) {
            if (child instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        }
    }
}

