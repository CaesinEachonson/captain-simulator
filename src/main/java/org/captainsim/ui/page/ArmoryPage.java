package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.captainsim.ui.consts.ThemeConst;

public class ArmoryPage extends VBox {

    public ArmoryPage() {
        setSpacing(20);
        setPadding(new Insets(30));
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        VBox header = new VBox(5);
        header.setMinHeight(60);
        header.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        header.setStyle(ThemeConst.FONT_XL);

        HBox weaponOverview = new HBox(15);
        weaponOverview.setMinHeight(150);
        for (int i = 0; i < 5; i++) {
            VBox category = new VBox();
            category.setMinSize(120, 130);
            category.getStyleClass().add(ThemeConst.CSS_CARD);
            weaponOverview.getChildren().add(category);
        }

        HBox mainContent = new HBox(20);
        mainContent.setMinHeight(350);

        VBox equipmentPool = new VBox(10);
        equipmentPool.setMinWidth(300);
        equipmentPool.getStyleClass().add(ThemeConst.CSS_CARD);
        equipmentPool.setPadding(new Insets(15));

        VBox distributionPanel = new VBox(10);
        distributionPanel.setMinWidth(400);
        distributionPanel.getStyleClass().add(ThemeConst.CSS_CARD);
        distributionPanel.setPadding(new Insets(15));

        mainContent.getChildren().addAll(equipmentPool, distributionPanel);
        HBox.setHgrow(equipmentPool, Priority.ALWAYS);
        HBox.setHgrow(distributionPanel, Priority.ALWAYS);

        getChildren().addAll(header, weaponOverview, mainContent);

        for (var child : getChildren()) {
            if (child instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        }
    }
}

