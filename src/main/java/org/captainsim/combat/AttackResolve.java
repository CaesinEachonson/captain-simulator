package org.captainsim.combat;

public class AttackResolve {
    private final int damage;
    private final boolean overheat;

    public AttackResolve(int damage) {
        this(damage, false);
    }

    public AttackResolve(int damage, boolean overheat) {
        this.damage = damage;
        this.overheat = overheat;
    }

    public int getDamage() { return damage; }
    public boolean isOverheat() { return overheat; }
}
