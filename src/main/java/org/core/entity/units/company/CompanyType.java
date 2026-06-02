package org.core.entity.units.company;

import org.core.entity.units.marine.enums.BattleRole;

public enum CompanyType {
    FIRST_COMPANY(100, 150),
    SECOND_COMPANY(90, 100),
    THIRD_COMPANY(80, 90),
    FOURTH_COMPANY(70, 80),
    FIFTH_COMPANY(60, 70),
    SIXTH_COMPANY(50, 60),
    SEVENTH_COMPANY(40, 50),
    EIGHTH_COMPANY(30, 40),
    NINTH_COMPANY(20, 30),
    TENTH_COMPANY(0, 20);

    private final int baseMinLevel;
    private final int baseMaxLevel;

    CompanyType(int baseMinLevel, int baseMaxLevel) {
        this.baseMinLevel = baseMinLevel;
        this.baseMaxLevel = baseMaxLevel;
    }

    public int getBaseMinLevel() { return baseMinLevel; }
    public int getBaseMaxLevel() { return baseMaxLevel; }

    public int getMinLevelForRole(BattleRole role) {
        return switch (role) {
            case CAPTAIN -> Math.max(baseMinLevel + 170, 250);
            case LIBRARIAN, TECHMARINE, APOTHECARY -> Math.max(baseMinLevel + 100, 150);
            case CHAPLAIN -> Math.max(baseMaxLevel + 120, 180);
            case COMPANY_CHAMPION -> Math.max(baseMinLevel + 120, 200);
            case SERGEANT -> Math.max(baseMinLevel + 40, 120);
            case VETERAN, ANCIENT -> Math.max(baseMinLevel + 70, 150);
            case TERMINATOR, HONOUR_GUARD -> Math.max(baseMinLevel + 120, 200);
            default -> baseMinLevel;
        };
    }

    public int getMaxLevelForRole(BattleRole role) {
        return switch (role) {
            case CAPTAIN -> Math.max(baseMaxLevel + 280, 400);
            case CHAPLAIN, LIBRARIAN, TECHMARINE, APOTHECARY -> Math.max(baseMaxLevel + 230, 350);
            case COMPANY_CHAMPION -> Math.max(baseMaxLevel + 180, 300);
            case SERGEANT -> Math.max(baseMaxLevel + 80, 200);
            case VETERAN, ANCIENT -> Math.max(baseMaxLevel + 130, 250);
            case TERMINATOR, HONOUR_GUARD -> Math.max(baseMaxLevel + 150, 350);
            default -> baseMaxLevel;
        };
    }
}
