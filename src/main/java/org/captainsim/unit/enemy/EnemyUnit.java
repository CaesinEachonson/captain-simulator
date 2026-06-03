package org.captainsim.unit.enemy;

import org.captainsim.unit.BaseUnit;
import org.captainsim.item.WeaponItem;

/**
 * A generic enemy unit. Stats are set via constructor from a template.
 */
public class EnemyUnit extends BaseUnit {

    private String typeId;
    private int cost;
    private WeaponItem rightHand;
    private WeaponItem leftHand;

    public EnemyUnit(String typeId, int cost) {
        super();
        this.typeId = typeId;
        this.cost = cost;
        this.factionId = "enemy";
    }

    /**
     * Set all core attributes and wounds at once.
     */
    public void setStats(int ws, int bs, int s, int t, int ag, int intelligence, int wp, int fel, int wounds) {
        this.ws = ws;
        this.bs = bs;
        this.s = s;
        this.t = t;
        this.ag = ag;
        this.intelligence = intelligence;
        this.wp = wp;
        this.fel = fel;
        this.wounds = wounds;
        this.currentWounds = wounds;
    }

    @Override
    public void generateAttributes() {
        // Stats are set externally via setStats(), not rolled
    }

    public String getTypeId() { return typeId; }
    public int getCost() { return cost; }

    public void setRightHand(WeaponItem rightHand) {
        this.rightHand = rightHand;
    }

    public WeaponItem getRightHand() {
        return rightHand;
    }

    public WeaponItem getLeftHand() {
        return leftHand;
    }

    public void setLeftHand(WeaponItem leftHand) {
        this.leftHand = leftHand;
    }
}

