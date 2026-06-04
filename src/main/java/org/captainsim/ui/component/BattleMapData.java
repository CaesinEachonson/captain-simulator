package org.captainsim.ui.component;

import java.util.*;
import java.util.function.Consumer;

public class BattleMapData {

    public static final int COLS = 40;
    public static final int ROWS = 40;
    public static final int CELL_SIZE = 40;

    public enum Terrain {
        OPEN("#0f120f"),
        RUBBLE("#2a2218"),
        CRATER("#1e2518"),
        TRENCH("#1a1a10"),
        RUINS("#2a1a10"),
        WATER("#0a1a20");

        public final String hexColor;
        Terrain(String hex) { this.hexColor = hex; }
    }

    public record UnitMarker(String id, String label, int col, int row,
                             boolean isMarine, int hp, int maxHp, int unitCount) {}

    public record CellInfo(int col, int row, Terrain terrain, UnitMarker unit) {}

    private final Terrain[][] terrain = new Terrain[COLS][ROWS];
    private final List<UnitMarker> units = new ArrayList<>();
    private Consumer<CellInfo> onClick;

    // ===== Selection state for manual control =====
    private String selectedUnitId;
    private boolean showRange;          // whether to highlight weapon range
    private int rangeRadius;            // range radius to display
    private boolean showChargeRange;    // whether to highlight charge range

