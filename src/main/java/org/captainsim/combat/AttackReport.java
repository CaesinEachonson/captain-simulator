package org.captainsim.combat;

public class AttackReport {
    private final String turnId;
    private final String attackerName;
    private final String attackerRole;
    private final String weaponName;
    private final String weaponSlot;       // "RIGHT_HAND", "LEFT_HAND"
    private final String targetType;
    private final int damage;
    private final boolean killed;
    private final boolean overheat;
    private final int distance;
    private final boolean isMelee;

    public AttackReport(String turnId, String attackerName, String attackerRole,
                        String weaponName, String weaponSlot, String targetType,
                        int damage, boolean killed, boolean overheat,
                        int distance, boolean isMelee) {
        this.turnId = turnId;
        this.attackerName = attackerName;
        this.attackerRole = attackerRole;
        this.weaponName = weaponName;
        this.weaponSlot = weaponSlot;
        this.targetType = targetType;
        this.damage = damage;
        this.killed = killed;
        this.overheat = overheat;
        this.distance = distance;
        this.isMelee = isMelee;
    }

    public String getTurnId() { return turnId; }
    public String getAttackerName() { return attackerName; }
    public String getAttackerRole() { return attackerRole; }
    public String getWeaponName() { return weaponName; }
    public String getWeaponSlot() { return weaponSlot; }
    public String getTargetType() { return targetType; }
    public int getDamage() { return damage; }
    public boolean isKilled() { return killed; }
    public boolean isOverheat() { return overheat; }
    public int getDistance() { return distance; }
    public boolean isMelee() { return isMelee; }

    /**
     * Unique key for grouping by attacker+weapon.
     */
    public String getWeaponKey() {
        return attackerName + "/" + weaponName + "/" + (isMelee ? "M" : weaponSlot.equals("LEFT_HAND") ? "L" : "R");
    }
}
