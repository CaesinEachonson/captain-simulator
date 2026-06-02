package org.core.entity.units.squad;

import org.core.entity.units.company.Company;
import org.core.entity.units.marine.MarineUnit;
import org.core.entity.units.marine.enums.BattleRole;
import org.core.mission.Mission;

import java.util.*;

public class Squad {
    private final String id;
    private final SquadType squadType;
    private final Company parentCompany;
    private String honorTitle;
    private MarineUnit sergeant;
    private final List<MarineUnit> members;

    private Mission currentMission;
    private boolean isAvailable;

    // package-private constructor — only SquadFactory calls this
    public Squad(String id, SquadType squadType, Company parentCompany, String honorTitle, int maxMembers) {
        this.id = id;
        this.squadType = squadType;
        this.parentCompany = parentCompany;
        this.honorTitle = honorTitle;
        this.members = new ArrayList<>(maxMembers - 1);
        this.isAvailable = true;
    }

    public int getMaxMembers() {
        return switch (squadType) {
            case TERMINATOR_SQUAD, TERMINATOR_ASSAULT_SQUAD -> 5;
            default -> 10;
        };
    }

    public String getDisplayName() {
        String typePrefix = switch (squadType) {
            case TACTICAL_SQUAD -> "Tactical Squad";
            case ASSAULT_SQUAD -> "Assault Squad";
            case DEVASTATOR_SQUAD -> "Devastator Squad";
            case STERNGUARD_SQUAD -> "Sternguard Squad";
            case VANGUARD_SQUAD -> "Vanguard Squad";
            case TERMINATOR_SQUAD -> "Terminator Squad";
            case TERMINATOR_ASSAULT_SQUAD -> "Assault Terminator Squad";
            case SCOUT_SQUAD -> "Scout Squad";
            case COMMAND_SQUAD -> "Command Squad";
        };
        if (honorTitle != null && !honorTitle.isEmpty()) {
            return typePrefix + " " + id + " \"" + honorTitle + "\"";
        }
        return typePrefix + " " + id;
    }

    public void addMarine(MarineUnit marine) {
        if (sergeant == null) {
            if (squadType != SquadType.COMMAND_SQUAD) {
                marine.setRole(BattleRole.SERGEANT);
            }
            this.sergeant = marine;
        } else {
            members.add(marine);
        }
        marine.setSquadId(this.id);
//        marine.setCompanyId(parentCompany.getName());
    }


    public void removeMarine(MarineUnit marine) {
        if (sergeant == marine) {
            if (!members.isEmpty()) {
                sergeant = members.remove(0);
                sergeant.setRole(BattleRole.SERGEANT);
            } else {
                sergeant = null;
            }
        } else {
            members.remove(marine);
        }
    }

    public int getSize() {
        int count = 0;
        if (sergeant != null) count++;
        count += members.size();
        return count;
    }

    public List<MarineUnit> getAllMarines() {
        List<MarineUnit> all = new ArrayList<>(getSize());
        if (sergeant != null) all.add(sergeant);
        all.addAll(members);
        return all;
    }

    public boolean isAvailable() {
        return isAvailable && currentMission == null;
    }

    public void assignMission(Mission mission) { this.currentMission = mission; }
    public void completeMission() { this.currentMission = null; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    // Getters
    public String getId() { return id; }
    public SquadType getSquadType() { return squadType; }
    public Company getParentCompany() { return parentCompany; }
    public String getHonorTitle() { return honorTitle; }
    public void setHonorTitle(String honorTitle) { this.honorTitle = honorTitle; }
    public MarineUnit getSergeant() { return sergeant; }
    public List<MarineUnit> getMembers() { return Collections.unmodifiableList(members); }
    public Mission getCurrentMission() { return currentMission; }
    public int getAvailableCount() {
        int count = 0;
        if (sergeant != null && sergeant.isAvailable() && sergeant.getCurrentWounds() > 0) count++;
        for (MarineUnit m : members) {
            if (m.isAvailable() && m.getCurrentWounds() > 0) count++;
        }
        return count;
    }

}
