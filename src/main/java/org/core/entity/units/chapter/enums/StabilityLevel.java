package org.core.entity.units.chapter.enums;

public enum StabilityLevel {
    STABLE(0.00f),
    RELIABLE(0.05f),
    VOLATILE(0.15f),
    CHAOTIC(0.35f);

    private final float mutationChancePerMarine;
    StabilityLevel(float chance) { this.mutationChancePerMarine = chance; }
    public float getMutationChancePerMarine() { return mutationChancePerMarine; }
}
