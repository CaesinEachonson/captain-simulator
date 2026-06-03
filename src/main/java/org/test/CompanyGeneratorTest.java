package org.test;

import org.captainsim.attribute.WorldType;
import org.captainsim.company.Company;
import org.captainsim.company.CompanyType;
import org.captainsim.item.ArmourItem;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.unit.marine.BattleRole;
import org.captainsim.item.WeaponItem;
import org.captainsim.squad.Squad;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.captainsim.util.EquipmentDistributor;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CompanyGeneratorTest {

    private static final WorldType DEFAULT_WORLD = WorldType.CIVILIZED;
    private static final CompanyType COMPANY_TYPE = CompanyType.THIRD_COMPANY;

    public static void main(String[] args) throws Exception {
        // 0. Load weapon data
        Map<String, WeaponItem> meleeWeapons = loadWeapons("data/weapons_melee.json");
        Map<String, WeaponItem> rangedWeapons = loadWeapons("data/weapons_ranged.json");
        Map<String, ArmourItem> armours = loadArmours();
        EquipmentDistributor.init(meleeWeapons, rangedWeapons, armours);
        System.out.println("Loaded " + meleeWeapons.size() + " melee weapons, " + rangedWeapons.size() + " ranged weapons.");

        // 1. Load names
        List<String> names = loadNames();
        System.out.println("Loaded " + names.size() + " names.");
        Collections.shuffle(names, new Random());

        // 2. Create company with squads
        Company company = new Company("3rd Company", null);
        List<Squad> squads = company.getSquads();
        Iterator<String> nameIt = names.iterator();

        // 3. Assign marines and equipment to each squad
        // Command Squad (index 0)
        Squad commandSquad = squads.get(0);

        equipAndAdd(commandSquad, nameIt.next(), BattleRole.CAPTAIN, m -> {
            EquipmentDistributor.equipCaptain(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(commandSquad, nameIt.next(), BattleRole.CHAPLAIN, m -> {
            EquipmentDistributor.equipChaplain(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(commandSquad, nameIt.next(), BattleRole.TECHMARINE, m -> {
            EquipmentDistributor.equipTechmarine(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(commandSquad, nameIt.next(), BattleRole.APOTHECARY, m -> {
            EquipmentDistributor.equipApothecary(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(commandSquad, nameIt.next(), BattleRole.COMPANY_CHAMPION, m -> {
            EquipmentDistributor.equipCompanyChampion(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(commandSquad, nameIt.next(), BattleRole.ANCIENT, m -> {
            EquipmentDistributor.equipAncient(m);
            EquipmentDistributor.assignArmor(m);
        });
        for (int i = 0; i < 4; i++) {
            equipAndAdd(commandSquad, nameIt.next(), BattleRole.VETERAN, m -> {
                EquipmentDistributor.equipVeteran(m);
                EquipmentDistributor.assignArmor(m);
            });
        }

        // Tactical Squads (indices 1-5)
        for (int squadIdx = 1; squadIdx <= 5; squadIdx++) {
            Squad squad = squads.get(squadIdx);

            // Sergeant
            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            // Determine which 2 of the 9 get replacements
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());
            int standardReplacementIdx = indices.get(0);
            int heavyReplacementIdx = indices.get(1);

            for (int i = 0; i < 9; i++) {
                final int idx = i;
                final boolean replaceStandard = (idx == standardReplacementIdx);
                final boolean replaceHeavy = (idx == heavyReplacementIdx);
                equipAndAdd(squad, nameIt.next(), BattleRole.TACTICAL, m -> {
                    EquipmentDistributor.equipTacticalMarine(m, replaceStandard, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // Assault Squads (indices 6-7)
        for (int squadIdx = 6; squadIdx <= 7; squadIdx++) {
            Squad squad = squads.get(squadIdx);

            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());

            int pistolReplacement1 = indices.get(0);
            int pistolReplacement2 = indices.get(1);
            int meleeReplacement1 = indices.get(2);
            int meleeReplacement2 = indices.get(3);

            for (int i = 0; i < 9; i++) {
                final int idx = i;
                final boolean replaceMelee = (idx == meleeReplacement1 || idx == meleeReplacement2);
                final boolean replacePistol = (idx == pistolReplacement1 || idx == pistolReplacement2);
                equipAndAdd(squad, nameIt.next(), BattleRole.ASSAULT, m -> {
                    EquipmentDistributor.equipAssaultMarine(m, replaceMelee, replacePistol);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // Devastator Squads (indices 8-9)
        for (int squadIdx = 8; squadIdx <= 9; squadIdx++) {
            Squad squad = squads.get(squadIdx);

            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());

            Set<Integer> heavyReplacements = new HashSet<>(indices.subList(0, 4));

            for (int i = 0; i < 9; i++) {
                final int idx = i;
                final boolean replaceHeavy = heavyReplacements.contains(idx);
                equipAndAdd(squad, nameIt.next(), BattleRole.DEVASTATOR, m -> {
                    EquipmentDistributor.equipDevastatorMarine(m, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // 4. Apply lightning claw dual-wield and random hand swap
        for (Squad squad : squads) {
            for (MarineUnit m : squad.getAllMarines()) {
                EquipmentDistributor.applyLightningClawDualWield(m);
                EquipmentDistributor.maybeSwapHands(m);
            }
        }

        // 5. Output grouped by squad
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream("company_report.txt"), StandardCharsets.UTF_8))) {

            writer.printf("======= 3rd Company Roster =======%n");
            writer.printf("World Origin: %s%n%n", DEFAULT_WORLD.name());

            int grandTotal = 0;
            for (Squad squad : squads) {
                List<MarineUnit> marines = squad.getAllMarines();
                if (marines.isEmpty()) continue;

                writer.printf("=== %s === (%d marines)%n",
                        squad.getDisplayName(), marines.size());
                writer.printf("%-3s %-20s %-20s %-16s %-16s %-20s %s%n",
                        "#", "Name", "Role", "Right Hand", "Left Hand", "Armour",
                        "WS  BS   S   T  Ag Int  WP Fel  Lv");

                int idx = 1;
                for (MarineUnit m : marines) {
                    String rh = m.getRightHand() != null ? m.getRightHand().getName() : "—";
                    String lh = m.getLeftHand() != null ? m.getLeftHand().getName() : "—";
                    String armour = m.getArmorKit() != null ? m.getArmorKit().getName() : "—";
                    writer.printf("%-3d %-20s %-20s %-16s %-16s %-20s %3d %3d %3d %3d %3d %3d %3d %3d %3d%n",
                            idx++,
                            m.getName(),
                            m.getRole(),
                            rh, lh, armour,
                            m.getWs(), m.getBs(), m.getS(), m.getT(),
                            m.getAg(), m.getIntelligence(), m.getWp(), m.getFel(),
                            m.getLevel());
                }
                writer.println();
                grandTotal += marines.size();
            }

            writer.printf("Total Marines: %d%n", grandTotal);
        }

        System.out.println("Done! Output written to company_report.txt");
    }

    // ==================== Helper ====================

    private static void equipAndAdd(Squad squad, String name, BattleRole role,
                                    java.util.function.Consumer<MarineUnit> equipper) {
        MarineUnit marine = createMarine(name, role);
        equipper.accept(marine);
        squad.addMarine(marine);
    }

    private static MarineUnit createMarine(String name, BattleRole role) {
        MarineUnit marine = new MarineUnit();
        marine.setName(name);
        marine.setWorldOrigin(DEFAULT_WORLD);
        marine.setRole(role);
        marine.generateAttributes();
        int minLevel = COMPANY_TYPE.getMinLevelForRole(role);
        int maxLevel = COMPANY_TYPE.getMaxLevelForRole(role);
        int level = ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);
        marine.applyLevels(level);
        return marine;
    }

    // ==================== Data Loading ====================

    private static Map<String, WeaponItem> loadWeapons(String path) throws Exception {
        try (InputStream is = CompanyGeneratorTest.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException(path + " not found in resources");
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, WeaponItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    private static Map<String, ArmourItem> loadArmours() throws Exception {
        try (InputStream is = CompanyGeneratorTest.class.getClassLoader().getResourceAsStream("data/armour.json")) {
            if (is == null) {
                throw new FileNotFoundException("data/armour.json" + " not found in resources");
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, ArmourItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    private static List<String> loadNames() throws Exception {
        try (InputStream is = CompanyGeneratorTest.class.getClassLoader()
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
}
