package org.captainsim.chapter.enums;

public enum PurityLevel {
    PURE(0),
    STABLE(1),
    TAINTED(2),
    CORRUPTED(3);

    private final int requiredFixedMutations;
    PurityLevel(int requiredMutations) { this.requiredFixedMutations = requiredMutations; }
    public int getRequiredFixedMutations() { return requiredFixedMutations; }
}