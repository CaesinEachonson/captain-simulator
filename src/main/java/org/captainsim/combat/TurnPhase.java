package org.captainsim.combat;

public enum TurnPhase {
    MOVEMENT("MOVEMENT PHASE"),
    SHOOTING("SHOOTING PHASE"),
    CHARGE("CHARGE PHASE"),
    ENEMY_MOVEMENT("ENEMY MOVEMENT"),
    ENEMY_SHOOTING("ENEMY SHOOTING"),
    ENEMY_CHARGE("ENEMY CHARGE"),
    COMPLETE("BATTLE COMPLETE");

    private final String displayName;

    TurnPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TurnPhase next() {
        switch (this) {
            case MOVEMENT:       return SHOOTING;
            case SHOOTING:       return CHARGE;
            case CHARGE:         return ENEMY_MOVEMENT;
            case ENEMY_MOVEMENT: return ENEMY_SHOOTING;
            case ENEMY_SHOOTING: return ENEMY_CHARGE;
            case ENEMY_CHARGE:   return MOVEMENT;
            default:             return COMPLETE;
        }
    }

    public boolean isPlayerPhase() {
        return this == MOVEMENT || this == SHOOTING || this == CHARGE;
    }

    public boolean isEnemyPhase() {
        return this == ENEMY_MOVEMENT || this == ENEMY_SHOOTING || this == ENEMY_CHARGE;
    }
}
