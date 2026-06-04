package org.captainsim.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.captainsim.combat.MovementConstants;
import org.captainsim.combat.SquadTurnState;
import org.captainsim.combat.TurnPhase;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.item.WeaponItem;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * RTS-style info panel showing selected squad data and available actions.
 */
public class SquadInfoPanel extends VBox {

    // ===== Callbacks =====
    private Runnable onHold;
    private Runnable onStandardMove;
    private Runnable onAdvance;
    private Runnable onShoot;
    private Runnable onCharge;
    private Runnable onSkip;

    // ===== UI Elements =====
    private final Label squadNameLabel;
    private final Label statusLabel;
    private final Label positionLabel;
    private final Label marineCountLabel;
    private final ProgressBar healthBar;
    private final Label healthText;

    private final Label wsVal, bsVal, sVal, tVal, agVal, intVal, wpVal, felVal;
    private final Label weaponLabel;

    private final Button holdBtn;
    private final Button moveBtn;
    private final Button advanceBtn;
    private final Button shootBtn;
    private final Button chargeBtn;
    private final Button skipBtn;

    private Squad currentSquad;
    private TurnPhase currentPhase;

    // ===== Action button container =====
    private final VBox actionsBox;

    public SquadInfoPanel() {
        setPrefWidth(340);
        setMinWidth(300);
        setMaxWidth(400);
        setPadding(new Insets(12));
        setSpacing(6);
        setStyle("-fx-background-color: #0a0c0a; -fx-border-color: #1a3a1a; -fx-border-width: 1;");

        // ===== Header =====
        squadNameLabel = new Label("No squad selected");
        squadNameLabel.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 18px; -fx-font-weight: bold;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #44cc44; -fx-font-size: 13px;");

        // ===== Info rows =====
        positionLabel = labelInfo("Pos: --");
        marineCountLabel = labelInfo("Marines: --");

        // Health bar
        healthBar = new ProgressBar(0);
        healthBar.setPrefWidth(Double.MAX_VALUE);
        healthBar.setStyle("-fx-accent: #33aa33; -fx-control-inner-background: #1a1a1a;");
        healthText = new Label("HP: --/--");
        healthText.setStyle("-fx-text-fill: #66cc66; -fx-font-size: 12px;");

        VBox healthBox = new VBox(2, healthBar, healthText);

        // Separator
        HBox sep1 = separator();

        // ===== Stats =====
        Label statsTitle = new Label("STATS");
        statsTitle.setStyle("-fx-text-fill: #aaffaa; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox statRow1 = new HBox(8);
        statRow1.setPadding(new Insets(2, 0, 0, 0));
        wsVal = statCell("WS"); bsVal = statCell("BS"); sVal = statCell("S"); tVal = statCell("T");
        statRow1.getChildren().addAll(wsVal, bsVal, sVal, tVal);

        HBox statRow2 = new HBox(8);
        agVal = statCell("Ag"); intVal = statCell("Int"); wpVal = statCell("WP"); felVal = statCell("Fel");
        statRow2.getChildren().addAll(agVal, intVal, wpVal, felVal);

        VBox statsBox = new VBox(2, statsTitle, statRow1, statRow2);
        statsBox.setPadding(new Insets(4, 0, 4, 0));

        // Separator
        HBox sep2 = separator();

        // ===== Equipment =====
        Label equipTitle = new Label("EQUIPMENT");
        equipTitle.setStyle("-fx-text-fill: #aaffaa; -fx-font-size: 13px; -fx-font-weight: bold;");

        weaponLabel = new Label("No data");
        weaponLabel.setStyle("-fx-text-fill: #88cc88; -fx-font-size: 12px;");
        weaponLabel.setWrapText(true);

        VBox equipBox = new VBox(2, equipTitle, weaponLabel);
        equipBox.setPadding(new Insets(4, 0, 4, 0));

        // Separator
        HBox sep3 = separator();

        // ===== Actions =====
        Label actionsTitle = new Label("ACTIONS");
        actionsTitle.setStyle("-fx-text-fill: #aaffaa; -fx-font-size: 13px; -fx-font-weight: bold;");

        actionsBox = new VBox(6);
        actionsBox.setPadding(new Insets(4, 0, 8, 0));

        holdBtn = actionButton("HOLD POSITION", "#334433");
        moveBtn = actionButton("STANDARD MOVE  [" + MovementConstants.STANDARD_MOVE + "]", "#335533");
        advanceBtn = actionButton("ADVANCE  [" + MovementConstants.ADVANCE_MOVE + "]", "#553333");
        shootBtn = actionButton("SHOOT", "#336633");
        chargeBtn = actionButton("CHARGE", "#664433");
        skipBtn = actionButton("SKIP PHASE", "#333344");

        holdBtn.setOnAction(e -> { if (onHold != null) onHold.run(); });
        moveBtn.setOnAction(e -> { if (onStandardMove != null) onStandardMove.run(); });
        advanceBtn.setOnAction(e -> { if (onAdvance != null) onAdvance.run(); });
        shootBtn.setOnAction(e -> { if (onShoot != null) onShoot.run(); });
        chargeBtn.setOnAction(e -> { if (onCharge != null) onCharge.run(); });
        skipBtn.setOnAction(e -> { if (onSkip != null) onSkip.run(); });

        // Assemble
        getChildren().addAll(
                squadNameLabel, statusLabel,
                positionLabel, marineCountLabel, healthBox,
                sep1, statsBox,
                sep2, equipBox,
                sep3, actionsTitle, actionsBox
        );

        clearSelection();
    }

    // ==================== Public API ====================

    public void displaySquad(Squad squad, TurnPhase phase, SquadTurnState state,
                             int col, int row, int aliveCount, int totalMarines, int totalHp, int maxHp) {
        this.currentSquad = squad;
        this.currentPhase = phase;

        squadNameLabel.setText(squad.getDisplayName());
        statusLabel.setText("Status: [ " + state.getStatusText() + " ]");
        positionLabel.setText("Pos: (" + col + "," + row + ")");
        marineCountLabel.setText("Marines: " + aliveCount + "/" + totalMarines);

        double hpRatio = maxHp > 0 ? (double) totalHp / maxHp : 0;
        healthBar.setProgress(hpRatio);
        healthText.setText("HP: " + totalHp + "/" + maxHp);

        // Stats - average across alive marines
        List<MarineUnit> alive = squad.getAllMarines().stream()
                .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0)
                .collect(Collectors.toList());

        if (!alive.isEmpty()) {
            double avgWs = alive.stream().mapToInt(MarineUnit::getWs).average().orElse(0);
            double avgBs = alive.stream().mapToInt(MarineUnit::getBs).average().orElse(0);
            double avgS  = alive.stream().mapToInt(MarineUnit::getS).average().orElse(0);
            double avgT  = alive.stream().mapToInt(MarineUnit::getT).average().orElse(0);
            double avgAg = alive.stream().mapToInt(MarineUnit::getAg).average().orElse(0);
            double avgInt= alive.stream().mapToInt(MarineUnit::getIntelligence).average().orElse(0);
            double avgWp = alive.stream().mapToInt(MarineUnit::getWp).average().orElse(0);
            double avgFel= alive.stream().mapToInt(MarineUnit::getFel).average().orElse(0);

            setStat(wsVal, (int)avgWs);
            setStat(bsVal, (int)avgBs);
            setStat(sVal, (int)avgS);
            setStat(tVal, (int)avgT);
            setStat(agVal, (int)avgAg);
            setStat(intVal, (int)avgInt);
            setStat(wpVal, (int)avgWp);
            setStat(felVal, (int)avgFel);

            // Equipment - show first marine's weapons as rep
            MarineUnit rep = alive.get(0);
            StringBuilder wsb = new StringBuilder();
            if (rep.getRightHand() != null) {
                WeaponItem w = rep.getRightHand();
                wsb.append("RH: ").append(w.getName());
                if (w.getRange() > 0) wsb.append(" [R:").append(w.getRange()).append(" D:").append(w.getDamage()).append(" AP:").append(w.getArmourPenetration()).append("]");
                else wsb.append(" [Melee D:").append(w.getDamage()).append(" AP:").append(w.getArmourPenetration()).append("]");
                wsb.append("\n");
            }
            if (rep.getLeftHand() != null) {
                WeaponItem w = rep.getLeftHand();
                wsb.append("LH: ").append(w.getName());
                if (w.getRange() > 0) wsb.append(" [R:").append(w.getRange()).append(" D:").append(w.getDamage()).append(" AP:").append(w.getArmourPenetration()).append("]");
                else wsb.append(" [Melee D:").append(w.getDamage()).append(" AP:").append(w.getArmourPenetration()).append("]");
            }
            weaponLabel.setText(wsb.toString().isEmpty() ? "Unarmed" : wsb.toString());
        } else {
            clearStats();
            weaponLabel.setText("All marines down");
        }

        // Update action buttons
        updateActions(state, phase);
        setVisible(true);
    }

