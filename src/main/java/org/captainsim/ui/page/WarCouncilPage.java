package org.captainsim.ui.page;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.campaign.Campaign;
import org.captainsim.campaign.CampaignLog;
import org.captainsim.campaign.CampaignMission;
import org.captainsim.ui.consts.ThemeConst;

import java.util.function.Consumer;

public class WarCouncilPage extends VBox {

    private final Consumer<CampaignMission> onMissionClick;
    private final HBox missionContainer;
    private final VBox logContainer;
    private final Button confirmBtn;

    public WarCouncilPage(Consumer<CampaignMission> onMissionClick) {
        this.onMissionClick = onMissionClick;

        setSpacing(15);
        setPadding(new Insets(20));
        setFillWidth(true);
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        Campaign campaign = GameData.getInstance().getCurrentCampaign();

        // ===== Top: Round & VP =====
        Label roundLabel = new Label("Week " + campaign.getRound() + " — " + campaign.getPlanetName());
        roundLabel.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        roundLabel.setStyle(ThemeConst.FONT_XL);

        Label vpLabel = new Label("Imperial VP: " + campaign.getImperialVP()
                + "  /  Enemy VP: " + campaign.getEnemyVP());
        vpLabel.getStyleClass().add(ThemeConst.CSS_TEXT_SECONDARY);
        vpLabel.setStyle(ThemeConst.FONT_L);

        HBox topSection = new HBox(20, roundLabel, vpLabel);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, 10, 0));

        // ===== Mission Cards =====
        missionContainer = new HBox(15);
        missionContainer.setPadding(new Insets(5));

        ScrollPane missionScroll = new ScrollPane(missionContainer);
        missionScroll.setFitToHeight(true);
        missionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        missionScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        missionScroll.getStyleClass().add("scroll-pane");
        missionScroll.setMinHeight(180);
        missionScroll.setMaxHeight(180);

        // ===== Campaign Log =====
        Label logHeader = new Label("Campaign Log");
        logHeader.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        logHeader.setStyle(ThemeConst.FONT_M);

        logContainer = new VBox(8);
        logContainer.setPadding(new Insets(10));
        logContainer.getStyleClass().add(ThemeConst.CSS_CARD);
        logContainer.setMinHeight(400);
        refreshLog(campaign);

        ScrollPane logScroll = new ScrollPane(logContainer);
        logScroll.setFitToWidth(true);
        logScroll.getStyleClass().add("scroll-pane");
        logScroll.setMinHeight(500);

        VBox logSection = new VBox(5, logHeader, logScroll);
        logSection.setPrefHeight(500);
        logSection.setMinHeight(500);


        // ===== Confirm Button =====
        confirmBtn = new Button("Confirm Deployment");
        confirmBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12px 30px; " +
                "-fx-background-color: #0a3a0a; -fx-text-fill: #33ff33; " +
                "-fx-border-color: #33ff33; -fx-border-width: 1;");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setMinHeight(50);

        confirmBtn.setOnAction(e -> {
            boolean allDeployed = campaign.getMissions().stream()
                    .allMatch(m -> m.isDeployed() || m.isResolved());
            if (allDeployed) {
                confirmBtn.setText("Battle Phase — Click a mission to fight");
                confirmBtn.setDisable(true);
                refreshMissionCards(campaign);
            } else {
                System.out.println("Not all missions have been assigned squads yet.");
            }
        });

        refreshMissionCards(campaign);

        // ===== Stack everything in a VBox, put that in a ScrollPane =====
        VBox inner = new VBox(15, topSection, missionScroll, logSection, confirmBtn);
        inner.setPadding(new Insets(0));
        inner.setFillWidth(true);

        getChildren().add(inner);
    }

    private void refreshMissionCards(Campaign campaign) {
        missionContainer.getChildren().clear();
        boolean battlePhase = confirmBtn.isDisable();
        for (CampaignMission cm : campaign.getMissions()) {
            missionContainer.getChildren().add(createMissionCard(cm, battlePhase));
        }
    }

    private HBox createMissionCard(CampaignMission cm, boolean battlePhase) {
        Label nameLabel = new Label(cm.getName());
        nameLabel.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        nameLabel.setStyle(ThemeConst.FONT_M);

        Label typeLabel = new Label(cm.getType().name());
        typeLabel.getStyleClass().add(ThemeConst.CSS_TEXT_SECONDARY);
        typeLabel.setStyle(ThemeConst.FONT_S);

        Label diffLabel = new Label("Difficulty: " + cm.getExpectedDifficulty());
        diffLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        diffLabel.setStyle(ThemeConst.FONT_S);

        VBox leftSection = new VBox(5, nameLabel, typeLabel, diffLabel);
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        String statusText;
        if (cm.isCompleted()) {
            statusText = "✓ Completed";
        } else if (cm.isFailed()) {
            statusText = "✗ Failed";
        } else if (cm.isDeployed()) {
            statusText = cm.getAssignedSquadIds().size() + " squads assigned";
        } else {
            statusText = "Not deployed";
        }

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle(ThemeConst.FONT_S);
        if (cm.isCompleted()) {
            statusLabel.getStyleClass().add(ThemeConst.CSS_HP_GOOD);
        } else if (cm.isFailed()) {
            statusLabel.getStyleClass().add(ThemeConst.CSS_HP_CRITICAL);
        } else {
            statusLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);
        }

        HBox card = new HBox(15, leftSection, statusLabel);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.getStyleClass().add(ThemeConst.CSS_CARD);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(60);

        if (cm.isResolved()) {
            card.setOpacity(0.5);
        } else if (battlePhase && cm.isDeployed()) {
            card.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && onMissionClick != null) {
                    onMissionClick.accept(cm);
                }
            });
            card.setCursor(javafx.scene.Cursor.HAND);
        } else if (!battlePhase && !cm.isDeployed()) {
            card.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && onMissionClick != null) {
                    onMissionClick.accept(cm);
                }
            });
            card.setCursor(javafx.scene.Cursor.HAND);
        }

        return card;
    }

    private void refreshLog(Campaign campaign) {
        logContainer.getChildren().clear();
        for (CampaignLog.LogEntry entry : campaign.getLog().getEntries()) {
            Label entryLabel = new Label(entry.formatted());
            entryLabel.setStyle(ThemeConst.FONT_XS + " -fx-text-fill: #33cc33;");
            entryLabel.setWrapText(true);
            logContainer.getChildren().add(entryLabel);
        }
    }
}
