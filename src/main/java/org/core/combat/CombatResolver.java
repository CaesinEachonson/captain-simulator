package org.core.combat;

import org.core.entity.base.BaseUnit;
import org.core.entity.items.WeaponItem;
import org.core.utils.Dice;

import java.util.List;

public class CombatResolver {

    private static final int OVERHEAT_THRESHOLD = 10;
    private static final float HEAVY_STATIONARY_BONUS = 1.5f;
    private static final int RAPID_FIRE_MULTIPLIER = 2;

    // ==================== Ranged Attack ====================

    public static AttackResolve calculateSingleRangedDamage(BaseUnit attacker, WeaponItem weapon,
                                                           BaseUnit target, int distance, boolean hasMoved) {
        if (weapon == null) return new AttackResolve(0);

        float bs = attacker.getBs();
        float skillMod = 1.0f + (bs - 40) / 100.0f;
        float rawPerHit = weapon.getDamage() * skillMod;

        if (weapon.getTraits().contains("heavy") && !hasMoved) {
            rawPerHit *= HEAVY_STATIONARY_BONUS;
        }

        int targetArmour = 0;
        if (target != null && target.getArmorKit() != null) {
            targetArmour = target.getArmorKit().getArmourValue();
        }

        if (weapon.getTraits().contains("melta_weapon") && distance <= weapon.getRange() / 2) {
            targetArmour = 0;
        }

        float gravBonus = 0;
        if (weapon.getTraits().contains("grav_weapon")) {
            gravBonus = targetArmour * 0.5f;
        }

        int effectiveArmour = Math.max(0, targetArmour - weapon.getArmourPenetration());
        float damage = Math.max(1, rawPerHit + gravBonus - effectiveArmour);

        boolean overheat = false;
        if (weapon.getTraits().contains("overheat") && Dice.rollD100() <= OVERHEAT_THRESHOLD) {
            overheat = true;
        }

        return new AttackResolve(Math.round(damage), overheat);
    }


    // ==================== Melee Attack ====================

    public static AttackResolve calculateSingleMeleeDamage(BaseUnit attacker, WeaponItem weapon,
                                                           BaseUnit target) {
        if (weapon == null) return new AttackResolve(0);

        float rawPerHit = getRawMeleePerHit(attacker, weapon);

        int targetArmour = 0;
        if (target != null && target.getArmorKit() != null) {
            targetArmour = target.getArmorKit().getArmourValue();
        }
        int effectiveArmour = Math.max(0, targetArmour - weapon.getArmourPenetration());
        float afterArmour = Math.max(1, rawPerHit - effectiveArmour);

        int totalDamage = Math.round(afterArmour * weapon.getAttacks());

        return new AttackResolve(totalDamage);
    }

    private static float getRawMeleePerHit(BaseUnit attacker, WeaponItem weapon) {
        float ws = attacker.getWs();
        float s = attacker.getS();
        float ag = attacker.getAg();

        float skillMod = 1.0f + (ws - 40) / 100.0f;

        float attributeMod;
        List<String> traits = weapon.getTraits();
        if (traits.contains("brutal")) {
            attributeMod = 1.0f + s / 100.0f;
        } else if (traits.contains("finesse")) {
            attributeMod = 1.0f + ag / 100.0f;
        } else {
            float avgStat = (s + ag) / 2.0f;
            attributeMod = 1.0f + avgStat / 100.0f;
        }

        return weapon.getDamage() * skillMod * attributeMod;
    }

    // ==================== Overheat Damage ====================

    public static int calculateOverheatDamage(WeaponItem weapon) {
        return Math.max(1, weapon.getDamage() / 5);
    }
}