    public void displayEnemyInfo(String typeName, int count, int hp, int maxHp, int col, int row) {
        this.currentSquad = null;
        this.currentPhase = null;

        squadNameLabel.setText("👾 " + typeName);
        statusLabel.setText("Status: [ ENEMY CLUSTER ]");
        positionLabel.setText("Pos: (" + col + "," + row + ")");
        marineCountLabel.setText("Units: " + count);
        weaponLabel.setText("Enemy cluster — " + count + " units remaining");

        double hpRatio = maxHp > 0 ? (double) hp / maxHp : 0;
        healthBar.setProgress(hpRatio);
        healthText.setText("HP: " + hp + "/" + maxHp);

        clearStats();
        actionsBox.getChildren().clear();
        setVisible(true);
    }

    public void clearSelection() {
        this.currentSquad = null;
        this.currentPhase = null;
        squadNameLabel.setText("No squad selected");
        statusLabel.setText("");
        positionLabel.setText("Pos: --");
        marineCountLabel.setText("Marines: --");
        healthBar.setProgress(0);
        healthText.setText("HP: --/--");
        clearStats();
        weaponLabel.setText("Select a unit on the map");
        actionsBox.getChildren().clear();
    }

    // ==================== Callbacks ====================

    public void setOnHold(Runnable r) { this.onHold = r; }
    public void setOnStandardMove(Runnable r) { this.onStandardMove = r; }
    public void setOnAdvance(Runnable r) { this.onAdvance = r; }
    public void setOnShoot(Runnable r) { this.onShoot = r; }
    public void setOnCharge(Runnable r) { this.onCharge = r; }
    public void setOnSkip(Runnable r) { this.onSkip = r; }

