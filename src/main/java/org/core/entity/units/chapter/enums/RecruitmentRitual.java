package org.core.entity.units.chapter.enums;

public enum RecruitmentRitual {
    TRIALS_OF_MIGHT(10, 5),
    TRIALS_OF_WISDOM(0, 15),
    ASPIRANT_SLAUGHTER(15, 0),
    BLOOD_RITE(-5, 10),
    PSYCHIC_AWAKENING(0, 8);

    private final int strengthBonus;
    private final int experienceBonus;
    RecruitmentRitual(int strBonus, int expBonus) {
        this.strengthBonus = strBonus;
        this.experienceBonus = expBonus;
    }
    public int getStrengthBonus() { return strengthBonus; }
    public int getExperienceBonus() { return experienceBonus; }
}
