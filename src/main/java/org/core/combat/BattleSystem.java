package org.core.combat;

import org.core.entity.units.enemy.EnemyUnit;
import org.core.entity.units.enemy.Horde;
import org.core.entity.units.marine.MarineUnit;
import org.core.entity.items.WeaponItem;
import org.core.entity.units.squad.Squad;
import org.core.utils.Dice;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BattleSystem {

    private static final int OVERHEAT_PERCENT = 10;
    private static final float HEAVY_BONUS = 1.5f;
    private static final int RAPID_FIRE_MULT = 2;

    private static int turnCounter = 0;

    public static String nextTurnId() {
        turnCounter++;
        return "T" + turnCounter;
    }

    // ==================== Squad Ranged Attack ====================

    public static CombatReport squadRangedAttack(Squad squad, Horde horde,
                                                 int distance, boolean hasMoved) {
        CombatReport report = new CombatReport();
        report.setTurnId(nextTurnId());

        for (MarineUnit marine : squad.getAllMarines()) {
            if (!marine.isAvailable() || marine.getCurrentWounds() <= 0) continue;

            // Right hand
            if (marine.getRightHand() != null && canFireRanged(marine.getRightHand())) {
                fireRangedWeapon(marine, marine.getRightHand(), "RIGHT_HAND",
                        horde, distance, hasMoved, false, report);
            }
            // Left hand
            if (marine.getLeftHand() != null && canFireRanged(marine.getLeftHand())) {
                fireRangedWeapon(marine, marine.getLeftHand(), "LEFT_HAND",
                        horde, distance, hasMoved, false, report);
            }
        }

        return report;
    }

    // ==================== Squad Melee Attack ====================

    public static CombatReport squadMeleeAttack(Squad squad, Horde horde) {
        CombatReport report = new CombatReport();
        report.setTurnId(nextTurnId());

        for (MarineUnit marine : squad.getAllMarines()) {
            if (!marine.isAvailable() || marine.getCurrentWounds() <= 0) continue;

            // Right hand — melee weapon
            if (marine.getRightHand() != null) {
                WeaponItem w = marine.getRightHand();
                if (isMeleeWeapon(w)) {
                    swingMeleeWeapon(marine, w, "RIGHT_HAND", horde, report);
                } else if (isPistol(w)) {
                    fireRangedWeapon(marine, w, "RIGHT_HAND",
                            horde, 0, false, true, report);
                }
            }
            // Left hand — melee weapon or pistol
            if (marine.getLeftHand() != null) {
                WeaponItem w = marine.getLeftHand();
                if (isMeleeWeapon(w)) {
                    swingMeleeWeapon(marine, w, "LEFT_HAND", horde, report);
                } else if (isPistol(w)) {
                    fireRangedWeapon(marine, w, "LEFT_HAND",
                            horde, 0, false, true, report);
                }
            }
        }

        return report;
    }

    // ==================== Horde Attacks Squad ====================

    public static CombatReport hordeAttacksSquad(Horde horde, Squad squad, int distance) {
        CombatReport report = new CombatReport();
        report.setTurnId(nextTurnId());

        for (EnemyUnit enemy : horde.getUnits()) {
            if (enemy.getCurrentWounds() <= 0) continue;

            WeaponItem weapon = enemy.getRightHand();
            if (weapon == null) continue;

            int attacks = weapon.getAttacks();
            for (int i = 0; i < attacks; i++) {
                MarineUnit target = pickRandomMarine(squad);
                if (target == null) break;

                float raw;
                String targetType = enemy.getTypeId();
                boolean overheat = false;
                int distanceUsed = distance;

                if (distance <= 1 && isMeleeWeapon(weapon)) {
                    AttackResolve result = CombatResolver.calculateSingleMeleeDamage(enemy, weapon, target);
                    raw = result.getDamage();
                } else if (distance > 1 && !isMeleeOnly(weapon)) {
                    AttackResolve result = CombatResolver.calculateSingleRangedDamage(
                            enemy, weapon, target, distance, false);
                    raw = result.getDamage();
                    overheat = result.isOverheat();
                } else if (distance <= 1 && isPistol(weapon)) {
                    AttackResolve result = CombatResolver.calculateSingleRangedDamage(
                            enemy, weapon, target, 0, false);
                    raw = result.getDamage();
                    overheat = result.isOverheat();
                    distanceUsed = 0;
                } else {
                    continue;
                }

                int dmg = Math.round(raw);
                if (dmg > 0) {
                    boolean killed = applyDamageToMarine(target, dmg);
                    report.addAttackRecord(new AttackReport(
                            report.getTurnId(), enemy.getTypeId(), "ENEMY",
                            weapon.getName(), "RIGHT_HAND", target.getName(),
                            dmg, killed, overheat, distanceUsed, distance <= 1));
                    if (killed) {
                        report.addMarineCasualty(target.getName());
                    }
                }
            }
        }

        return report;
    }

    // ==================== Ranged Weapon Fire ====================

    private static void fireRangedWeapon(MarineUnit shooter, WeaponItem weapon, String slot,
                                         Horde horde, int distance, boolean hasMoved,
                                         boolean isMelee, CombatReport report) {
        int attacks = weapon.getAttacks();

        if (!isMelee && weapon.getTraits().contains("rapid_fire") && distance <= weapon.getRange() / 2) {
            attacks *= RAPID_FIRE_MULT;
        }

        float baseDamage;
        {
            AttackResolve sample = CombatResolver.calculateSingleRangedDamage(
                    shooter, weapon, null, distance, hasMoved);
            // We use the raw per-hit value — we need to recalc for each target
        }

        for (int i = 0; i < attacks; i++) {
            if (horde.isEmpty()) break;

            EnemyUnit target = selectTarget(horde, weapon);
            if (target == null) break;

            AttackResolve result = CombatResolver.calculateSingleRangedDamage(
                    shooter, weapon, target, distance, hasMoved);
            int dmg = result.getDamage();

            if (dmg > 0) {
                boolean killed = applyDamageToEnemy(target, dmg);
                report.addAttackRecord(new AttackReport(
                        report.getTurnId(), shooter.getName(), shooter.getRole().name(),
                        weapon.getName(), slot, target.getTypeId(),
                        dmg, killed, false, distance, false));
                if (killed) {
                    report.addEnemyKillSummary(shooter.getName() + " killed " + target.getTypeId());
                }
            }

            // Overheat per shot
            if (weapon.getTraits().contains("overheat") && Dice.rollD100() <= OVERHEAT_PERCENT) {
                int selfDmg = CombatResolver.calculateOverheatDamage(weapon);
                shooter.setCurrentWounds(shooter.getCurrentWounds() - selfDmg);
                report.addOverheat(shooter.getName(), selfDmg);
            }
        }
    }

    // ==================== Melee Swing ====================

    private static void swingMeleeWeapon(MarineUnit attacker, WeaponItem weapon, String slot,
                                         Horde horde, CombatReport report) {
        int attacks = weapon.getAttacks();

        for (int i = 0; i < attacks; i++) {
            if (horde.isEmpty()) break;

            EnemyUnit target = selectTarget(horde, weapon);
            if (target == null) break;

            AttackResolve result = CombatResolver.calculateSingleMeleeDamage(attacker, weapon, target);
            int dmg = result.getDamage();

            if (dmg > 0) {
                boolean killed = applyDamageToEnemy(target, dmg);
                report.addAttackRecord(new AttackReport(
                        report.getTurnId(), attacker.getName(), attacker.getRole().name(),
                        weapon.getName(), slot, target.getTypeId(),
                        dmg, killed, false, 0, true));
                if (killed) {
                    report.addEnemyKillSummary(attacker.getName() + " killed " + target.getTypeId());
                }
            }
        }
    }

    // ==================== Damage Application ====================

    private static boolean applyDamageToEnemy(EnemyUnit target, int damage) {
        int newWounds = target.getCurrentWounds() - damage;
        if (newWounds <= 0) {
            target.setCurrentWounds(0);
            return true;
        }
        target.setCurrentWounds(newWounds);
        return false;
    }

    private static boolean applyDamageToMarine(MarineUnit target, int damage) {
        int newWounds = target.getCurrentWounds() - damage;
        if (newWounds <= 0) {
            target.setCurrentWounds(0);
            target.setAvailable(false);
            return true;
        }
        target.setCurrentWounds(newWounds);
        return false;
    }

    // ==================== Target Selection ====================

    private static EnemyUnit selectTarget(Horde horde, WeaponItem weapon) {
        List<EnemyUnit> targets = horde.getUnits().stream()
                .filter(u -> u.getCurrentWounds() > 0).toList();
        if (targets.isEmpty()) return null;

        if (weapon.getTraits().contains("melta_weapon")) {
            return targets.stream()
                    .max(Comparator.comparingInt(u -> {
                        if (u.getArmorKit() != null) return u.getArmorKit().getArmourValue();
                        return Math.round(u.getT());
                    }))
                    .orElse(null);
        } else if (weapon.getTraits().contains("precise")) {
            return targets.stream()
                    .max(Comparator.comparingInt(EnemyUnit::getCost))
                    .orElse(null);
        } else {
            return targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
        }
    }

    private static MarineUnit pickRandomMarine(Squad squad) {
        List<MarineUnit> available = squad.getAllMarines().stream()
                .filter(m -> m.isAvailable() && m.getCurrentWounds() > 0)
                .toList();
        if (available.isEmpty()) return null;
        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    // ==================== Weapon Type Checks ====================

    private static boolean isMeleeWeapon(WeaponItem w) {
        return w.getTraits().contains("balanced") || w.getTraits().contains("brutal")
                || w.getTraits().contains("finesse") || w.getTraits().contains("banner");
    }

    private static boolean isPistol(WeaponItem w) {
        return w.getTraits().contains("pistol");
    }

    private static boolean isMeleeOnly(WeaponItem w) {
        return isMeleeWeapon(w) && !isPistol(w);
    }

    private static boolean canFireRanged(WeaponItem w) {
        return !isMeleeOnly(w);
    }
}
