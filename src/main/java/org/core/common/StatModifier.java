package org.core.common;

/**
 * Generic stat modifier — used by mutations, traits, equipment, etc.
 */
public class StatModifier {
    public enum Target {
        STRENGTH,           // melee damage, carry capacity
        TOUGHNESS,          // damage reduction, survival
        AGILITY,            // movement, dodge
        PERCEPTION,         // ranged accuracy, detection
        INTELLIGENCE,       // tactical actions, tech-use
        WILLPOWER,          // morale resistance, psychic defense
        FELLOWSHIP,         // leadership, diplomacy
        MELEE_SKILL,
        BALLISTIC_SKILL,
        WOUNDS,
        INITIATIVE,
        EXPERIENCE_GAIN
    }

    public enum Type {
        FLAT,       // +5, -10
        PERCENTAGE  // +20%, -15%
    }

    private final Target target;
    private final Type type;
    private final float value;   // positive = buff, negative = debuff

    public StatModifier(Target target, Type type, float value) {
        this.target = target;
        this.type = type;
        this.value = value;
    }

    public Target getTarget() { return target; }
    public Type getType() { return type; }
    public float getValue() { return value; }
}

