package org.core.entity.units.squad;
public enum SquadType {
    COMMAND_SQUAD("CMD"),
    TACTICAL_SQUAD("TAC"),
    ASSAULT_SQUAD("ASLT"),
    DEVASTATOR_SQUAD("DEV"),
    STERNGUARD_SQUAD("STG"),
    VANGUARD_SQUAD("VGD"),
    TERMINATOR_SQUAD("TAC"),
    TERMINATOR_ASSAULT_SQUAD("TAA"),
    SCOUT_SQUAD("SCT");

    private final String abbreviation;

    SquadType(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() { return abbreviation; }
}
