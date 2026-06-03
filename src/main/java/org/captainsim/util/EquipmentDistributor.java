package org.captainsim.util;

import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.WeaponItem;
import org.captainsim.util.Dice;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EquipmentDistributor {

    private static Map<String, WeaponItem> meleeWeapons;
    private static Map<String, WeaponItem> rangedWeapons;
    private static Map<String, ArmourItem> armours;

    public static void init(Map<String, WeaponItem> melee, Map<String, WeaponItem> ranged,
                            Map<String, ArmourItem> armourMap) {
        meleeWeapons = melee;
        rangedWeapons = ranged;
        armours = armourMap;
    }

    // ==================== Weapon Pools (String IDs) ====================

    public static final List<String> STANDARD_IDS = List.of(
            "bolt_gun", "plasma_gun", "melta_gun", "grav_gun"
    );
    public static final List<String> PISTOL_IDS = List.of(
            "bolt_pistol", "plasma_pistol", "inferno_pistol", "grav_pistol"
    );
    public static final List<String> HEAVY_IDS = List.of(
            "heavy_bolt_gun", "plasma_cannon", "multi_melta", "grav_cannon"
    );
    public static final List<String> ALL_MELEE_IDS = List.of(
            "combat_knife", "chainsword", "power_sword", "power_axe",
            "thunder_hammer", "power_fist", "lightning_claw"
    );

    // ==================== Armor Distribution ====================

    public static String rollArmorId() {
        int roll = Dice.rollD10();
        return switch (roll) {
            case 1 -> "mk_iv_power_armour";
            case 2 -> "mk_v_power_armour";
            case 3, 4 -> "mk_vi_power_armour";
            case 5, 6, 7, 8, 9 -> "mk_vii_power_armour";
            case 10 -> "mk_viii_power_armour";
            default -> throw new IllegalStateException("Unexpected roll: " + roll);
        };
    }

    public static void assignArmor(MarineUnit marine) {
        String armourId = rollArmorId();
        ArmourItem armour = armours.get(armourId);
        if (armour == null) {
            throw new IllegalArgumentException("Armour not found: " + armourId);
        }
        marine.setArmorKit(armour);
    }

    // ==================== Weapon Lookup ====================

    private static WeaponItem getMelee(String id) {
        WeaponItem w = meleeWeapons.get(id);
        if (w == null) throw new IllegalArgumentException("Melee weapon not found: " + id);
        return w;
    }

    private static WeaponItem getRanged(String id) {
        WeaponItem w = rangedWeapons.get(id);
        if (w == null) throw new IllegalArgumentException("Ranged weapon not found: " + id);
        return w;
    }

    private static WeaponItem getAnyWeapon(String id) {
        WeaponItem w = rangedWeapons.get(id);
        if (w != null) return w;
        w = meleeWeapons.get(id);
        if (w != null) return w;
        throw new IllegalArgumentException("Weapon not found: " + id);
    }

    // ==================== Random Selection ====================

    private static WeaponItem randomRanged(List<String> pool) {
        return getRanged(pool.get(ThreadLocalRandom.current().nextInt(pool.size())));
    }

    private static WeaponItem randomRangedExcept(List<String> pool, String... excludes) {
        List<String> filtered = new ArrayList<>(pool);
        for (String ex : excludes) filtered.remove(ex);
        return getRanged(filtered.get(ThreadLocalRandom.current().nextInt(filtered.size())));
    }

    private static WeaponItem randomMelee(List<String> pool) {
        return getMelee(pool.get(ThreadLocalRandom.current().nextInt(pool.size())));
    }

    private static WeaponItem randomMeleeExcept(List<String> pool, String... excludes) {
        List<String> filtered = new ArrayList<>(pool);
        for (String ex : excludes) filtered.remove(ex);
        return getMelee(filtered.get(ThreadLocalRandom.current().nextInt(filtered.size())));
    }

    private static WeaponItem randomStandard() {
        return randomRanged(STANDARD_IDS);
    }

    private static WeaponItem randomStandardExcept(String exclude) {
        return randomRangedExcept(STANDARD_IDS, exclude);
    }

    private static WeaponItem randomPistol() {
        return randomRanged(PISTOL_IDS);
    }

    private static WeaponItem randomPistolExcept(String exclude) {
        return randomRangedExcept(PISTOL_IDS, exclude);
    }

    private static WeaponItem randomHeavy() {
        return randomRanged(HEAVY_IDS);
    }

    private static WeaponItem randomMelee() {
        return randomMelee(ALL_MELEE_IDS);
    }

    private static WeaponItem randomMeleeExcept(String... excludes) {
        return randomMeleeExcept(ALL_MELEE_IDS, excludes);
    }

    private static WeaponItem randomStandardOrPistol() {
        List<String> pool = new ArrayList<>(STANDARD_IDS);
        pool.addAll(PISTOL_IDS);
        return randomRanged(pool);
    }

    // ==================== Post-Processing ====================

    public static void applyLightningClawDualWield(MarineUnit marine) {
        WeaponItem rh = marine.getRightHand();
        WeaponItem lh = marine.getLeftHand();
        if (rh != null && "lightning_claw".equals(rh.getId()) && !"lightning_claw".equals(lh.getId())) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                marine.setLeftHand(getMelee("lightning_claw"));
            }
        } else if (lh != null && "lightning_claw".equals(lh.getId()) && !"lightning_claw".equals(rh.getId())) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                marine.setRightHand(getMelee("lightning_claw"));
            }
        }
    }

    public static void maybeSwapHands(MarineUnit marine) {
        if (ThreadLocalRandom.current().nextBoolean()) {
            WeaponItem tmp = marine.getRightHand();
            marine.setRightHand(marine.getLeftHand());
            marine.setLeftHand(tmp);
        }
    }

    // ==================== Command Group ====================

    public static void equipCaptain(MarineUnit marine) {
        marine.setRightHand(randomMelee());
        marine.setLeftHand(randomPistol());
    }

    public static void equipChaplain(MarineUnit marine) {
        marine.setRightHand(getMelee("crozius_arcanum"));
        marine.setLeftHand(randomPistol());
    }

    public static void equipTechmarine(MarineUnit marine) {
        marine.setRightHand(getMelee("omnissian_axe"));
        marine.setLeftHand(randomPistol());
    }

    public static void equipApothecary(MarineUnit marine) {
        marine.setRightHand(getMelee("chainsword"));
        marine.setLeftHand(getRanged("bolt_pistol"));
    }

    public static void equipCompanyChampion(MarineUnit marine) {
        marine.setRightHand(getMelee("power_sword"));
        marine.setLeftHand(getRanged("bolt_pistol"));
    }

    public static void equipAncient(MarineUnit marine) {
        marine.setRightHand(getMelee("company_standard"));
        marine.setLeftHand(getRanged("bolt_gun"));
    }

    public static void equipVeteran(MarineUnit marine) {
        marine.setRightHand(randomStandardOrPistol());
        marine.setLeftHand(getMelee("chainsword"));
    }

    public static void equipSergeant(MarineUnit marine) {
        marine.setRightHand(randomMeleeExcept("thunder_hammer"));
        marine.setLeftHand(randomPistol());
    }

    public static void equipTacticalMarine(MarineUnit marine, boolean replaceStandard, boolean replaceHeavy) {
        if (replaceHeavy) {
            marine.setRightHand(randomHeavy());
        } else if (replaceStandard) {
            marine.setRightHand(randomStandardExcept("bolt_gun"));
        } else {
            marine.setRightHand(getRanged("bolt_gun"));
        }
        marine.setLeftHand(getMelee("combat_knife"));
    }

    public static void equipAssaultMarine(MarineUnit marine, boolean replaceMelee, boolean replacePistol) {
        marine.setRightHand(replaceMelee
                ? randomMeleeExcept("chainsword", "combat_knife", "thunder_hammer")
                : getMelee("chainsword"));
        marine.setLeftHand(replacePistol
                ? randomPistolExcept("bolt_pistol")
                : getRanged("bolt_pistol"));
    }

    public static void equipDevastatorMarine(MarineUnit marine, boolean replaceHeavy) {
        marine.setRightHand(replaceHeavy ? randomHeavy() : getRanged("bolt_gun"));
        marine.setLeftHand(getMelee("combat_knife"));
    }
}
