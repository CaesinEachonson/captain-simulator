package org.captainsim.ui.page;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import org.captainsim.GameData;
import org.captainsim.campaign.CampaignMission;
import org.captainsim.combat.*;
import org.captainsim.combat.SquadTurnState;
import org.captainsim.squad.Squad;
import org.captainsim.ui.component.BattleMap;
import org.captainsim.ui.component.BattleMapData;
import org.captainsim.ui.component.SquadInfoPanel;
import org.captainsim.ui.consts.ThemeConst;
import org.captainsim.unit.enemy.Horde;
import org.captainsim.unit.enemy.factions.EnemyChaos;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.item.WeaponItem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.captainsim.combat.MovementConstants.STANDARD_MOVE;

public class MissionPage extends VBox {

    private final CampaignMission mission;
    private final Runnable onMissionEnd;
    private final TextArea logArea;
    private final Button endTurnBtn;
    private final Button skipPhaseBtn;
    private final BattleMap battleMap;
    private final BattleMapData mapData;
    private final SquadInfoPanel infoPanel;
    private final Label phaseLabel;
    private final Label turnLabel;
    private Label enemyCountLabel;

    // ===== Multi-cluster enemies =====
    private List<Horde> enemyClusters;
    private static final String[] CLUSTER_NAMES = {"Host α", "Host β", "Host γ", "Host δ", "Host ε"};

    private List<Squad> participantSquads;
    private int totalEnemyKills;
    private int turnNumber;
    private boolean battleComplete;

    // ===== Manual Control State =====
    private TurnPhase currentPhase;
    private final Map<String, SquadMapEntry> squadEntries;

    // ===== Movement targeting mode =====
    private boolean moveTargetingMode;
    private int moveTargetRange;

    private static class SquadMapEntry {
        final Squad squad;
        int col;
        int row;
        SquadTurnState turnState;

        SquadMapEntry(Squad squad, int col, int row) {
            this.squad = squad;
            this.col = col;
            this.row = row;
            this.turnState = new SquadTurnState();
        }
    }

    public MissionPage(CampaignMission mission, Runnable onMissionEnd) {
        this.mission = mission;
        this.onMissionEnd = onMissionEnd;
        this.squadEntries = new LinkedHashMap<>();
        this.turnNumber = 0;

        setSpacing(8);
        setPadding(new Insets(12));
        setFillWidth(true);
        getStyleClass().add(ThemeConst.CSS_BG_DARK);

        // ===== Header =====
        Label title = new Label(mission.getName());
        title.getStyleClass().add(ThemeConst.CSS_PAGE_HEADER);
        title.setStyle(ThemeConst.FONT_XL);

        phaseLabel = new Label("READY");
        phaseLabel.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 16px; -fx-font-weight: bold;");

        turnLabel = new Label("Turn: --");
        turnLabel.setStyle(ThemeConst.FONT_M);
        turnLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        HBox headerRow = new HBox(20, title, phaseLabel, turnLabel);
        headerRow.setPadding(new Insets(0, 0, 5, 0));

        // ===== Center: Map Left | Info Panel Right =====
        mapData = new BattleMapData();
        mapData.generateTerrain(new Random().nextLong());

        battleMap = new BattleMap(mapData);
        battleMap.draw();

        ScrollPane mapScroll = new ScrollPane(battleMap);
        mapScroll.setPannable(true);
        mapScroll.setPrefSize(700, 500);
        mapScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(mapScroll, Priority.ALWAYS);

        mapData.setOnClick(info -> handleMapClick(info));

        VBox mapPanel = new VBox(5, new Label("BATTLEFIELD"), mapScroll);
        mapPanel.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        HBox.setHgrow(mapPanel, Priority.ALWAYS);

        // Info Panel (Right)
        infoPanel = new SquadInfoPanel();
        infoPanel.setVisible(false);
        setupInfoPanelCallbacks();

        VBox rightPanel = new VBox(8, infoPanel, createEnemyStatusBox());
        rightPanel.setMinWidth(300);
        rightPanel.setMaxWidth(350);

        HBox centerSplit = new HBox(15, mapPanel, rightPanel);
        HBox.setHgrow(mapPanel, Priority.ALWAYS);
        VBox.setVgrow(centerSplit, Priority.ALWAYS);

        // ===== Bottom =====
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(100);
        logArea.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #33ff33; " +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        skipPhaseBtn = new Button("SKIP PHASE");
        skipPhaseBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 20px; " +
                "-fx-background-color: #333322; -fx-text-fill: #aaaa44; " +
                "-fx-border-color: #666633; -fx-border-width: 1;");
        skipPhaseBtn.setMaxWidth(Double.MAX_VALUE);
        skipPhaseBtn.setOnAction(e -> {
            if (currentPhase != null && currentPhase.isPlayerPhase()) {
                advancePhase();
            }
        });
        skipPhaseBtn.setVisible(false);

