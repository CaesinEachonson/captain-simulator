package org.captainsim;

import org.captainsim.attribute.WorldType;
import org.captainsim.company.Company;
import org.captainsim.company.CompanyType;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.BattleRole;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.util.EquipmentDistributor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class CompanyGenerator {

    private static final WorldType DEFAULT_WORLD = WorldType.CIVILIZED;
    private static final CompanyType COMPANY_TYPE = CompanyType.THIRD_COMPANY;

    /**
     * Generates a full company: 10 squads, 100 marines, all equipped.
     */
    public static Company generate() throws Exception {
        List<String> names = GameData.loadNames();
        Collections.shuffle(names, new Random());
        Iterator<String> nameIt = names.iterator();

        Company company = new Company("3rd Company", null);
        List<Squad> squads = company.getSquads();

        // Command Squad (index 0)
        Squad cmd = squads.get(0);
        equipAndAdd(cmd, nameIt.next(), BattleRole.CAPTAIN, m -> {
            EquipmentDistributor.equipCaptain(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(cmd, nameIt.next(), BattleRole.CHAPLAIN, m -> {
            EquipmentDistributor.equipChaplain(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(cmd, nameIt.next(), BattleRole.TECHMARINE, m -> {
            EquipmentDistributor.equipTechmarine(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(cmd, nameIt.next(), BattleRole.APOTHECARY, m -> {
            EquipmentDistributor.equipApothecary(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(cmd, nameIt.next(), BattleRole.COMPANY_CHAMPION, m -> {
            EquipmentDistributor.equipCompanyChampion(m);
            EquipmentDistributor.assignArmor(m);
        });
        equipAndAdd(cmd, nameIt.next(), BattleRole.ANCIENT, m -> {
            EquipmentDistributor.equipAncient(m);
            EquipmentDistributor.assignArmor(m);
        });
        for (int i = 0; i < 4; i++) {
            equipAndAdd(cmd, nameIt.next(), BattleRole.VETERAN, m -> {
                EquipmentDistributor.equipVeteran(m);
                EquipmentDistributor.assignArmor(m);
            });
        }

        // Tactical Squads (indices 1-5)
        for (int idx = 1; idx <= 5; idx++) {
            Squad squad = squads.get(idx);

            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());
            int specIdx = indices.get(0);
            int heavyIdx = indices.get(1);

            for (int i = 0; i < 9; i++) {
                final int fi = i;
                final boolean replaceStandard = (fi == specIdx);
                final boolean replaceHeavy = (fi == heavyIdx);
                equipAndAdd(squad, nameIt.next(), BattleRole.TACTICAL, m -> {
                    EquipmentDistributor.equipTacticalMarine(m, replaceStandard, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // Assault Squads (indices 6-7)
        for (int idx = 6; idx <= 7; idx++) {
            Squad squad = squads.get(idx);

            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());
            int p1 = indices.get(0), p2 = indices.get(1);
            int m1 = indices.get(2), m2 = indices.get(3);

            for (int i = 0; i < 9; i++) {
                final int fi = i;
                final boolean replacePistol = (fi == p1 || fi == p2);
                final boolean replaceMelee = (fi == m1 || fi == m2);
                equipAndAdd(squad, nameIt.next(), BattleRole.ASSAULT, m -> {
                    EquipmentDistributor.equipAssaultMarine(m, replaceMelee, replacePistol);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // Devastator Squads (indices 8-9)
        for (int idx = 8; idx <= 9; idx++) {
            Squad squad = squads.get(idx);

            equipAndAdd(squad, nameIt.next(), BattleRole.SERGEANT, m -> {
                EquipmentDistributor.equipSergeant(m);
                EquipmentDistributor.assignArmor(m);
            });

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 9; i++) indices.add(i);
            Collections.shuffle(indices, new Random());
            Set<Integer> heavySet = new HashSet<>(indices.subList(0, 4));

            for (int i = 0; i < 9; i++) {
                final int fi = i;
                final boolean replaceHeavy = heavySet.contains(fi);
                equipAndAdd(squad, nameIt.next(), BattleRole.DEVASTATOR, m -> {
                    EquipmentDistributor.equipDevastatorMarine(m, replaceHeavy);
                    EquipmentDistributor.assignArmor(m);
                });
            }
        }

        // Post-processing
        for (Squad squad : squads) {
            for (MarineUnit m : squad.getAllMarines()) {
                EquipmentDistributor.applyLightningClawDualWield(m);
                EquipmentDistributor.maybeSwapHands(m);
            }
        }

        return company;
    }

    private static void equipAndAdd(Squad squad, String name, BattleRole role,
                                    Consumer<MarineUnit> equipper) {
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
}
