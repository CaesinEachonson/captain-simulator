package org.core.entity.units.company;

import org.core.entity.units.chapter.Chapter;
import org.core.entity.units.marine.MarineUnit;
import org.core.mission.Mission;
import org.core.entity.units.squad.Squad;
import org.core.entity.factory.SquadFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Company {
    private String name;
    private String commanderTitle;
    private Chapter parentChapter;

    private List<Squad> squads;

    private Armory armory;
    private ResourcePool resources;
    private ProductionManager production;

    private int experience;
    private int renown;
    private int casualtiesTotal;
    private int battlesFought;

    public Company(String name, Chapter chapter) {
        this.name = name;
        this.parentChapter = chapter;
        this.squads = new ArrayList<>(10);
        this.armory = new Armory();
        this.resources = new ResourcePool();
        this.production = new ProductionManager();
        this.squads = SquadFactory.createBattleCompanySquads(this);
    }

    /**
     * Allocate a marine to a specific squad.
     */
    public void assignMarineToSquad(int squadIndex, MarineUnit marine) {
        squads.get(squadIndex).addMarine(marine);
    }

    /**
     * Deploy a squad to a mission.
     */
    public void deploySquad(int squadIndex, Mission mission) {
        Squad squad = squads.get(squadIndex);
        if (squad.isAvailable()) {
            squad.assignMission(mission);
        }
    }

    /**
     * Total number of marines across all squads.
     */
    public int getTotalMarines() {
        return squads.stream().mapToInt(Squad::getSize).sum();
    }

    /**
     * Get all marines in this company (from all squads).
     */
    public List<MarineUnit> getAllMarines() {
        return squads.stream()
                .flatMap(squad -> squad.getAllMarines().stream())
                .toList();
    }

    // ==================== Getters ====================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCommanderTitle() { return commanderTitle; }
    public void setCommanderTitle(String commanderTitle) { this.commanderTitle = commanderTitle; }

    public Chapter getParentChapter() { return parentChapter; }

    public List<Squad> getSquads() { return Collections.unmodifiableList(squads); }

    public Armory getArmory() { return armory; }
    public ResourcePool getResources() { return resources; }
    public ProductionManager getProduction() { return production; }

    public int getExperience() { return experience; }
    public void addExperience(int exp) { this.experience += exp; }

    public int getRenown() { return renown; }
    public void addRenown(int renown) { this.renown += renown; }

    public int getCasualtiesTotal() { return casualtiesTotal; }
    public void addCasualties(int count) { this.casualtiesTotal += count; }

    public int getBattlesFought() { return battlesFought; }
    public void incrementBattlesFought() { this.battlesFought++; }
}