        endTurnBtn = new Button("DEPLOY SQUADS & START");
        endTurnBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10px 25px; " +
                "-fx-background-color: #0a3a0a; -fx-text-fill: #33ff33; " +
                "-fx-border-color: #33ff33; -fx-border-width: 1;");
        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setOnAction(e -> {
            if (!battleComplete) {
                startBattle();
            } else {
                if (onMissionEnd != null) onMissionEnd.run();
            }
        });

        HBox bottomButtons = new HBox(10, endTurnBtn, skipPhaseBtn);
        HBox.setHgrow(endTurnBtn, Priority.ALWAYS);
        HBox.setHgrow(skipPhaseBtn, Priority.ALWAYS);

        VBox bottomBox = new VBox(5, logArea, bottomButtons);

        getChildren().addAll(headerRow, centerSplit, bottomBox);
        VBox.setVgrow(centerSplit, Priority.ALWAYS);
    }

    // ==================== Initialization ====================

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

    private void deployUnitsOnMap() {
        mapData.clearUnits();
        squadEntries.clear();
        Random rand = new Random();

        // Marines: left deploy zone
        int marineIndex = 0;
        for (Squad squad : participantSquads) {
            String label = squad.getId().length() > 5 ? squad.getId().substring(0, 5) : squad.getId();
            int maxHp = squad.getAllMarines().stream().mapToInt(MarineUnit::getWounds).sum();
            int row = 5 + marineIndex * 7;
            if (row >= BattleMapData.ROWS - 2) row = 5 + rand.nextInt(BattleMapData.ROWS - 10);
            int alive = countAlive(squad);

            mapData.addUnit(new BattleMapData.UnitMarker(
                    squad.getId(), label, 1, row, true, calcSquadHp(squad), maxHp, alive));

            squadEntries.put(squad.getId(), new SquadMapEntry(squad, 1, row));
            marineIndex++;
        }

        // Enemy clusters: each cluster = one marker
        int eRow = 5;
        for (int i = 0; i < enemyClusters.size(); i++) {
            Horde cluster = enemyClusters.get(i);
            String id = "cluster_" + i;
            String label = CLUSTER_NAMES[i % CLUSTER_NAMES.length];
            int clusterHp = 0;
            int clusterUnits = 0;
            for (var unit : cluster.getUnits()) {
                if (unit.getCurrentWounds() > 0) {
                    clusterUnits++;
                    clusterHp += unit.getCurrentWounds();
                }
            }
            int col = Math.max(25, 28 - i * 2);
            mapData.addUnit(new BattleMapData.UnitMarker(
                    id, label, col, eRow, false,
                    clusterHp, clusterHp, clusterUnits));
            eRow += 6;
            if (eRow >= BattleMapData.ROWS - 3) { eRow = 5; }
        }

        battleMap.draw();
    }

    private void startBattle() {
        loadParticipantSquads();

        int totalStrength = mission.getExpectedDifficulty() * 600;
        int numClusters = 3 + ThreadLocalRandom.current().nextInt(3); // 3~5
        enemyClusters = new ArrayList<>();

        int baseStrength = totalStrength / numClusters;
        for (int i = 0; i < numClusters; i++) {
            int str = baseStrength + ThreadLocalRandom.current().nextInt(-100, 101);
            str = Math.max(200, str);
            Horde cluster = EnemyChaos.generateHorde(
                    CLUSTER_NAMES[i % CLUSTER_NAMES.length],
                    str, mission.getExpectedDifficulty());
            enemyClusters.add(cluster);
        }

        totalEnemyKills = 0;
        turnNumber = 0;

        deployUnitsOnMap();

        int totalSize = enemyClusters.stream().mapToInt(Horde::getSize).sum();
        appendLog("=== Mission: " + mission.getName() + " ===");
        appendLog("Participating squads: " + participantSquads.size());
        appendLog("Enemy clusters: " + numClusters + " (" + totalSize + " total units)");
        appendLog("");

        endTurnBtn.setVisible(true);
        skipPhaseBtn.setVisible(true);
        startNewPlayerTurn();
    }

    // ==================== Turn Management ====================

    private void startNewPlayerTurn() {
        turnNumber++;
        currentPhase = TurnPhase.MOVEMENT;
        moveTargetingMode = false;

        for (SquadMapEntry entry : squadEntries.values()) {
            entry.turnState = new SquadTurnState();
        }

        int totalRemaining = enemyClusters.stream().mapToInt(Horde::getSize).sum();

        Platform.runLater(() -> {
            turnLabel.setText("Turn: " + turnNumber);
            updatePhaseUI();
            enemyCountLabel.setText("Remaining: " + totalRemaining);
            battleMap.draw();
            appendLog("\n=== Turn " + turnNumber + " ===");
            appendLog("▶ Player " + currentPhase.getDisplayName());
            infoPanel.clearSelection();
            mapData.clearSelection();
            checkPhaseEnd();
        });
    }

    private void advancePhase() {
        if (currentPhase == null || currentPhase == TurnPhase.COMPLETE) return;

        moveTargetingMode = false;

        // Auto-hold squads that haven't moved in MOVEMENT phase
        if (currentPhase == TurnPhase.MOVEMENT) {
            for (SquadMapEntry entry : squadEntries.values()) {
                if (squadHasAlive(entry.squad) && !entry.turnState.hasMoved()) {
                    entry.turnState.doHold();
                }
            }
        }

        currentPhase = currentPhase.next();
        mapData.clearSelection();
        infoPanel.clearSelection();
        battleMap.clearHighlights();
        mapData.setMoveRange(0);
        mapData.setShowRange(false, 0);
        mapData.setShowChargeRange(false);
        battleMap.draw();

        if (currentPhase.isEnemyPhase()) {
            Platform.runLater(() -> {
                updatePhaseUI();
                endTurnBtn.setDisable(true);
                endTurnBtn.setText("Enemy turn...");
                skipPhaseBtn.setVisible(false);
                infoPanel.setVisible(false);
            });
            runEnemySubPhase();
        } else if (currentPhase.isPlayerPhase()) {
            if (currentPhase == TurnPhase.MOVEMENT) {
                Platform.runLater(this::startNewPlayerTurn);
            } else {
                Platform.runLater(() -> {
                    updatePhaseUI();
                    appendLog("▶ Player " + currentPhase.getDisplayName());
                    checkPhaseEnd();
                });
            }
        }
    }

    private void updatePhaseUI() {
        if (currentPhase.isPlayerPhase()) {
            phaseLabel.setText("▶ " + currentPhase.getDisplayName());
            phaseLabel.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else if (currentPhase.isEnemyPhase()) {
            phaseLabel.setText("▶ " + currentPhase.getDisplayName());
            phaseLabel.setStyle("-fx-text-fill: #ff8844; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }

    private void checkPhaseEnd() {
        boolean allDone = true;
        for (SquadMapEntry entry : squadEntries.values()) {
            if (!squadHasAlive(entry.squad)) continue;
            SquadTurnState st = entry.turnState;
            switch (currentPhase) {
                case MOVEMENT -> { if (!st.hasMoved()) allDone = false; }
                case SHOOTING -> { if (st.canShoot()) allDone = false; }
                case CHARGE   -> { if (st.canCharge()) allDone = false; }
                default -> {}
            }
        }

        if (allDone) {
            endTurnBtn.setDisable(false);
            endTurnBtn.setText("END PHASE");
            endTurnBtn.setOnAction(e -> advancePhase());
            skipPhaseBtn.setVisible(true);
            skipPhaseBtn.setText("SKIP PHASE");
        } else {
            endTurnBtn.setDisable(true);
            endTurnBtn.setText("Select a squad to act");
            skipPhaseBtn.setVisible(true);
            skipPhaseBtn.setText("SKIP PHASE");
        }
    }

    // ==================== Map Click Handling ====================

    private void handleMapClick(BattleMapData.CellInfo info) {
        if (currentPhase == null || currentPhase.isEnemyPhase() || currentPhase == TurnPhase.COMPLETE) {
            return;
        }

        // Movement targeting mode
        if (moveTargetingMode) {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;

            int targetCol = info.col();
            int targetRow = info.row();
            int dist = BattleMapData.manhattanDistance(entry.col, entry.row, targetCol, targetRow);

            if (dist <= moveTargetRange) {
                entry.col = targetCol;
                entry.row = targetRow;
                mapData.moveUnit(entry.squad.getId(), targetCol, targetRow);

                if (moveTargetRange == STANDARD_MOVE) {
                    entry.turnState.doStandardMove();
                    appendLog(entry.squad.getDisplayName() + " moves to (" + targetCol + "," + targetRow + ")");
                } else {
                    entry.turnState.doAdvance();
                    appendLog(entry.squad.getDisplayName() + " ADVANCES to (" + targetCol + "," + targetRow + ") (no shoot/charge)");
                }

                moveTargetingMode = false;
                mapData.clearSelection();
                infoPanel.clearSelection();
                battleMap.clearHighlights();
                mapData.setMoveRange(0);
                battleMap.draw();
                afterAction(entry);
            } else {
                appendLog("[!] " + entry.squad.getDisplayName() + " — target too far (" + dist + " > " + moveTargetRange + ")");
            }
            return;
        }

        // Normal click
        BattleMapData.UnitMarker clicked = info.unit();
        if (clicked == null) return;

        if (clicked.isMarine()) {
            SquadMapEntry entry = squadEntries.get(clicked.id());
            if (entry == null) return;
            if (!canActInPhase(entry)) {
                appendLog("[!] " + entry.squad.getDisplayName() + " has already acted this phase");
                return;
            }
            selectSquad(entry);
        } else if (clicked.id().startsWith("cluster_")) {
            // Clicked enemy = try to shoot/charge, or just show info
            handleEnemyClick(clicked);
        }
    }

    private void selectSquad(SquadMapEntry entry) {
        mapData.selectUnit(entry.squad.getId());
        mapData.setShowRange(false, 0);
        mapData.setShowChargeRange(false);
        mapData.setMoveRange(0);
        battleMap.clearHighlights();
        battleMap.draw();

        SquadTurnState st = entry.turnState;
        int alive = countAlive(entry.squad);
        int total = entry.squad.getAllMarines().size();
        int hp = calcSquadHp(entry.squad);
        int maxHp = entry.squad.getAllMarines().stream().mapToInt(MarineUnit::getWounds).sum();

        infoPanel.displaySquad(entry.squad, currentPhase, st, entry.col, entry.row,
                alive, total, hp, maxHp);
        infoPanel.setVisible(true);

        if (currentPhase == TurnPhase.SHOOTING && st.canShoot()) {
            showWeaponRange(entry);
        } else if (currentPhase == TurnPhase.CHARGE && st.canCharge()) {
            showChargeRange(entry);
        }
    }

    private void handleEnemyClick(BattleMapData.UnitMarker enemy) {
        int clusterIndex = parseClusterIndex(enemy.id());
        if (clusterIndex < 0 || clusterIndex >= enemyClusters.size()) return;
        Horde cluster = enemyClusters.get(clusterIndex);

        SquadMapEntry selectedEntry = getSelectedSquadEntry();

        if (selectedEntry != null) {
            SquadTurnState st = selectedEntry.turnState;

            if (currentPhase == TurnPhase.SHOOTING && st.canShoot()) {
                int dist = mapData.distanceToEnemy(enemy.id());
                int weaponRange = getBestWeaponRange(selectedEntry.squad);
                if (weaponRange > 0 && dist <= weaponRange) {
                    executeShoot(selectedEntry, cluster, clusterIndex);
                } else {
                    appendLog("[!] " + selectedEntry.squad.getDisplayName()
                            + " — target out of range (" + dist + " > " + weaponRange + ")");
                }
                return;
            } else if (currentPhase == TurnPhase.CHARGE && st.canCharge()) {
                int dist = mapData.distanceToEnemy(enemy.id());
                if (dist <= MovementConstants.CHARGE_RANGE) {
                    executeCharge(selectedEntry, cluster, enemy, clusterIndex);
                } else {
                    appendLog("[!] " + selectedEntry.squad.getDisplayName()
                            + " — target too far to charge");
                }
                return;
            }
        }

        // No combat action: just show enemy info
        showEnemyInfo(enemy, cluster);
    }

    private int parseClusterIndex(String markerId) {
        try {
            return Integer.parseInt(markerId.replace("cluster_", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int getTotalEnemyCount() {
        return enemyClusters.stream().mapToInt(h -> {
            int count = 0;
            for (var u : h.getUnits()) {
                if (u.getCurrentWounds() > 0) count++;
            }
            return count;
        }).sum();
    }

    // ==================== Squad Actions ====================

    private void setupInfoPanelCallbacks() {
        infoPanel.setOnHold(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;
            entry.turnState.doHold();
            appendLog(entry.squad.getDisplayName() + " holds position");
            afterAction(entry);
        });

        infoPanel.setOnStandardMove(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;
            moveTargetRange = STANDARD_MOVE;
            enterMoveTargetingMode(entry, moveTargetRange);
        });

        infoPanel.setOnAdvance(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;
            moveTargetRange = MovementConstants.ADVANCE_MOVE;
            enterMoveTargetingMode(entry, moveTargetRange);
        });

        infoPanel.setOnShoot(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;
            showWeaponRange(entry);
            appendLog("[!] Click on an enemy marker to shoot");
        });

        infoPanel.setOnCharge(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) return;
            showChargeRange(entry);
            appendLog("[!] Click on a nearby enemy to charge");
        });

        infoPanel.setOnSkip(() -> {
            SquadMapEntry entry = getSelectedSquadEntry();
            if (entry == null) {
                advancePhase();
                return;
            }
            if (currentPhase == TurnPhase.MOVEMENT && !entry.turnState.hasMoved()) {
                entry.turnState.doHold();
                appendLog(entry.squad.getDisplayName() + " holds (skipped)");
            }
            afterAction(entry);
        });
    }

    private void enterMoveTargetingMode(SquadMapEntry entry, int range) {
        moveTargetingMode = true;
        mapData.selectUnit(entry.squad.getId());
        mapData.setMoveRange(range);
        battleMap.draw();
        appendLog("[MOVE] Click a cell on the map (" + range + " cell range)");
        infoPanel.setVisible(false);
    }

    private void afterAction(SquadMapEntry entry) {
        mapData.clearSelection();
        infoPanel.clearSelection();
        battleMap.clearHighlights();
        mapData.setMoveRange(0);
        mapData.setShowRange(false, 0);
        mapData.setShowChargeRange(false);
        battleMap.draw();

        enemyCountLabel.setText("Remaining: " + getTotalEnemyCount());
        checkAutoMelee();
        checkPhaseEnd();

        if (endTurnBtn.isDisable()) {
            infoPanel.setVisible(false);
        }
    }

    // ==================== Combat Execution ====================

    private void executeShoot(SquadMapEntry entry, Horde cluster, int clusterIndex) {
        entry.turnState.doShoot();
        int dist = mapData.nearestEnemyDistance(entry.col, entry.row);
        CombatReport report = BattleSystem.squadRangedAttack(entry.squad, cluster, dist, false);
        logSquadReport(report, entry.squad.getDisplayName() + " [SHOOT " + CLUSTER_NAMES[clusterIndex % CLUSTER_NAMES.length] + "]");
        updateClusterMarker(clusterIndex);
        battleMap.draw();

        if (allClustersEmpty()) { battleVictory(); return; }
        afterAction(entry);
    }

    private void executeCharge(SquadMapEntry entry, Horde cluster,
                               BattleMapData.UnitMarker enemy, int clusterIndex) {
        entry.turnState.doCharge();

        int chargeCol = Math.min(enemy.col() + 1, BattleMapData.COLS - 1);
        entry.col = chargeCol;
        mapData.moveUnit(entry.squad.getId(), chargeCol, entry.row);
        appendLog(entry.squad.getDisplayName() + " CHARGES into " + enemy.label() + "!");

        CombatReport report = BattleSystem.squadMeleeAttack(entry.squad, cluster);
        logSquadReport(report, entry.squad.getDisplayName() + " [CHARGE MELEE]");

        CombatReport jumpReport = BattleSystem.squadJumpPackAssault(entry.squad, cluster);
        if (!jumpReport.getAttackRecords().isEmpty()) {
            logSquadReport(jumpReport, entry.squad.getDisplayName() + " [HAMMER OF WRATH]");
        }

        updateClusterMarker(clusterIndex);
        battleMap.draw();

        if (allClustersEmpty()) { battleVictory(); return; }
        afterAction(entry);
    }

    // ==================== Enemy Turn (3 Sub-Phases) ====================

    private void runEnemySubPhase() {
        new Thread(() -> {
            try {
                TurnPhase phase = currentPhase;
                switch (phase) {
                    case ENEMY_MOVEMENT -> runEnemyMovement();
                    case ENEMY_SHOOTING -> runEnemyShooting();
                    case ENEMY_CHARGE   -> runEnemyCharge();
                    default -> {}
                }
                Thread.sleep(400);
                Platform.runLater(() -> advancePhase());
            } catch (Exception e) {
                appendLog("ERROR in enemy phase: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void runEnemyMovement() {
        // Each cluster advances independently
        for (int i = 0; i < enemyClusters.size(); i++) {
            mapData.advanceEnemiesForCluster("cluster_" + i, 2);
        }
        Platform.runLater(() -> {
            battleMap.draw();
            appendLog("Enemy forces advance");
        });
        checkAutoMelee();
    }

    private void runEnemyShooting() {
        Platform.runLater(() -> appendLog("--- Enemy Shooting ---"));

        for (int ci = 0; ci < enemyClusters.size(); ci++) {
            Horde cluster = enemyClusters.get(ci);
            if (cluster.isEmpty()) continue;

            for (SquadMapEntry entry : squadEntries.values()) {
                if (!squadHasAlive(entry.squad)) continue;
                int dist = mapData.nearestEnemyDistance(entry.col, entry.row);
                if (dist > 10 || dist <= 1) continue;

                CombatReport report = BattleSystem.hordeAttacksSquad(cluster, entry.squad, dist);
                logEnemyReport(report, entry.squad.getDisplayName());

                for (String casualty : report.getMarineCasualties()) {
                    updateSquadMarkerHp(entry.squad.getId());
                }

                if (!squadHasAlive(entry.squad)) {
                    appendLog("[!] " + entry.squad.getDisplayName() + " has been wiped out!");
                    mapData.removeDeadUnit(entry.squad.getId());
                }
            }
        }

        Platform.runLater(() -> {
            battleMap.draw();
            enemyCountLabel.setText("Remaining: " + getTotalEnemyCount());
        });
        checkDefeat();
    }

    private void runEnemyCharge() {
        Platform.runLater(() -> appendLog("--- Enemy Charge ---"));

        for (int ci = 0; ci < enemyClusters.size(); ci++) {
            Horde cluster = enemyClusters.get(ci);
            if (cluster.isEmpty()) continue;

            for (SquadMapEntry entry : squadEntries.values()) {
                if (!squadHasAlive(entry.squad)) continue;
                int nearestEnemyDist = mapData.nearestEnemyDistance(entry.col, entry.row);

                if (nearestEnemyDist <= MovementConstants.CHARGE_RANGE) {
                    appendLog("Enemy charges " + entry.squad.getDisplayName() + "!");

                    CombatReport report = BattleSystem.hordeAttacksSquad(cluster, entry.squad, 0);
                    logEnemyReport(report, entry.squad.getDisplayName() + " [MELEE]");

                    for (String casualty : report.getMarineCasualties()) {
                        updateSquadMarkerHp(entry.squad.getId());
                    }

                    if (!squadHasAlive(entry.squad)) {
                        mapData.removeDeadUnit(entry.squad.getId());
                    }
                }
            }
        }

        Platform.runLater(() -> {
            battleMap.draw();
            enemyCountLabel.setText("Remaining: " + getTotalEnemyCount());
        });
        checkDefeat();
    }

    private void checkDefeat() {
        boolean anyAlive = squadEntries.values().stream()
                .anyMatch(e -> squadHasAlive(e.squad));
        if (!anyAlive) {
            Platform.runLater(this::battleDefeat);
        }
    }

    // ==================== Victory / Defeat ====================

    private void battleVictory() {
        battleComplete = true;
        Platform.runLater(() -> {
            appendLog("\n>>> VICTORY: All enemies destroyed! <<<");
            phaseLabel.setText("VICTORY");
            phaseLabel.setStyle("-fx-text-fill: #44ff44; -fx-font-size: 18px; -fx-font-weight: bold;");
            endTurnBtn.setDisable(false);
            endTurnBtn.setText("Return to War Council");
            endTurnBtn.setOnAction(e -> { if (onMissionEnd != null) onMissionEnd.run(); });
            skipPhaseBtn.setVisible(false);
            mission.complete("Victory! " + totalEnemyKills + " enemies killed.");
        });
    }

    private void battleDefeat() {
        battleComplete = true;
        Platform.runLater(() -> {
            appendLog("\n>>> DEFEAT: All marines lost! <<<");
            phaseLabel.setText("DEFEAT");
            phaseLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 18px; -fx-font-weight: bold;");
            endTurnBtn.setDisable(false);
            endTurnBtn.setText("Return to War Council");
            endTurnBtn.setOnAction(e -> { if (onMissionEnd != null) onMissionEnd.run(); });
            skipPhaseBtn.setVisible(false);
            mission.complete("Defeat. All squads lost.");
        });
    }

    // ==================== Helpers ====================

    private SquadMapEntry getSelectedSquadEntry() {
        String id = mapData.getSelectedUnitId();
        return id != null ? squadEntries.get(id) : null;
    }

    private boolean canActInPhase(SquadMapEntry entry) {
        if (!squadHasAlive(entry.squad)) return false;
        SquadTurnState st = entry.turnState;
        return switch (currentPhase) {
            case MOVEMENT -> !st.hasMoved();
            case SHOOTING -> st.canShoot();
            case CHARGE -> st.canCharge();
            default -> false;
        };
    }

    private boolean squadHasAlive(Squad squad) {
        return squad.getAllMarines().stream()
                .anyMatch(m -> m.isAvailable() && m.getCurrentWounds() > 0);
    }

    private int countAlive(Squad squad) {
        return (int) squad.getAllMarines().stream()
                .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0).count();
    }

    private int calcSquadHp(Squad squad) {
        return squad.getAllMarines().stream()
                .filter(m -> m.isAvailable())
                .mapToInt(MarineUnit::getCurrentWounds)
                .sum();
    }

    private int getBestWeaponRange(Squad squad) {
        return squad.getAllMarines().stream()
                .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0)
                .mapToInt(m -> {
                    int maxR = 0;
                    if (m.getRightHand() != null && !isMeleeOnly(m.getRightHand()))
                        maxR = Math.max(maxR, m.getRightHand().getRange());
                    if (m.getLeftHand() != null && !isMeleeOnly(m.getLeftHand()))
                        maxR = Math.max(maxR, m.getLeftHand().getRange());
                    return maxR;
                })
                .max().orElse(0);
    }

    private boolean isMeleeOnly(WeaponItem w) {
        return w.getTraits().contains("balanced") || w.getTraits().contains("brutal")
                || w.getTraits().contains("finesse") || w.getTraits().contains("banner");
    }

    private void showWeaponRange(SquadMapEntry entry) {
        int range = getBestWeaponRange(entry.squad);
        if (range > 0) {
            mapData.setShowRange(true, range);
            List<BattleMapData.UnitMarker> inRange = mapData.findEnemiesInRange(entry.col, entry.row, range);
            Set<String> targets = inRange.stream().map(BattleMapData.UnitMarker::id).collect(Collectors.toSet());
            battleMap.setHighlightTargets(targets);
        } else {
            appendLog("[!] " + entry.squad.getDisplayName() + " has no ranged weapons!");
        }
        battleMap.draw();
    }

    private void showChargeRange(SquadMapEntry entry) {
        mapData.setShowChargeRange(true);
        List<BattleMapData.UnitMarker> inRange = mapData.findEnemiesInChargeRange(entry.col, entry.row);
        Set<String> targets = inRange.stream().map(BattleMapData.UnitMarker::id).collect(Collectors.toSet());
        battleMap.setHighlightTargets(targets);
        if (targets.isEmpty()) {
            appendLog("[!] No enemies within charge range (" + MovementConstants.CHARGE_RANGE + " cells)");
        }
        battleMap.draw();
    }

    private void checkAutoMelee() {
        for (SquadMapEntry entry : squadEntries.values()) {
            if (!squadHasAlive(entry.squad)) continue;
            int nearestEnemyDist = mapData.nearestEnemyDistance(entry.col, entry.row);
            if (nearestEnemyDist <= 1) {
                appendLog("[!] " + entry.squad.getDisplayName() + " is in melee contact!");
            }
        }
    }

    /** Update a single cluster's map marker from horde state */
    private void updateClusterMarker(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= enemyClusters.size()) return;
        Horde cluster = enemyClusters.get(clusterIndex);
        String id = "cluster_" + clusterIndex;

        int aliveUnits = 0;
        int totalHp = 0;
        for (var unit : cluster.getUnits()) {
            if (unit.getCurrentWounds() > 0) {
                aliveUnits++;
                totalHp += unit.getCurrentWounds();
            }
        }

        if (aliveUnits == 0) {
            mapData.removeDeadUnit(id);
        } else {
            mapData.updateUnitHp(id, totalHp);
            // updateUnitHp preserves unitCount, but we need the latest count
            // so we manually recreate the marker with updated unitCount
            BattleMapData.UnitMarker old = mapData.getUnits().stream()
                    .filter(u -> u.id().equals(id)).findFirst().orElse(null);
            if (old != null) {
                // Remove and re-add with updated unitCount
                mapData.removeDeadUnit(id);
                mapData.addUnit(new BattleMapData.UnitMarker(
                        id, old.label(), old.col(), old.row(), false,
                        totalHp, totalHp, aliveUnits));
            }
        }

        cluster.removeDeadUnits();
    }

    /** Update all cluster markers */
    private void updateAllClusterMarkers() {
        for (int i = 0; i < enemyClusters.size(); i++) {
            updateClusterMarker(i);
        }
        battleMap.draw();
    }

    private boolean allClustersEmpty() {
        return enemyClusters.stream().allMatch(h -> {
            for (var u : h.getUnits()) {
                if (u.getCurrentWounds() > 0) return false;
            }
            return true;
        });
    }

    /** Show enemy info in the info panel */
    private void showEnemyInfo(BattleMapData.UnitMarker marker, Horde cluster) {
        mapData.selectUnit(marker.id());
        battleMap.draw();

        int aliveUnits = 0;
        int totalHp = 0;
        for (var unit : cluster.getUnits()) {
            if (unit.getCurrentWounds() > 0) {
                aliveUnits++;
                totalHp += unit.getCurrentWounds();
            }
        }

        infoPanel.displayEnemyInfo(
                marker.label(),
                aliveUnits,
                totalHp,
                totalHp,
                marker.col(),
                marker.row()
        );
    }

    private void updateSquadMarkerHp(String squadId) {
        SquadMapEntry entry = squadEntries.get(squadId);
        if (entry == null) return;
        int hp = calcSquadHp(entry.squad);
        mapData.updateUnitHp(squadId, hp);
    }

    // ==================== Enemy Status Box ====================

    private VBox createEnemyStatusBox() {
        enemyCountLabel = new Label("Enemies: --");
        enemyCountLabel.setStyle(ThemeConst.FONT_M);
        enemyCountLabel.getStyleClass().add(ThemeConst.CSS_TEXT_MUTED);

        VBox box = new VBox(5, new Label("ENEMY STATUS"), enemyCountLabel);
        box.getStyleClass().add(ThemeConst.CSS_TEXT_GOLD);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #1a2a1a; -fx-border-width: 1;");
        return box;
    }

    // ==================== Logging ====================

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
                if (record.isKilled()) { totalKills++; totalEnemyKills++; }
            }
            appendLog(totalDmg > 0
                    ? String.format("    Damage: %d, Kills: %d", totalDmg, totalKills)
                    : "    No hits");
        });
    }

    private void logEnemyReport(CombatReport report, String squadName) {
        Platform.runLater(() -> {
            appendLog("  Enemy attack on " + squadName + ":");
            for (var record : report.getAttackRecords()) {
                appendLog(String.format("    %s hit %s for %d dmg",
                        record.getAttackerName(), record.getTargetType(), record.getDamage()));
            }
            for (String casualty : report.getMarineCasualties()) {
                appendLog("    [DOWN] " + casualty);
            }
        });
    }
}
