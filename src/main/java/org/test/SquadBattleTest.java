package org.test;

import org.captainsim.attribute.WorldType;
import org.captainsim.combat.AttackReport;
import org.captainsim.combat.BattleSystem;
import org.captainsim.combat.CombatReport;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.WeaponItem;
import org.captainsim.item.factory.GearFactory;
import org.captainsim.unit.enemy.EnemyUnit;
import org.captainsim.unit.enemy.Horde;
import org.captainsim.unit.enemy.factions.EnemyChaos;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.unit.marine.BattleRole;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.captainsim.squad.Squad;
import org.captainsim.squad.SquadType;
import org.captainsim.util.EquipmentDistributor;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SquadBattleTest {

    // ==================== Configuration ====================

    private static final SquadType SQUAD_TYPE = SquadType.ASSAULT_SQUAD;
    private static final boolean USE_CUSTOM_ENEMY = true;
    private static final int ENEMY_STRENGTH = 1200;
    private static final int ENEMY_THREAT = 2;
    private static final int COMBAT_DISTANCE = 2;

    // ==================== Main ====================

    public static void main(String[] args) throws Exception {
        // 1. Load data
        Map<String, WeaponItem> meleeWeapons = loadWeapons("data/weapons_melee.json");
        Map<String, WeaponItem> rangedWeapons = loadWeapons("data/weapons_ranged.json");
        Map<String, ArmourItem> armours = loadArmours("data/armour.json");
        GearFactory.init(SquadBattleTest.class.getClassLoader().getResourceAsStream("data/gear.json"));
        EquipmentDistributor.init(meleeWeapons, rangedWeapons, armours);

        // 2. Create squad
        Squad squad = createSquad(SQUAD_TYPE);
        System.out.println("Squad created: " + squad.getDisplayName() + " (" + squad.getSize() + " marines)");

        // 3. Create enemy horde
        Horde horde = USE_CUSTOM_ENEMY
                ? EnemyChaos.generateHorde("enemy_force", ENEMY_STRENGTH, ENEMY_THREAT)
                : EnemyChaos.generateFixedHorde("enemy_force", "chaos_patrol");
        System.out.println("Horde created: " + horde.getSize() + " units");

        // 4. Run battle
        runBattle(squad, horde);
    }

    // ==================== Squad Creation ====================

    private static Squad createSquad(SquadType type) throws Exception {
        List<String> names = loadNames();
        Collections.shuffle(names, new Random());

        Squad squad = new Squad("test_" + type.name().toLowerCase(), type, null, "Test", 10);
        Iterator<String> nameIt = names.iterator();

        switch (type) {
            case TACTICAL_SQUAD -> {
                MarineUnit sgt = createMarine(nameIt.next(), BattleRole.SERGEANT);
                EquipmentDistributor.equipSergeant(sgt);
                EquipmentDistributor.assignArmor(sgt);
                squad.addMarine(sgt);

                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < 9; i++) indices.add(i);
                Collections.shuffle(indices, new Random());
                int standardIdx = indices.get(0);
                int heavyIdx = indices.get(1);

                for (int i = 0; i < 9; i++) {
                    final int idx = i;
                    final boolean replaceStandard = (idx == standardIdx);
                    final boolean replaceHeavy = (idx == heavyIdx);
                    MarineUnit m = createMarine(nameIt.next(), BattleRole.TACTICAL);
                    EquipmentDistributor.equipTacticalMarine(m, replaceStandard, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                    squad.addMarine(m);
                }
            }
            case ASSAULT_SQUAD -> {
                MarineUnit sgt = createMarine(nameIt.next(), BattleRole.SERGEANT);
                EquipmentDistributor.equipSergeant(sgt);
                EquipmentDistributor.assignArmor(sgt);
                squad.addMarine(sgt);

                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < 9; i++) indices.add(i);
                Collections.shuffle(indices, new Random());
                int melee1 = indices.get(0), melee2 = indices.get(1);
                int pistol1 = indices.get(2), pistol2 = indices.get(3);

                for (int i = 0; i < 9; i++) {
                    final int idx = i;
                    final boolean replaceMelee = (idx == melee1 || idx == melee2);
                    final boolean replacePistol = (idx == pistol1 || idx == pistol2);
                    MarineUnit m = createMarine(nameIt.next(), BattleRole.ASSAULT);
                    EquipmentDistributor.equipAssaultMarine(m, replaceMelee, replacePistol);
                    EquipmentDistributor.assignArmor(m);
                    squad.addMarine(m);
                }
            }
            case DEVASTATOR_SQUAD -> {
                MarineUnit sgt = createMarine(nameIt.next(), BattleRole.SERGEANT);
                EquipmentDistributor.equipSergeant(sgt);
                EquipmentDistributor.assignArmor(sgt);
                squad.addMarine(sgt);

                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < 9; i++) indices.add(i);
                Collections.shuffle(indices, new Random());
                Set<Integer> heavyReplacements = new HashSet<>(indices.subList(0, 4));

                for (int i = 0; i < 9; i++) {
                    final int idx = i;
                    final boolean replaceHeavy = heavyReplacements.contains(idx);
                    MarineUnit m = createMarine(nameIt.next(), BattleRole.DEVASTATOR);
                    EquipmentDistributor.equipDevastatorMarine(m, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                    squad.addMarine(m);
                }
            }
            case COMMAND_SQUAD -> {
                MarineUnit cap = createMarine(nameIt.next(), BattleRole.CAPTAIN);
                EquipmentDistributor.equipCaptain(cap);
                EquipmentDistributor.assignArmor(cap);
                squad.addMarine(cap);

                for (int i = 0; i < 9; i++) {
                    MarineUnit m = createMarine(nameIt.next(), BattleRole.VETERAN);
                    EquipmentDistributor.equipVeteran(m);
                    EquipmentDistributor.assignArmor(m);
                    squad.addMarine(m);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported squad type: " + type);
        }

        return squad;
    }

    private static MarineUnit createMarine(String name, BattleRole role) {
        MarineUnit marine = new MarineUnit();
        marine.setName(name);
        marine.setWorldOrigin(WorldType.CIVILIZED);
        marine.setRole(role);
        marine.generateAttributes();
        marine.applyLevels(80 + ThreadLocalRandom.current().nextInt(40));
        return marine;
    }

    private static List<String> loadNames() throws Exception {
        try (InputStream is = SquadBattleTest.class.getClassLoader()
                .getResourceAsStream("data/marine_names.json")) {
            if (is == null) {
                throw new FileNotFoundException("data/marine_names.json not found in resources");
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(json, listType);
        }
    }

    // ==================== Battle ====================

    private static void runBattle(Squad squad, Horde horde) throws Exception {
        int distance = COMBAT_DISTANCE;
        int turn = 0;

        StringBuilder log = new StringBuilder();
        log.append("======= Squad Battle Report =======\n");
        log.append(String.format("Squad: %s (%d marines)\n", squad.getDisplayName(), squad.getSize()));
        log.append(String.format("Enemy: %s (%d units)\n", horde.getId(), horde.getSize()));
        log.append(String.format("Starting distance: %d\n\n", distance));

        StringBuilder turnLog = new StringBuilder();
        Map<String, Integer> totalMarineWeaponDamage = new LinkedHashMap<>();
        Map<String, Integer> totalMarineKillsByType = new LinkedHashMap<>();
        int totalMarineDown = 0;

        // Pre-battle: print squad equipment
        turnLog.append("--- Squad Equipment ---\n");
        for (MarineUnit m : squad.getAllMarines()) {
            String rh = m.getRightHand() != null ? m.getRightHand().getName() : "—";
            String lh = m.getLeftHand() != null ? m.getLeftHand().getName() : "—";
            String arm = m.getArmorKit() != null ? m.getArmorKit().getName() : "—";
            turnLog.append(String.format("  %-10s [%s] RH: %-16s LH: %-16s Armour: %s\n",
                    m.getName(), m.getRole(), rh, lh, arm));
        }
        turnLog.append("\n");

        // Print enemy composition
        turnLog.append("--- Enemy Composition ---\n");
        Map<String, Long> enemyCount = new LinkedHashMap<>();
        for (var unit : horde.getUnits()) {
            enemyCount.merge(unit.getTypeId(), 1L, Long::sum);
        }
        for (var entry : enemyCount.entrySet()) {
            turnLog.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
        }
        turnLog.append("\n");

        while (distance >= 0 && !horde.isEmpty() && getAvailableCount(squad) > 0) {
            turn++;

            turnLog.append(String.format("--- Turn %d (Distance: %d) ---\n", turn, distance));

            // --- Player Phase ---
            if (distance > 0) {
                // Check if jump pack available
                boolean hasJumpPack = squad.getAllMarines().stream()
                        .anyMatch(m -> m.isAvailable() && m.getCurrentWounds() > 0
                                && m.getExtraSlot1() != null
                                && m.getExtraSlot1().hasTrait("jump_pack"));

                if (hasJumpPack && distance <= 2) {
                    // Death from above
                    CombatReport howReport = BattleSystem.squadJumpPackAssault(squad, horde);
                    printMarineAttackDetails(howReport, turnLog, "Hammer of Wrath");
                    recordMarineResults(howReport, totalMarineWeaponDamage, totalMarineKillsByType, turnLog);

                    horde.removeDeadUnits();

                    // Enter melee
                    distance = 0;

                    CombatReport meleeReport = BattleSystem.squadMeleeAttack(squad, horde);
                    printMarineAttackDetails(meleeReport, turnLog, "Marines Charge Melee");
                    recordMarineResults(meleeReport, totalMarineWeaponDamage, totalMarineKillsByType, turnLog);
                } else {
                    // Ranged
                    CombatReport report = BattleSystem.squadRangedAttack(squad, horde, distance, false);
                    printMarineAttackDetails(report, turnLog, "Marines Ranged");
                    recordMarineResults(report, totalMarineWeaponDamage, totalMarineKillsByType, turnLog);
                }
            } else {
                CombatReport report = BattleSystem.squadMeleeAttack(squad, horde);
                printMarineAttackDetails(report, turnLog, "Marines Melee");
                recordMarineResults(report, totalMarineWeaponDamage, totalMarineKillsByType, turnLog);
            }


            horde.removeDeadUnits();

            // --- Enemy Phase ---
            if (!horde.isEmpty()) {
                CombatReport enemyReport = BattleSystem.hordeAttacksSquad(horde, squad, distance);
                printEnemyAttackDetails(enemyReport, turnLog);
                totalMarineDown += enemyReport.getTotalCasualties();
                for (String casualty : enemyReport.getMarineCasualties()) {
                    turnLog.append(String.format("    [CASUALTY] %s\n", casualty));
                }
            }

            // Print squad health
            printSquadHealth(squad, turnLog);

            // Print enemy remaining
            turnLog.append(String.format("  Enemies remaining: %d\n\n", horde.getSize()));

            // Move closer
            if (distance > 0) distance--;

            if (turn > 50) break;
        }

        log.append(turnLog);

        // === Weapon Damage Summary ===
        log.append("======= Marine Weapon Damage =======\n");
        for (var entry : totalMarineWeaponDamage.entrySet()) {
            log.append(String.format("  %s: %d total damage\n", entry.getKey(), entry.getValue()));
        }

        // === Kill Summary ===
        log.append("\n======= Kills by Enemy Type =======\n");
        int totalKills = 0;
        for (var entry : totalMarineKillsByType.entrySet()) {
            log.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            totalKills += entry.getValue();
        }

        // === Outcome ===
        log.append("\n======= Battle Outcome =======\n");
        if (horde.isEmpty()) {
            log.append("VICTORY: Enemy horde destroyed!\n");
        } else {
            log.append("DEFEAT: Squad overrun!\n");
        }
        log.append(String.format("Turns fought: %d\n", turn));
        log.append(String.format("Marines combat-ready: %d/%d\n", getAvailableCount(squad), squad.getSize()));
        log.append(String.format("Enemies remaining: %d\n", horde.getSize()));
        log.append(String.format("Marines down: %d\n", totalMarineDown));
        log.append(String.format("Enemies killed: %d\n", totalKills));

        // Write to file
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream("battle_report.txt"), StandardCharsets.UTF_8))) {
            writer.print(log);
        }

        System.out.println("Done! Battle report written to battle_report.txt");
    }

    private static void recordMarineResults(CombatReport report,
                                            Map<String, Integer> weaponDmg,
                                            Map<String, Integer> killsByType,
                                            StringBuilder turnLog) {
        for (AttackReport rec : report.getAttackRecords()) {
            weaponDmg.merge(rec.getWeaponKey(), rec.getDamage(), Integer::sum);
        }
        Map<String, Long> kills = report.getKillsByTargetType();
        for (var entry : kills.entrySet()) {
            killsByType.merge(entry.getKey(), entry.getValue().intValue(), Integer::sum);
        }
        for (String ev : report.getOverheatEvents()) {
            turnLog.append("  [OVERHEAT] ").append(ev).append("\n");
        }
    }

    private static int getAvailableCount(Squad squad) {
        int count = 0;
        for (MarineUnit m : squad.getAllMarines()) {
            if (m.isAvailable() && m.getCurrentWounds() > 0) count++;
        }
        return count;
    }

    // ==================== Data Loading ====================

    private static Map<String, WeaponItem> loadWeapons(String path) throws Exception {
        try (InputStream is = SquadBattleTest.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException(path + " not found");
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, WeaponItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    private static Map<String, ArmourItem> loadArmours(String path) throws Exception {
        try (InputStream is = SquadBattleTest.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException(path + " not found");
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, ArmourItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    private static void printMarineAttackDetails(CombatReport report, StringBuilder turnLog, String phaseLabel) {
        turnLog.append("  ").append(phaseLabel).append(":\n");

        Map<String, List<AttackReport>> byMarine = report.getAttackRecords().stream()
                .collect(Collectors.groupingBy(
                        AttackReport::getAttackerName,
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (var entry : byMarine.entrySet()) {
            String name = entry.getKey();
            String role = entry.getValue().get(0).getAttackerRole();

            Map<String, List<AttackReport>> byWeapon = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            ar -> ar.getWeaponName() + (ar.isMelee() ? "(M)" : ar.getWeaponSlot().equals("LEFT_HAND") ? "(L)" : "(R)"),
                            LinkedHashMap::new,
                            Collectors.toList()));

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("    %-10s [%s]", name, role));
            for (var we : byWeapon.entrySet()) {
                int dmg = we.getValue().stream().mapToInt(AttackReport::getDamage).sum();
                long kills = we.getValue().stream().filter(AttackReport::isKilled).count();
                if (dmg > 0) {
                    sb.append(String.format(" %s: %d dmg %d kill%s",
                            we.getKey(), dmg, kills, kills == 1 ? "" : "s"));
                }
            }
            turnLog.append(sb.toString()).append("\n");
        }

        int totalDmg = report.getAttackRecords().stream().mapToInt(AttackReport::getDamage).sum();
        long totalKills = report.getAttackRecords().stream().filter(AttackReport::isKilled).count();
        turnLog.append(String.format("    >> Squad total: %d damage, %d kills\n", totalDmg, totalKills));
    }

    private static void printEnemyAttackDetails(CombatReport report, StringBuilder turnLog) {
        turnLog.append("  Enemy Phase:\n");

        Map<String, List<AttackReport>> byType = report.getAttackRecords().stream()
                .collect(Collectors.groupingBy(
                        AttackReport::getAttackerName,
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (var entry : byType.entrySet()) {
            String type = entry.getKey();
            int dmg = entry.getValue().stream().mapToInt(AttackReport::getDamage).sum();
            long kills = entry.getValue().stream().filter(AttackReport::isKilled).count();
            int hits = entry.getValue().size();
            turnLog.append(String.format("    %s: %d hits, %d total damage, %d marine%s down\n",
                    type, hits, dmg, kills, kills == 1 ? "" : "s"));
        }
    }

    private static void printSquadHealth(Squad squad, StringBuilder turnLog) {
        turnLog.append("  Squad health:\n");
        for (MarineUnit m : squad.getAllMarines()) {
            String name = m.getName();
            String status;
            if (m.getCurrentWounds() <= 0) {
                status = "DOWN";
            } else {
                int hpPercent = Math.round((float) m.getCurrentWounds() / m.getWounds() * 100);
                status = String.format("%d%% (%d/%d)", hpPercent, m.getCurrentWounds(), m.getWounds());
            }
            turnLog.append(String.format("    %-10s %s\n", name, status));
        }
    }

}