    public BattleMapData() {
        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++)
                terrain[x][y] = Terrain.OPEN;
    }

    public void generateTerrain(long seed) {
        Random rand = new Random(seed);
        for (int x = 3; x < COLS - 3; x++) {
            for (int y = 0; y < ROWS; y++) {
                double r = rand.nextDouble();
                if (r < 0.06) terrain[x][y] = Terrain.RUBBLE;
                else if (r < 0.12) terrain[x][y] = Terrain.CRATER;
                else if (r < 0.16) terrain[x][y] = Terrain.TRENCH;
                else if (r < 0.19) terrain[x][y] = Terrain.RUINS;
                else if (r < 0.21) terrain[x][y] = Terrain.WATER;
            }
        }
    }

    public void clearUnits() { units.clear(); }
    public void addUnit(UnitMarker u) { units.add(u); }

    /** 敌人整体向左推进 steps 格 */
    public void advanceEnemies(int steps) {
        for (int i = 0; i < units.size(); i++) {
            UnitMarker u = units.get(i);
            if (!u.isMarine()) {
                units.set(i, new UnitMarker(u.id(), u.label(),
                        Math.max(3, u.col() - steps),
                        u.row(), false, u.hp(), u.maxHp(), u.unitCount));
            }
        }
    }

    public void advanceEnemiesForCluster(String clusterId, int steps) {
        for (int i = 0; i < units.size(); i++) {
            UnitMarker u = units.get(i);
            if (u.id().equals(clusterId)) {
                units.set(i, new UnitMarker(u.id(), u.label(),
                        Math.max(3, u.col() - steps),
                        u.row(), false, u.hp(), u.maxHp(), u.unitCount));
                return;
            }
        }
    }

    public void handleClick(int col, int row) {
        if (onClick != null) {
            UnitMarker u = findUnit(col, row);
            onClick.accept(new CellInfo(col, row, terrain[col][row], u));
        }
    }

    public UnitMarker findUnit(int col, int row) {
        return units.stream().filter(u -> u.col() == col && u.row() == row).findFirst().orElse(null);
    }

    public void removeDeadUnit(String id) {
        units.removeIf(u -> u.id().equals(id));
    }

    public void updateUnitHp(String id, int hp) {
        for (int i = 0; i < units.size(); i++) {
            UnitMarker u = units.get(i);
            if (u.id().equals(id)) {
                units.set(i, new UnitMarker(u.id(), u.label(), u.col(), u.row(), u.isMarine(), hp, u.maxHp(), u.unitCount));
                return;
            }
        }
    }

    /** Move a marine unit marker to a new position */
    public void moveUnit(String id, int newCol, int newRow) {
        for (int i = 0; i < units.size(); i++) {
            UnitMarker u = units.get(i);
            if (u.id().equals(id)) {
                units.set(i, new UnitMarker(u.id(), u.label(), newCol, newRow, u.isMarine(), u.hp(), u.maxHp(), u.unitCount));
                return;
            }
        }
    }

    // ==================== Selection ====================

    public void selectUnit(String unitId) {
        this.selectedUnitId = unitId;
    }

    public void clearSelection() {
        this.selectedUnitId = null;
        this.showRange = false;
        this.showChargeRange = false;
    }

    public String getSelectedUnitId() {
        return selectedUnitId;
    }

    public UnitMarker getSelectedUnit() {
        if (selectedUnitId == null) return null;
        return units.stream().filter(u -> u.id().equals(selectedUnitId)).findFirst().orElse(null);
    }

    // ==================== Range Display ====================

    public void setShowRange(boolean show, int range) {
        this.showRange = show;
        this.rangeRadius = range;
    }

    public boolean isShowRange() { return showRange; }
    public int getRangeRadius() { return rangeRadius; }

    public void setShowChargeRange(boolean show) {
        this.showChargeRange = show;
    }

    public boolean isShowChargeRange() { return showChargeRange; }

    // ==================== Distance Calculations ====================

    /** Manhattan distance between two cells */
    public static int manhattanDistance(int c1, int r1, int c2, int r2) {
        return Math.abs(c1 - c2) + Math.abs(r1 - r2);
    }

    /** Get distance from the selected unit to a specific enemy marker */
    public int distanceToEnemy(String enemyId) {
        UnitMarker selected = getSelectedUnit();
        if (selected == null) return Integer.MAX_VALUE;
        UnitMarker enemy = units.stream().filter(u -> u.id().equals(enemyId)).findFirst().orElse(null);
        if (enemy == null) return Integer.MAX_VALUE;
        return manhattanDistance(selected.col(), selected.row(), enemy.col(), enemy.row());
    }

    /** Get the nearest enemy distance from a given position */
    public int nearestEnemyDistance(int col, int row) {
        return units.stream()
                .filter(u -> !u.isMarine() && u.hp() > 0)
                .mapToInt(u -> manhattanDistance(col, row, u.col(), u.row()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    /** Check if a cell is within range of the selected unit's position */
    public boolean isInRangeOfSelected(int col, int row) {
        UnitMarker sel = getSelectedUnit();
        if (sel == null || !showRange) return false;
        return manhattanDistance(sel.col(), sel.row(), col, row) <= rangeRadius;
    }

    /** Check if a cell is within charge range of the selected unit */
    public boolean isInChargeRangeOfSelected(int col, int row) {
        UnitMarker sel = getSelectedUnit();
        if (sel == null || !showChargeRange) return false;
        return manhattanDistance(sel.col(), sel.row(), col, row) <= 2;
    }

    /** Find all enemy markers within a given weapon range from a position */
    public List<UnitMarker> findEnemiesInRange(int col, int row, int weaponRange) {
        return units.stream()
                .filter(u -> !u.isMarine() && u.hp() > 0)
                .filter(u -> manhattanDistance(col, row, u.col(), u.row()) <= weaponRange)
                .toList();
    }

    /** Find all enemy markers within charge range (2 cells) from a position */
    public List<UnitMarker> findEnemiesInChargeRange(int col, int row) {
        return units.stream()
                .filter(u -> !u.isMarine() && u.hp() > 0)
                .filter(u -> manhattanDistance(col, row, u.col(), u.row()) <= 2)
                .toList();
    }

    // ==================== Combat Distance ====================

    /**
     * Calculate the effective combat distance between a marine squad position
     * and the nearest enemy marker. Used for ranged attack resolution.
     */
    public int getCombatDistance(int marineCol, int marineRow) {
        return nearestEnemyDistance(marineCol, marineRow);
    }

    // Getters
    public Terrain getTerrain(int col, int row) { return terrain[col][row]; }
    public List<UnitMarker> getUnits() { return units; }
    public void setOnClick(Consumer<CellInfo> onClick) { this.onClick = onClick; }

    /** Check if a cell is within movement range (Manhattan diamond) of the selected unit */
    public boolean isInMoveRangeOfSelected(int col, int row) {
        UnitMarker sel = getSelectedUnit();
        if (sel == null || moveRange <= 0) return false;
        return manhattanDistance(sel.col(), sel.row(), col, row) <= moveRange
                && col >= 0 && col < COLS && row >= 0 && row < ROWS;
    }

    /** Get all valid move destination cells within range from a position */
    public List<int[]> getValidMoveCells(int fromCol, int fromRow, int range) {
        List<int[]> cells = new ArrayList<>();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                int nc = fromCol + dx;
                int nr = fromRow + dy;
                if (nc >= 0 && nc < COLS && nr >= 0 && nr < ROWS) {
                    if (manhattanDistance(fromCol, fromRow, nc, nr) <= range) {
                        cells.add(new int[]{nc, nr});
                    }
                }
            }
        }
        return cells;
    }

    // Add a field and setter
    private int moveRange = 0;

    public void setMoveRange(int range) {
        this.moveRange = range;
    }
    public int getMoveRange() { return moveRange; }
}
