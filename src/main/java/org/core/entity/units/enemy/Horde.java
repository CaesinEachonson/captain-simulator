package org.core.entity.units.enemy;

import java.util.*;

public class Horde {

    private String id;
    private List<EnemyUnit> units;

    public Horde(String id) {
        this.id = id;
        this.units = new ArrayList<>();
    }

    public Horde(String id, List<EnemyUnit> units) {
        this.id = id;
        this.units = new ArrayList<>(units);
    }

    public void addUnit(EnemyUnit unit) {
        units.add(unit);
    }

    public void removeUnit(EnemyUnit unit) {
        units.remove(unit);
    }

    public void removeUnit(int index) {
        if (index >= 0 && index < units.size()) {
            units.remove(index);
        }
    }

    /**
     * Remove all units with currentWounds <= 0.
     * Call this after each round of combat to clean up dead enemies.
     */
    public void removeDeadUnits() {
        units.removeIf(unit -> unit.getCurrentWounds() <= 0);
    }

    public int getSize() {
        return units.size();
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }

    public List<EnemyUnit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public EnemyUnit getUnit(int index) {
        return units.get(index);
    }

    public String getId() {
        return id;
    }
}
