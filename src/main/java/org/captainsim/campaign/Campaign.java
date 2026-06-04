package org.captainsim.campaign;

import org.captainsim.GameData;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.BattleRole;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.util.Dice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Campaign {

    private String planetName;
    private int round;
    private int imperialVP;
    private int enemyVP;
    private int victoryThreshold;
    private int intensity;
    private boolean active;
    private boolean playerWon;

    private final List<CampaignMission> missions;
    private final CampaignLog log;

    private final Set<String> deployedSquadIds = new HashSet<>();

    public Campaign(String planetName, int intensity) {
        this.planetName = planetName;
        this.intensity = intensity;
        this.round = 1;
        this.imperialVP = 0;
        this.enemyVP = 0;
        this.victoryThreshold = 100;
        this.active = true;
        this.missions = new ArrayList<>();
        this.log = new CampaignLog();
    }

    // ===== Round Management =====

    public void nextRound() {
        round++;
        deployedSquadIds.clear();
        missions.clear();
    }

    public boolean isVictoryReached() {
        return imperialVP >= victoryThreshold || enemyVP >= victoryThreshold;
    }

    public void endCampaign(boolean playerWon) {
        this.active = false;
        this.playerWon = playerWon;
    }

    // ===== Getters & Setters =====

    public String getPlanetName() { return planetName; }
    public int getRound() { return round; }
    public int getImperialVP() { return imperialVP; }
    public void addImperialVP(int vp) { this.imperialVP += vp; }
    public int getEnemyVP() { return enemyVP; }
    public void addEnemyVP(int vp) { this.enemyVP += vp; }
    public int getVictoryThreshold() { return victoryThreshold; }
    public int getIntensity() { return intensity; }
    public boolean isActive() { return active; }
    public boolean isPlayerWon() { return playerWon; }
    public List<CampaignMission> getMissions() { return missions; }
    public CampaignLog getLog() { return log; }

    public Set<String> getDeployedSquadIds() { return deployedSquadIds; }

    public boolean isSquadDeployed(String squadId) {
        return deployedSquadIds.contains(squadId);
    }

    public void markSquadDeployed(String squadId) {
        deployedSquadIds.add(squadId);
    }

    public void generateMissionsForRound() {
        missions.clear();

        // 每回合生成 2~3 个任务
        int count = 2 + ThreadLocalRandom.current().nextInt(2);

        for (int i = 0; i < count; i++) {
            CampaignMission cm = new CampaignMission(
                    "mission_" + round + "_" + (i + 1),
                    generateMissionName(),
                    randomMissionType(),
                    generateDifficulty()
            );
//            cm.setNarrativeDescription(generateNarrative(cm));
//            cm.setIntelligenceSummary(generateIntel(cm));
            cm.setNarrativeDescription("test Narrative");
            cm.setIntelligenceSummary("test intelligence");
            missions.add(cm);
        }

        // log
        log.addEntry(round, "Week " + round + " begins. " + count + " missions detected.");
    }
    private String generateMissionName() {
        String[] names = {"Hive Purge", "Bridgehead Assault", "Relic Retrieval",
                "Outpost Defense", "Cultist Sweep", "Supply Run",
                "Artillery Silence", "Comms Relay"};
        return names[ThreadLocalRandom.current().nextInt(names.length)];
    }
    private MissionType randomMissionType() {
        MissionType[] types = MissionType.values();
        return types[ThreadLocalRandom.current().nextInt(types.length)];
    }
    private int generateDifficulty() {
        // Randomize difficulty based on war intensity
        int base = intensity;
        int roll = ThreadLocalRandom.current().nextInt(-1, 3);
        return Math.max(1, Math.min(7, base + roll));
    }

    public boolean allMissionsResolved() {
        return missions.stream().allMatch(CampaignMission::isResolved);
    }

    public void advanceRound() {
        processPostRoundRecovery();
        nextRound();
        generateMissionsForRound();
    }

    public void processPostRoundRecovery() {
        for (Squad squad : GameData.getInstance().getSquads()) {
            boolean hasApothecary = squadHasApothecary(squad);
            MarineUnit apothecary = hasApothecary ? findApothecary(squad) : null;
            for (MarineUnit m : squad.getAllMarines()) {
                if (m.getCurrentWounds() <= 0) {
                    if (apothecary != null) {
                        int intel = apothecary.getIntelligence();
                        int threshold = intel / 2;
                        int roll = Dice.rollD100();
                        if (roll <= threshold) {
                            m.setCurrentWounds(1);
                            m.setAvailable(true);
                            log.addEntry(round, "Apothecary " + apothecary.getName()
                                    + " revived " + m.getName() + " (rolled " + roll + "/" + threshold + ").");
                        } else {
                            log.addEntry(round, "Apothecary " + apothecary.getName()
                                    + " failed to revive " + m.getName() + " (rolled " + roll + "/" + threshold + ").");
                        }
                    }
                } else if (m.getCurrentWounds() < m.getWounds()) {
                    int missing = m.getWounds() - m.getCurrentWounds();
                    int heal = Math.max(1, (int) Math.ceil(missing * 0.5));
                    m.setCurrentWounds(Math.min(m.getWounds(), m.getCurrentWounds() + heal));
                }
            }
        }
    }
    private boolean squadHasApothecary(Squad squad) {
        return squad.getAllMarines().stream()
                .anyMatch(m -> m.getRole() == BattleRole.APOTHECARY);
    }
    private MarineUnit findApothecary(Squad squad) {
        return squad.getAllMarines().stream()
                .filter(m -> m.getRole() == BattleRole.APOTHECARY)
                .findFirst()
                .orElse(null);
    }

}
