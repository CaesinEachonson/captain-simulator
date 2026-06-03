package test;

import org.captainsim.unit.enemy.EnemyUnit;
import org.captainsim.unit.enemy.Horde;
import org.captainsim.unit.enemy.factions.EnemyChaos;

import java.util.*;

public class HordeGenerator {

    public static void main(String[] args) {
        // 低难度（1），强度 200
        System.out.println("=== Difficulty 1, Budget 200 ===");
        printHorde(EnemyChaos.generateHorde("low", 1000, 1));

        // 中难度（5），强度 200
        System.out.println("\n=== Difficulty 4, Budget 200 ===");
        printHorde(EnemyChaos.generateHorde("mid", 1000, 4));

        // 高难度（10），强度 200
        System.out.println("\n=== Difficulty 7, Budget 200 ===");
        printHorde(EnemyChaos.generateHorde("high", 1000, 7));
    }

    private static void printHorde(Horde horde) {
        System.out.println("Total units: " + horde.getSize());
        Map<String, Integer> countByType = new LinkedHashMap<>();
        Map<String, Integer> countByCategory = new LinkedHashMap<>();
        for (EnemyUnit unit : horde.getUnits()) {
            countByType.merge(unit.getTypeId(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : countByType.entrySet()) {
            System.out.printf("  %-20s x %d%n", entry.getKey(), entry.getValue());
        }
    }
}
