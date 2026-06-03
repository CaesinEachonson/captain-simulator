package org.captainsim.unit.enemy.factions;

import org.captainsim.unit.enemy.EnemyUnit;
import org.captainsim.unit.enemy.Horde;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.WeaponItem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EnemyChaos {

    // ==================== Template Record ====================

    public record ChaosTemplate(
            String typeId,
            int cost,
            int weight,
            String category,
            String weaponId,
            String sidearmId,
            String armourId,
            int ws, int bs, int s, int t, int ag, int intelligence, int wp, int fel,
            int wounds
    ) {}

    // ==================== Templates ====================

    private static final List<ChaosTemplate> TEMPLATES = List.of(
            // === Troops ===
            new ChaosTemplate("cultist",             10, 60, "troops", "autogun",       "combat_blade",   null,             25, 25, 20, 20, 20, 15, 15, 15, 40),
            new ChaosTemplate("traitor_guardsman",   20, 40, "troops", "lasgun",        "bayonet",        null,             30, 30, 25, 25, 25, 20, 20, 20, 50),

            // === Adept ===
            new ChaosTemplate("chaos_marine",       50, 70, "adept",  "bolt_gun",      "chainsword",     "mk_v_power_armour", 45, 40, 45, 45, 35, 30, 35, 30, 90),
            new ChaosTemplate("possessed",          60, 30, "adept",  "daemon_claws",  null,             "mk_v_power_armour", 50, 40, 55, 50, 40, 25, 40, 20, 100),
            new ChaosTemplate("raptor",             45, 40, "adept",  "assault_blade", "bolt_pistol",    "mk_v_power_armour", 45, 35, 40, 40, 50, 25, 30, 25, 80),
            new ChaosTemplate("chaos_biker",        55, 25, "adept",  "bolt_gun",      "chainsword",     "mk_v_power_armour", 40, 35, 45, 45, 45, 25, 30, 25, 90),

            // === Elites ===
            new ChaosTemplate("chaos_terminator",  100, 70, "elites","power_fist",    "heavy_war_bolter", "terminator_armour", 55, 50, 55, 55, 40, 35, 45, 35, 110),
            new ChaosTemplate("cultist_champion",   25, 30, "elites","combat_blade",  "autopistol",     null,             35, 35, 35, 30, 30, 25, 25, 30, 60),
            new ChaosTemplate("chaos_master",      120, 20, "elites","master_crafted_weapon", "bolt_pistol", "artificer_armour", 60, 55, 55, 55, 45, 45, 50, 45, 110),
            new ChaosTemplate("chaos_sorcerer",     90, 15, "elites","force_staff",   "bolt_pistol",    "mk_v_power_armour", 40, 40, 40, 40, 35, 55, 55, 40, 80)
    );

    // ==================== Threat Ratios (7 levels) ====================

    private static int[] getRatio(int threat) {
        int[][] ratios = {
                {80, 18, 2},
                {70, 25, 5},
                {60, 30, 10},
                {45, 35, 20},
                {30, 35, 35},
                {20, 30, 50},
                {10, 25, 65}
        };
        int index = Math.max(0, Math.min(6, threat - 1));
        return ratios[index];
    }

    // ==================== Horde Generation ====================

    public static Horde generateHorde(String id, int strength, int threat) {
        Horde horde = new Horde(id);
        int[] ratios = getRatio(threat);

        int troopsBudget  = Math.round(strength * ratios[0] / 100.0f);
        int adeptBudget   = Math.round(strength * ratios[1] / 100.0f);
        int elitesBudget  = strength - troopsBudget - adeptBudget;

        spendBudget(horde, "troops",  troopsBudget);
        spendBudget(horde, "adept",   adeptBudget);
        spendBudget(horde, "elites",  elitesBudget);

        return horde;
    }

    private static void spendBudget(Horde horde, String category, int budget) {
        int remaining = budget;
        int maxAttempts = 500;

        while (remaining > 0 && maxAttempts > 0) {
            ChaosTemplate template = pickWeightedInCategory(category);
            if (template == null) break;

            if (template.cost() <= remaining) {
                horde.addUnit(createFromTemplate(template));
                remaining -= template.cost();
            } else {
                ChaosTemplate cheapest = findCheapestInCategory(category);
                if (cheapest == null || cheapest.cost() > remaining) {
                    break;
                }
            }
            maxAttempts--;
        }
    }

    // ==================== Fixed Compositions ====================

    public static Horde generateFixedHorde(String id, String composition) {
        return switch (composition) {
            case "cultist_mob" -> {
                Horde h = new Horde(id);
                for (int i = 0; i < 20; i++) h.addUnit(createFromTemplate(getTemplate("cultist")));
                h.addUnit(createFromTemplate(getTemplate("cultist_champion")));
                yield h;
            }
            case "chaos_patrol" -> {
                Horde h = new Horde(id);
                for (int i = 0; i < 3; i++) h.addUnit(createFromTemplate(getTemplate("chaos_marine")));
                for (int i = 0; i < 5; i++) h.addUnit(createFromTemplate(getTemplate("cultist")));
                yield h;
            }
            case "terminator_squad" -> {
                Horde h = new Horde(id);
                for (int i = 0; i < 5; i++) h.addUnit(createFromTemplate(getTemplate("chaos_terminator")));
                yield h;
            }
            default -> new Horde(id);
        };
    }

    // ==================== Internal Helpers ====================

    private static final Map<String, List<ChaosTemplate>> BY_CATEGORY = new HashMap<>();
    private static final Map<String, Integer> CATEGORY_TOTAL_WEIGHT = new HashMap<>();

    static {
        for (ChaosTemplate t : TEMPLATES) {
            BY_CATEGORY.computeIfAbsent(t.category(), k -> new ArrayList<>()).add(t);
        }
        for (Map.Entry<String, List<ChaosTemplate>> entry : BY_CATEGORY.entrySet()) {
            CATEGORY_TOTAL_WEIGHT.put(entry.getKey(),
                    entry.getValue().stream().mapToInt(ChaosTemplate::weight).sum());
        }
    }

    private static ChaosTemplate pickWeightedInCategory(String category) {
        List<ChaosTemplate> pool = BY_CATEGORY.get(category);
        if (pool == null || pool.isEmpty()) return null;
        int totalWeight = CATEGORY_TOTAL_WEIGHT.get(category);
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (ChaosTemplate t : pool) {
            cumulative += t.weight();
            if (roll < cumulative) return t;
        }
        return pool.get(pool.size() - 1);
    }

    private static ChaosTemplate findCheapestInCategory(String category) {
        List<ChaosTemplate> pool = BY_CATEGORY.get(category);
        if (pool == null || pool.isEmpty()) return null;
        return pool.stream().min(Comparator.comparingInt(ChaosTemplate::cost)).orElse(null);
    }

    private static ChaosTemplate getTemplate(String typeId) {
        return TEMPLATES.stream()
                .filter(t -> t.typeId().equals(typeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enemy type: " + typeId));
    }

    private static EnemyUnit createFromTemplate(ChaosTemplate t) {
        EnemyUnit enemy = new EnemyUnit(t.typeId(), t.cost());
        enemy.setStats(t.ws(), t.bs(), t.s(), t.t(), t.ag(), t.intelligence(), t.wp(), t.fel(), t.wounds());
        if (t.weaponId() != null) {
            enemy.setRightHand(new WeaponItem(t.weaponId()));
        }
        if (t.sidearmId() != null) {
            enemy.setLeftHand(new WeaponItem(t.sidearmId()));
        }
        if (t.armourId() != null) {
            enemy.setArmorKit(new ArmourItem(t.armourId()));
        }
        return enemy;
    }
}
