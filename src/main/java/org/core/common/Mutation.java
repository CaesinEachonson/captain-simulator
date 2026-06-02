package org.core.common;

/**
 * A single mutation entry shared by both fixed and random mutation systems.
 * Fixed mutations are deliberately selected during chapter creation;
 * random mutations are applied probabilistically to individual marines.
 */
public class Mutation {
    private final String id;              // i18n key, e.g. "mutation.betchers_gland"
    private final StatModifier modifier;   // gameplay effect
    private final boolean isVisiblyMutated; // cosmetic flag

    public Mutation(String id, StatModifier modifier, boolean isVisiblyMutated) {
        this.id = id;
        this.modifier = modifier;
        this.isVisiblyMutated = isVisiblyMutated;
    }

    public String getId() { return id; }
    public StatModifier getModifier() { return modifier; }
    public boolean isVisiblyMutated() { return isVisiblyMutated; }
}
