package org.captainsim.campaign;

import java.util.*;

public class Mission {
    private String id;                    // i18n key, e.g. "mission.defend_outpost"
    private String name;                  // display name

    private MissionType type;             // 任务类型
    private int difficulty;               // 难度等级

    private List<String> objectives;      // 任务目标ID列表
    private Map<String, Integer> enemyForces;  // 敌军配置：单位类型 → 数量

    private boolean isCompleted;
    private boolean isFailed;

    public Mission(String id, String name, MissionType type, int difficulty) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.difficulty = difficulty;
        this.objectives = new ArrayList<>();
        this.enemyForces = new HashMap<>();
        this.isCompleted = false;
        this.isFailed = false;
    }

    // ==================== Getters & Setters ====================

    public String getId() { return id; }
    public String getName() { return name; }
    public MissionType getType() { return type; }
    public int getDifficulty() { return difficulty; }
    public List<String> getObjectives() { return Collections.unmodifiableList(objectives); }
    public Map<String, Integer> getEnemyForces() { return Collections.unmodifiableMap(enemyForces); }
    public boolean isCompleted() { return isCompleted; }
    public boolean isFailed() { return isFailed; }

    public void addObjective(String objectiveId) {
        objectives.add(objectiveId);
    }

    public void addEnemyForce(String unitType, int count) {
        enemyForces.merge(unitType, count, Integer::sum);
    }

    public void complete() {
        this.isCompleted = true;
    }

    public void fail() {
        this.isFailed = true;
    }
}