    public Squad getCurrentSquad() { return currentSquad; }

    // ==================== Internals ====================

    private void updateActions(SquadTurnState state, TurnPhase phase) {
        actionsBox.getChildren().clear();

        switch (phase) {
            case MOVEMENT -> {
                if (!state.hasMoved()) {
                    actionsBox.getChildren().addAll(holdBtn, moveBtn, advanceBtn);
                } else {
                    // Squad already moved this phase, show skip to proceed
                    actionsBox.getChildren().add(skipBtn);
                }
            }
            case SHOOTING -> {
                if (state.canShoot()) {
                    actionsBox.getChildren().add(shootBtn);
                }
                actionsBox.getChildren().add(skipBtn);
            }
            case CHARGE -> {
                if (state.canCharge()) {
                    actionsBox.getChildren().add(chargeBtn);
                }
                actionsBox.getChildren().add(skipBtn);
            }
            case ENEMY_MOVEMENT, ENEMY_SHOOTING, ENEMY_CHARGE -> {
                Label wait = new Label("Enemy turn in progress...");
                wait.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
                actionsBox.getChildren().add(wait);
            }
            default -> {}
        }
    }

    private Button actionButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: #ccffcc; " +
                        "-fx-border-color: #446644; " +
                        "-fx-border-width: 1; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6px 12px; " +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + bgColor.replace("33", "55") + "; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #66aa66; " +
                        "-fx-border-width: 1; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6px 12px; " +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> {
            String orig = "-fx-background-color: " + bgColor + "; " +
                    "-fx-text-fill: #ccffcc; " +
                    "-fx-border-color: #446644; " +
                    "-fx-border-width: 1; " +
                    "-fx-font-size: 13px; " +
                    "-fx-padding: 6px 12px; " +
                    "-fx-cursor: hand;";
            btn.setStyle(orig);
        });
        return btn;
    }

    private Label statCell(String name) {
        Label l = new Label(name + ": --");
        l.setStyle("-fx-text-fill: #aaddaa; -fx-font-size: 12px; -fx-font-family: 'Monospaced';");
        l.setMinWidth(60);
        return l;
    }

    private void setStat(Label label, int value) {
        String prefix = label.getText().substring(0, 3); // "WS:", "BS:", etc.
        label.setText(prefix + " " + value);
        if (value >= 50) label.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 12px; -fx-font-family: 'Monospaced';");
        else if (value >= 35) label.setStyle("-fx-text-fill: #aaddaa; -fx-font-size: 12px; -fx-font-family: 'Monospaced';");
        else label.setStyle("-fx-text-fill: #aa8866; -fx-font-size: 12px; -fx-font-family: 'Monospaced';");
    }

    private void clearStats() {
        String gray = "-fx-text-fill: #555555; -fx-font-size: 12px; -fx-font-family: 'Monospaced';";
        wsVal.setStyle(gray); bsVal.setStyle(gray); sVal.setStyle(gray); tVal.setStyle(gray);
        agVal.setStyle(gray); intVal.setStyle(gray); wpVal.setStyle(gray); felVal.setStyle(gray);
    }

    private Label labelInfo(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #88bb88; -fx-font-size: 12px;");
        return l;
    }

    private HBox separator() {
        HBox sep = new HBox();
        sep.setPrefHeight(1);
        sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #1a3a1a;");
        sep.setPadding(new Insets(2, 0, 2, 0));
        return sep;
    }
}
