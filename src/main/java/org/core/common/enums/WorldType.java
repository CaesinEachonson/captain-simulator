package org.core.common.enums;

import org.core.common.consts.AttributeConst;

public enum WorldType {
    CIVILIZED(12, 13, 14, 14, 14, 16, 16, 21),
    FERAL(20, 11, 18, 16, 17, 10, 15, 13),
    DEATH_WORLD(18, 12, 19, 19, 15, 11, 16, 10),
    FORGE_WORLD(12, 17, 14, 15, 12, 20, 13, 17),
    HIVE_WORLD(14, 14, 13, 11, 17, 16, 14, 21),
    AGRICULTURAL(15, 13, 17, 18, 12, 12, 14, 19),
    ICE_WORLD(16, 14, 16, 20, 13, 13, 17, 11),
    DESERT_WORLD(14, 18, 15, 17, 14, 14, 15, 13),
    OCEAN_WORLD(13, 15, 14, 15, 19, 14, 16, 14),
    FOREST_WORLD(17, 16, 15, 14, 18, 12, 14, 14),
    VOLCANIC_WORLD(17, 13, 20, 21, 11, 12, 16, 10),
    DAEMON_WORLD(18, 13, 18, 17, 14, 13, 22, 5);

    private final int wsMod;
    private final int bsMod;
    private final int sMod;
    private final int tMod;
    private final int agMod;
    private final int intMod;
    private final int wpMod;
    private final int felMod;

    WorldType(int wsMod, int bsMod, int sMod, int tMod,
              int agMod, int intMod, int wpMod, int felMod) {
        this.wsMod = wsMod;
        this.bsMod = bsMod;
        this.sMod = sMod;
        this.tMod = tMod;
        this.agMod = agMod;
        this.intMod = intMod;
        this.wpMod = wpMod;
        this.felMod = felMod;
    }

    public int getModifier(int attribute) {
        return switch (attribute) {
            case AttributeConst.WEAPON_SKILL -> wsMod;
            case AttributeConst.BALLISTIC_SKILL -> bsMod;
            case AttributeConst.STRENGTH -> sMod;
            case AttributeConst.TOUGHNESS -> tMod;
            case AttributeConst.AGILITY -> agMod;
            case AttributeConst.INTELLIGENCE -> intMod;
            case AttributeConst.WILLPOWER -> wpMod;
            case AttributeConst.FELLOWSHIP -> felMod;
            default -> throw new IllegalArgumentException("Unknown attribute: " + attribute);
        };
    }
}
