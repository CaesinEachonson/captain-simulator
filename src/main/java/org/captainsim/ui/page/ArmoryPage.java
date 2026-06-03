package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.layout.*;

public class ArmoryPage extends VBox {

    public ArmoryPage() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #1a1a2e;");

        VBox header = new VBox(5);
        header.setMinHeight(60);
        header.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        HBox weaponOverview = new HBox(15);
        weaponOverview.setMinHeight(150);
        for (int i = 0; i < 5; i++) {
            VBox category = new VBox();
            category.setMinSize(120, 130);
            category.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 6;");
            weaponOverview.getChildren().add(category);
        }

        HBox mainContent = new HBox(20);
        mainContent.setMinHeight(350);

        VBox equipmentPool = new VBox(10);
        equipmentPool.setMinWidth(300);
        equipmentPool.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
        equipmentPool.setPadding(new Insets(15));

        VBox distributionPanel = new VBox(10);
        distributionPanel.setMinWidth(400);
        distributionPanel.setStyle("-fx-border-color: #333; -fx-border-width: 1; -fx-border-radius: 4;");
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

