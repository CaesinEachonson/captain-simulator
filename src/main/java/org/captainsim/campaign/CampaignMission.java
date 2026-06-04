package org.captainsim.campaign;

import java.util.ArrayList;
import java.util.List;

public class CampaignMission {

    private String id;
    private String name;
    private String narrativeDescription;
    private MissionType type;
    private int expectedDifficulty;
    private String intelligenceSummary;

    private boolean deployed;
    private final List<String> assignedSquadIds;
    private boolean completed;
    private boolean failed;
    private String resultSummary;

    public CampaignMission(String id, String name, MissionType type, int expectedDifficulty) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.expectedDifficulty = expectedDifficulty;
        this.assignedSquadIds = new ArrayList<>();
        this.deployed = false;
        this.completed = false;
        this.failed = false;
    }

    // ===== Deployment =====

    public void assignSquad(String squadId) {
        if (!deployed) {
            assignedSquadIds.add(squadId);
        }
    }

    public void removeSquad(String squadId) {
        assignedSquadIds.remove(squadId);
    }

    public void confirmDeployment() {
        this.deployed = true;
    }

    // ===== Completion =====

    public void complete(String summary) {
        this.completed = true;
        this.resultSummary = summary;
    }

    public void fail(String summary) {
        this.failed = true;
        this.resultSummary = summary;
    }

    public boolean isResolved() {
        return completed || failed;
    }

    // ===== Getters =====

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNarrativeDescription() { return narrativeDescription; }
    public void setNarrativeDescription(String s) { this.narrativeDescription = s; }
    public MissionType getType() { return type; }
    public int getExpectedDifficulty() { return expectedDifficulty; }
    public String getIntelligenceSummary() { return intelligenceSummary; }
    public void setIntelligenceSummary(String s) { this.intelligenceSummary = s; }
    public boolean isDeployed() { return deployed; }
    public List<String> getAssignedSquadIds() { return assignedSquadIds; }
    public boolean isCompleted() { return completed; }
    public boolean isFailed() { return failed; }
    public String getResultSummary() { return resultSummary; }
}
