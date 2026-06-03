package org.captainsim.chapter.enums;

public enum ChapterStrength {
    CRIPPLED(400),
    DEPLETED(600),
    STANDARD(1000),
    OVERSTRENGTH(1200);

    private final int maxMarines;
    ChapterStrength(int max) { this.maxMarines = max; }
    public int getMaxMarines() { return maxMarines; }
}
