package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.captainsim.ui.consts.ThemeConst;

public class HomePage extends VBox {

    public HomePage() {
        setSpacing(20);
        setPadding(new Insets(30));
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        VBox header = new VBox(5);
        header.setMinHeight(80);
        header.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        header.setStyle(ThemeConst.FONT_XL);

        HBox summaryCards = new HBox(20);
        summaryCards.setMinHeight(120);
        for (int i = 0; i < 4; i++) {
            VBox card = new VBox();
            card.setMinSize(180, 100);
            card.getStyleClass().add(ThemeConst.CSS_CARD);
            summaryCards.getChildren().add(card);
        }

        HBox quickActions = new HBox(20);
        quickActions.setMinHeight(150);
        for (int i = 0; i < 3; i++) {
            VBox action = new VBox();
            action.setMinSize(200, 120);
            action.getStyleClass().add(ThemeConst.CSS_CARD);
            quickActions.getChildren().add(action);
        }

        VBox recentActivity = new VBox(10);
        recentActivity.setMinHeight(200);
        recentActivity.getStyleClass().add(ThemeConst.CSS_CARD);
        recentActivity.setPadding(new Insets(15));

        getChildren().addAll(header, summaryCards, quickActions, recentActivity);

        for (var child : getChildren()) {
            if (child instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        }
    }
}
