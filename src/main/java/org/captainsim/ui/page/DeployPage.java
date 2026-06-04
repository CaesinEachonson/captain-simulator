package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.campaign.Campaign;
import org.captainsim.campaign.CampaignMission;
import org.captainsim.squad.Squad;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.unit.marine.MarineUnit;

import java.util.ArrayList;
import java.util.List;

public class DeployPage extends VBox {

    private final CampaignMission mission;
    private final Runnable onDeployConfirmed;
    private final List<String> selectedSquadIds = new ArrayList<>();
    private final VBox squadListContainer;
    private final Button confirmBtn;

    public DeployPage(CampaignMission mission, Runnable onDeployConfirmed) {
        this.mission = mission;
        this.onDeployConfirmed = onDeployConfirmed;

        setSpacing(15);
        setPadding(new Insets(20));
        setFillWidth(true);
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        // ===== Header =====
        Label title = new Label(mission.getName());
        title.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        title.setStyle(ThemeConst.FONT_XL);

        // ===== Mission Details =====
        VBox details = new VBox(8);
        details.setPadding(new Insets(15));
        details.getStyleClass().add(ThemeConst.CSS_CARD);

        Label typeLabel = new Label("Type: " + mission.getType().name());
        typeLabel.setStyle(ThemeConst.FONT_L);
        typeLabel.getStyleClass().add(ThemeConst.CSS_TEXT_PRIMARY);

        Label diffLabel = new Label("Expected Difficulty: " + mission.getExpectedDifficulty());
        diffLabel.setStyle(ThemeConst.FONT_M);
        diffLabel.getStyleClass().add(ThemeConst.CSS_TEXT_SECONDARY);

        Label narrative = new Label(mission.getNarrativeDescription());
        narrative.setWrapText(true);
        narrative.setStyle(ThemeConst.FONT_S);
        narrative.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        Label intel = new Label("Intelligence: " + mission.getIntelligenceSummary());
        intel.setWrapText(true);
        intel.setStyle(ThemeConst.FONT_S);
        intel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        details.getChildren().addAll(typeLabel, diffLabel, narrative, intel);

        // ===== Squad List =====
        Label squadHeader = new Label("Select Squads to Deploy");
        squadHeader.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        squadHeader.setStyle(ThemeConst.FONT_L);

        squadListContainer = new VBox(8);
        squadListContainer.setPadding(new Insets(0));
        refreshSquadList();

        ScrollPane squadScroll = new ScrollPane(squadListContainer);
        squadScroll.setFitToWidth(true);
        squadScroll.getStyleClass().add("scroll-pane");

        // ===== Confirm =====
        confirmBtn = new Button("Confirm Deployment");
        confirmBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12px 30px; " +
                "-fx-background-color: #0a3a0a; -fx-text-fill: #33ff33; " +
                "-fx-border-color: #33ff33; -fx-border-width: 1;");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> {
            Campaign campaign = GameData.getInstance().getCurrentCampaign();
            for (String id : selectedSquadIds) {
                mission.assignSquad(id);
                campaign.markSquadDeployed(id);
            }
            mission.confirmDeployment();
            if (onDeployConfirmed != null) onDeployConfirmed.run();
        });

        getChildren().addAll(title, details, squadHeader, squadScroll, confirmBtn);
        VBox.setVgrow(squadScroll, Priority.ALWAYS);
    }

    private void refreshSquadList() {
        squadListContainer.getChildren().clear();
        Campaign campaign = GameData.getInstance().getCurrentCampaign();

        for (Squad squad : GameData.getInstance().getSquads()) {
            if (campaign.isSquadDeployed(squad.getId())) {
                continue;
            }
            squadListContainer.getChildren().add(createSquadRow(squad));
        }
    }


    private HBox createSquadRow(Squad squad) {
        int alive = (int) squad.getAllMarines().stream()
                .filter(MarineUnit::isAvailable).count();

        Label nameLabel = new Label(squad.getDisplayName());
        nameLabel.setStyle(ThemeConst.FONT_M);
        nameLabel.getStyleClass().add(ThemeConst.CSS_TEXT_PRIMARY);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label statusLabel = new Label(alive + "/" + squad.getSize());
        statusLabel.setStyle(ThemeConst.FONT_M);
        statusLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        Label checkLabel = new Label("[ ]");
        checkLabel.setStyle(ThemeConst.FONT_L);
        checkLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        HBox row = new HBox(15, checkLabel, nameLabel, statusLabel);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.getStyleClass().add(ThemeConst.CSS_CARD);
        row.setMaxWidth(Double.MAX_VALUE);

        String squadId = squad.getId();
        row.setOnMouseClicked(e -> {
            if (selectedSquadIds.contains(squadId)) {
                selectedSquadIds.remove(squadId);
                checkLabel.setText("[ ]");
            } else {
                selectedSquadIds.add(squadId);
                checkLabel.setText("[X]");
            }
            confirmBtn.setDisable(selectedSquadIds.isEmpty());
        });

        return row;
    }
}
