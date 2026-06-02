package org.core.combat;

import java.util.*;
import java.util.stream.Collectors;

public class CombatReport {

    private final List<AttackReport> attackRecords = new ArrayList<>();
    private final List<String> overheatEvents = new ArrayList<>();
    private final List<String> marineCasualties = new ArrayList<>();
    private final List<String> enemyKillSummary = new ArrayList<>();

    private int totalMarineKills;
    private int totalMarineCasualties;
    private String turnId;

    public void setTurnId(String turnId) { this.turnId = turnId; }

    public void addAttackRecord(AttackReport record) {
        attackRecords.add(record);
        if (record.isKilled()) {
            totalMarineKills++;
        }
    }

    public void addOverheat(String sourceName, int damage) {
        overheatEvents.add(sourceName + " overheated for " + damage);
    }

    public void addMarineCasualty(String marineName) {
        marineCasualties.add(marineName);
        totalMarineCasualties++;
    }

    public void addEnemyKillSummary(String summary) {
        enemyKillSummary.add(summary);
    }

    // ==================== Query Methods ====================

    public List<AttackReport> getAttackRecords() {
        return Collections.unmodifiableList(attackRecords);
    }

    /**
     * Get total damage grouped by attacker name + weapon name.
     */
    public Map<String, Integer> getDamageByWeaponKey() {
        return attackRecords.stream()
                .collect(Collectors.groupingBy(
                        AttackReport::getWeaponKey,
                        LinkedHashMap::new,
                        Collectors.summingInt(AttackReport::getDamage)
                ));
    }

    /**
     * Get total kill count by target type.
     */
    public Map<String, Long> getKillsByTargetType() {
        return attackRecords.stream()
                .filter(AttackReport::isKilled)
                .collect(Collectors.groupingBy(
                        AttackReport::getTargetType,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    /**
     * Get total damage dealt by each marine.
     */
    public Map<String, Integer> getDamageByMarine() {
        return attackRecords.stream()
                .collect(Collectors.groupingBy(
                        AttackReport::getAttackerName,
                        LinkedHashMap::new,
                        Collectors.summingInt(AttackReport::getDamage)
                ));
    }

    public int getTotalKills() { return totalMarineKills; }
    public int getTotalCasualties() { return totalMarineCasualties; }
    public List<String> getOverheatEvents() { return Collections.unmodifiableList(overheatEvents); }
    public List<String> getMarineCasualties() { return Collections.unmodifiableList(marineCasualties); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Combat Report ===\n");
        sb.append("Kills: ").append(totalMarineKills).append("\n");

        Map<String, Long> killsByType = getKillsByTargetType();
        for (Map.Entry<String, Long> entry : killsByType.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        if (!marineCasualties.isEmpty()) {
            sb.append("Casualties: ").append(totalMarineCasualties).append("\n");
            for (String name : marineCasualties) {
                sb.append("  ").append(name).append("\n");
            }
        }

        if (!overheatEvents.isEmpty()) {
            sb.append("Overheat:\n");
            for (String ev : overheatEvents) {
                sb.append("  ").append(ev).append("\n");
            }
        }

        sb.append("\nWeapon Damage Summary:\n");
        Map<String, Integer> dmgByWeapon = getDamageByWeaponKey();
        for (Map.Entry<String, Integer> entry : dmgByWeapon.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public String getTurnId() {
        return turnId;
    }
}
