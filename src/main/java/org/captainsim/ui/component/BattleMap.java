package org.captainsim.ui.component;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashSet;
import java.util.Set;

public class BattleMap extends Canvas {

    private final BattleMapData data;
    private int hoverCol = -1, hoverRow = -1;

    /** Units that should be rendered with a "this can be targeted" highlight */
    private final Set<String> highlightTargets = new HashSet<>();

    public BattleMap(BattleMapData data) {
        super(BattleMapData.COLS * BattleMapData.CELL_SIZE,
                BattleMapData.ROWS * BattleMapData.CELL_SIZE);
        this.data = data;

        setOnMouseMoved(this::onMouseMoved);
        setOnMouseClicked(this::onMouseClicked);
        setOnMouseExited(e -> { hoverCol = -1; hoverRow = -1; draw(); });
    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        int cs = BattleMapData.CELL_SIZE;
        int cols = BattleMapData.COLS;
        int rows = BattleMapData.ROWS;

        gc.setFill(Color.rgb(8, 10, 8));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // ===== Terrain =====
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int px = x * cs, py = y * cs;
                gc.setFill(parseColor(data.getTerrain(x, y).hexColor));
                gc.fillRect(px, py, cs, cs);

                // Deploy zone shading
                if (x < 4) {
                    gc.setFill(Color.rgb(0, 80, 30, 0.10));
                    gc.fillRect(px, py, cs, cs);
                } else if (x >= cols - 4) {
                    gc.setFill(Color.rgb(80, 0, 0, 0.10));
                    gc.fillRect(px, py, cs, cs);
                }
            }
        }

        // ===== Range / Charge highlighting =====
        if (data.isShowRange() || data.isShowChargeRange()) {
            BattleMapData.UnitMarker sel = data.getSelectedUnit();
            if (sel != null) {
                int radius = data.isShowRange() ? data.getRangeRadius() : 2;
                Color highlightColor = data.isShowRange()
                        ? Color.rgb(0, 100, 0, 0.15)
                        : Color.rgb(100, 80, 0, 0.20);

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        int cx = sel.col() + dx;
                        int cy = sel.row() + dy;
                        if (cx >= 0 && cx < cols && cy >= 0 && cy < rows) {
                            if (Math.abs(dx) + Math.abs(dy) <= radius) {
                                gc.setFill(highlightColor);
                                gc.fillRect(cx * cs, cy * cs, cs, cs);
                            }
                        }
                    }
                }
            }
        }

        // ===== Movement range overlay =====
        if (data.getMoveRange() > 0) {
            BattleMapData.UnitMarker sel = data.getSelectedUnit();
            if (sel != null) {
                int range = data.getMoveRange();
                for (int dx = -range; dx <= range; dx++) {
                    for (int dy = -range; dy <= range; dy++) {
                        int cx = sel.col() + dx;
                        int cy = sel.row() + dy;
                        if (cx >= 0 && cx < cols && cy >= 0 && cy < rows) {
                            if (Math.abs(dx) + Math.abs(dy) <= range) {
                                gc.setFill(Color.rgb(0, 180, 80, 0.20));
                                gc.fillRect(cx * cs, cy * cs, cs, cs);
                                gc.setStroke(Color.rgb(0, 255, 100, 0.4));
                                gc.setLineWidth(0.5);
                                gc.strokeRect(cx * cs, cy * cs, cs, cs);
                                gc.setLineWidth(0.5);
                            }
                        }
                    }
                }
            }
        }


        // ===== Units =====
        BattleMapData.UnitMarker selected = data.getSelectedUnit();

        for (var unit : data.getUnits()) {
            int px = unit.col() * cs, py = unit.row() * cs;
            double hpRatio = Math.max(0, Math.min(1, (double) unit.hp() / unit.maxHp()));
            boolean isSelected = selected != null && selected.id().equals(unit.id());
            boolean isTargeted = highlightTargets.contains(unit.id());

            // Draw unit circle
            if (unit.isMarine()) {
                int green = (int)(80 + 140 * hpRatio);
                gc.setFill(Color.rgb(0, Math.min(255, green), 40));
            } else {
                if (isTargeted) {
                    gc.setFill(Color.rgb(255, 100, 100)); // bright red if targetable
                } else {
                    int red = (int)(80 + 140 * (1 - hpRatio * 0.5));
                    gc.setFill(Color.rgb(Math.min(255, red), 30, 30));
                }
            }

            // Draw as rounded rect for better visibility
            gc.fillOval(px + 4, py + 4, cs - 8, cs - 8);

            // Selection highlight: bright border
            if (isSelected) {
                gc.setStroke(Color.rgb(100, 255, 100));
                gc.setLineWidth(3);
                gc.strokeOval(px + 2, py + 2, cs - 4, cs - 4);
                gc.setLineWidth(0.5);
            }

            // Target highlight
            if (isTargeted) {
                gc.setStroke(Color.rgb(255, 200, 100));
                gc.setLineWidth(2);
                gc.strokeOval(px + 1, py + 1, cs - 2, cs - 2);
                gc.setLineWidth(0.5);
            }

            // Label
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", 10));
            gc.fillText(unit.label(), px + 4, py + cs / 2 + 3);

            // HP bar under label
            if (unit.hp() < unit.maxHp()) {
                double barWidth = cs - 8;
                double barHeight = 3;
                gc.setFill(Color.rgb(40, 40, 40));
                gc.fillRect(px + 4, py + cs - 6, barWidth, barHeight);
                gc.setFill(unit.isMarine() ? Color.rgb(0, 180, 60) : Color.rgb(180, 40, 40));
                gc.fillRect(px + 4, py + cs - 6, barWidth * hpRatio, barHeight);
            }
        }

        // ===== Grid lines =====
        gc.setStroke(Color.rgb(30, 40, 30, 0.4));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= cols; x++)
            gc.strokeLine(x * cs, 0, x * cs, rows * cs);
        for (int y = 0; y <= rows; y++)
            gc.strokeLine(0, y * cs, cols * cs, y * cs);

        // ===== Hover highlight =====
        if (hoverCol >= 0 && hoverRow >= 0) {
            gc.setStroke(Color.rgb(100, 255, 100));
            gc.setLineWidth(2);
            gc.strokeRect(hoverCol * cs, hoverRow * cs, cs, cs);
            gc.setLineWidth(0.5);
        }
    }

    // ==================== Public helpers ====================

    public void setHighlightTargets(Set<String> targetIds) {
        highlightTargets.clear();
        if (targetIds != null) highlightTargets.addAll(targetIds);
    }

    public void clearHighlights() {
        highlightTargets.clear();
    }

    // ==================== Mouse events ====================

    private void onMouseMoved(MouseEvent e) {
        int cs = BattleMapData.CELL_SIZE;
        int col = (int)(e.getX() / cs);
        int row = (int)(e.getY() / cs);
        if (col != hoverCol || row != hoverRow) {
            hoverCol = col; hoverRow = row;
            draw();
        }
    }

    private void onMouseClicked(MouseEvent e) {
        int cs = BattleMapData.CELL_SIZE;
        int col = (int)(e.getX() / cs);
        int row = (int)(e.getY() / cs);
        if (col >= 0 && col < BattleMapData.COLS && row >= 0 && row < BattleMapData.ROWS) {
            data.handleClick(col, row);
        }
    }

    private Color parseColor(String hex) {
        return Color.web(hex);
    }
}
