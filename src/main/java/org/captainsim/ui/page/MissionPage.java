package org.captainsim.ui.page;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.campaign.CampaignMission;
import org.captainsim.combat.BattleSystem;
import org.captainsim.combat.CombatReport;
import org.captainsim.squad.Squad;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.unit.enemy.Horde;
import org.captainsim.unit.enemy.factions.EnemyChaos;
import org.captainsim.unit.marine.MarineUnit;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MissionPage extends VBox {

    private final CampaignMission mission;
    private final Runnable onMissionEnd;
    private final TextArea logArea;
    private final VBox squadStatusContainer;
    private final Label enemyStatusLabel;
    private final Button actionBtn;

    private Horde horde;
    private List<Squad> participantSquads;
    private int distance;
    private boolean battleComplete;
    private int totalEnemyKills;

    public MissionPage(CampaignMission mission, Runnable onMissionEnd) {
        this.mission = mission;
        this.onMissionEnd = onMissionEnd;

        setSpacing(10);
        setPadding(new Insets(15));
        setFillWidth(true);
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        // ===== Header =====
        Label title = new Label(mission.getName());
        title.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        title.setStyle(ThemeConst.FONT_XL);

        // ===== Center: Split =====
        // Left: Battle Log
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #33ff33; " +
                "-fx-font-family: 'Courier New'; -fx-font-size: 13px;");

        ScrollPane logScroll = new ScrollPane(logArea);
        logScroll.setFitToWidth(true);
        logScroll.setFitToHeight(true);
        logScroll.getStyleClass().add("scroll-pane");

        VBox leftPanel = new VBox(5, new Label("Battle Log"), logScroll);
        leftPanel.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        VBox.setVgrow(logScroll, Priority.ALWAYS);

        // Right: Squad Status
        squadStatusContainer = new VBox(10);
        squadStatusContainer.setPadding(new Insets(10));
        squadStatusContainer.getStyleClass().add(ThemeConst.CSS_CARD);
        squadStatusContainer.setMinWidth(280);

        enemyStatusLabel = new Label("Enemies: --");
        enemyStatusLabel.setStyle(ThemeConst.FONT_M);
        enemyStatusLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        Label rightHeader = new Label("Battlefield Status");
        rightHeader.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        rightHeader.setStyle(ThemeConst.FONT_M);

        VBox rightPanel = new VBox(10, rightHeader, squadStatusContainer, enemyStatusLabel);
        rightPanel.setMinWidth(300);

        HBox centerSplit = new HBox(20, leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        VBox.setVgrow(centerSplit, Priority.ALWAYS);

        // ===== Bottom =====
        actionBtn = new Button("Start Battle");
        actionBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12px 30px; " +
                "-fx-background-color: #0a3a0a; -fx-text-fill: #33ff33; " +
                "-fx-border-color: #33ff33; -fx-border-width: 1;");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setOnAction(e -> {
            if (!battleComplete) {
                startBattle();
            } else {
                if (onMissionEnd != null) onMissionEnd.run();
            }
        });

        // ===== Prepare squads =====
        loadParticipantSquads();

        getChildren().addAll(title, centerSplit, actionBtn);
        VBox.setVgrow(centerSplit, Priority.ALWAYS);
    }

    private void loadParticipantSquads() {
        participantSquads = new ArrayList<>();
        for (String squadId : mission.getAssignedSquadIds()) {
            for (Squad squad : GameData.getInstance().getSquads()) {
                if (squad.getId().equals(squadId)) {
                    participantSquads.add(squad);
                    break;
                }
            }
        }
    }

    private void startBattle() {
        actionBtn.setDisable(true);
        actionBtn.setText("Battle in progress...");

        // Generate enemies
        int enemyStrength = mission.getExpectedDifficulty() * 600;
        horde = EnemyChaos.generateHorde("enemy_force", enemyStrength, mission.getExpectedDifficulty());
        distance = 6 + ThreadLocalRandom.current().nextInt(4);
        totalEnemyKills = 0;

        appendLog("=== Mission: " + mission.getName() + " ===");
        appendLog("Participating squads: " + participantSquads.size());
        appendLog("Enemy force: " + horde.getSize() + " units");
        appendLog("Starting distance: " + distance);
        appendLog("");

        new Thread(() -> {
            try {
                runBattleLoop();
            } catch (Exception e) {
                appendLog("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                battleComplete = true;
                actionBtn.setDisable(false);
                actionBtn.setText("Return to War Council");
                updateStatus();
                mission.complete("Battle completed. " + totalEnemyKills + " enemies killed.");
            });
        }).start();
    }

    private void runBattleLoop() throws Exception {
        int turn = 0;

        while (distance >= 0 && !horde.isEmpty() && hasAliveMarines()) {
            turn++;
            int currentDistance = distance;
            int currentTurn = turn;
            Platform.runLater(() -> {
                appendLog("--- Turn " + currentTurn + " (Distance: " + currentDistance + ") ---");
                updateStatus();
            });

            // Player phase — each squad acts independently
            for (Squad squad : participantSquads) {
                if (!squadHasAlive(squad)) continue;
                if (horde.isEmpty()) break;

                CombatReport report;
                String phaseName;
                if (currentDistance > 0) {
                    report = BattleSystem.squadRangedAttack(squad, horde, currentDistance, false);
                    phaseName = squad.getDisplayName() + " [Ranged]";
                } else {
                    report = BattleSystem.squadMeleeAttack(squad, horde);
                    phaseName = squad.getDisplayName() + " [Melee]";
                }

                logSquadReport(report, phaseName);
                horde.removeDeadUnits();
                Thread.sleep(100);
            }

            // Enemy phase
            if (!horde.isEmpty()) {
                CombatReport enemyReport = BattleSystem.hordeAttacksSquad(horde,
                        createCombinedSquad(), currentDistance);
                logEnemyReport(enemyReport);
            }

            // Update status
            Platform.runLater(this::updateStatus);

            distance--;
            Thread.sleep(200);
        }

        // Result
        Platform.runLater(() -> {
            if (horde.isEmpty()) {
                appendLog("\n>>> VICTORY: All enemies destroyed! <<<");
            } else {
                appendLog("\n>>> DEFEAT: Squads overrun! <<<");
            }
            updateStatus();
        });
    }

    // ==================== Helper Methods ====================

    private boolean hasAliveMarines() {
        return participantSquads.stream()
                .anyMatch(this::squadHasAlive);
    }

    private boolean squadHasAlive(Squad squad) {
        return squad.getAllMarines().stream()
                .anyMatch(m -> m.isAvailable() && m.getCurrentWounds() > 0);
    }

    private int getTotalAliveMarines() {
        return participantSquads.stream()
                .flatMap(s -> s.getAllMarines().stream())
                .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0)
                .toList()
                .size();
    }

    private Squad createCombinedSquad() {
        // Horde attacks need a squad target — use first alive squad
        for (Squad squad : participantSquads) {
            if (squadHasAlive(squad)) return squad;
        }
        return participantSquads.get(0);
    }

    private void updateStatus() {
        squadStatusContainer.getChildren().clear();
        for (Squad squad : participantSquads) {
            int alive = (int) squad.getAllMarines().stream()
                    .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0)
                    .count();
            int total = squad.getSize();

            String color;
            if (alive == 0) color = "#cc0000";
            else if (alive < total / 2) color = "#ffb000";
            else color = "#33ff33";

            Label status = new Label(squad.getDisplayName() + ": " + alive + "/" + total);
            status.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
            squadStatusContainer.getChildren().add(status);
        }
        enemyStatusLabel.setText("Enemies remaining: " + horde.getSize());
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    private void logSquadReport(CombatReport report, String header) {
        Platform.runLater(() -> {
            appendLog("  " + header);
            int totalDmg = 0;
            long totalKills = 0;
            for (var record : report.getAttackRecords()) {
                totalDmg += record.getDamage();
                if (record.isKilled()) {
                    totalKills++;
                    totalEnemyKills++;
                }
            }
            if (totalDmg > 0) {
                appendLog(String.format("    Damage: %d, Kills: %d", totalDmg, totalKills));
            } else {
                appendLog("    No hits");
            }
        });
    }

    private void logEnemyReport(CombatReport report) {
        Platform.runLater(() -> {
            appendLog("  Enemy Phase:");
            for (var record : report.getAttackRecords()) {
                appendLog(String.format("    %s hit %s for %d dmg",
                        record.getAttackerName(), record.getTargetType(),
                        record.getDamage()));
            }
            for (String casualty : report.getMarineCasualties()) {
                appendLog("    [DOWN] " + casualty);
            }
        });
    }
}
