package org.captainsim.chapter;

import java.util.HashMap;
import java.util.Map;

public class ChapterRelations {
    public enum Faction {
        MECHANICUS,
        INQUISITION,
        ADMINISTRATUM,
        ECCLESIARCHY,
        OTHER_CHAPTERS,
        PLANETARY_GOVERNORS,
        NAVIS_NOBILITE
    }

    public enum RelationLevel {
        ALLIED(50, 100),
        FRIENDLY(20, 49),
        NEUTRAL(-19, 19),
        HOSTILE(-49, -20),
        ENEMY(-100, -50);

        private final int min;
        private final int max;
        RelationLevel(int min, int max) { this.min = min; this.max = max; }

        public static RelationLevel fromValue(int value) {
            for (RelationLevel level : values()) {
                if (value >= level.min && value <= level.max) return level;
            }
            return NEUTRAL;
        }
    }

    private final Map<Faction, Integer> relations = new HashMap<>();

    public ChapterRelations() {
        for (Faction f : Faction.values()) {
            relations.put(f, 0);
        }
    }

    public void modifyRelation(Faction faction, int delta) {
        int current = relations.get(faction);
        relations.put(faction, Math.max(-100, Math.min(100, current + delta)));
    }

    public int getValue(Faction faction) { return relations.get(faction); }
    public RelationLevel getLevel(Faction faction) {
        return RelationLevel.fromValue(relations.get(faction));
    }
}
