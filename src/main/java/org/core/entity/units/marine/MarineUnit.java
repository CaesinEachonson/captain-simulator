package org.core.entity.units.marine;

import org.core.common.Mutation;
import org.core.common.consts.AttributeConst;
import org.core.entity.base.BaseUnit;
import org.core.entity.items.ArmourItem;
import org.core.entity.items.GearItem;
import org.core.entity.items.WeaponItem;
import org.core.entity.units.marine.enums.BattleRole;
import org.core.utils.Dice;

import java.util.*;

public class MarineUnit extends BaseUnit {

    // ==================== Growth Curve ====================

    private static record GrowthSegment(int startLevel, int endLevel, float growthPerLevel) {}

    private static final Map<Integer, List<GrowthSegment>> GROWTH_CURVES = new HashMap<>();

    static {
        GROWTH_CURVES.put(AttributeConst.WEAPON_SKILL, List.of(
                new GrowthSegment(0, 20, 0.30f),
                new GrowthSegment(20, 60, 0.20f),
                new GrowthSegment(60, 150, 0.10f),
                new GrowthSegment(150, Integer.MAX_VALUE, 0.05f)
        ));
        GROWTH_CURVES.put(AttributeConst.BALLISTIC_SKILL, List.of(
                new GrowthSegment(0, 20, 0.25f),
                new GrowthSegment(20, 60, 0.20f),
                new GrowthSegment(60, 150, 0.15f),
                new GrowthSegment(150, Integer.MAX_VALUE, 0.05f)
        ));
        GROWTH_CURVES.put(AttributeConst.STRENGTH, List.of(
                new GrowthSegment(0, 20, 0.20f),
                new GrowthSegment(20, 60, 0.15f),
                new GrowthSegment(60, 150, 0.05f),
                new GrowthSegment(150, Integer.MAX_VALUE, 0.03f)
        ));
        GROWTH_CURVES.put(AttributeConst.TOUGHNESS, List.of(
                new GrowthSegment(0, 20, 0.20f),
                new GrowthSegment(20, 60, 0.15f),
                new GrowthSegment(60, 150, 0.05f),
                new GrowthSegment(150, Integer.MAX_VALUE, 0.03f)
        ));
        GROWTH_CURVES.put(AttributeConst.AGILITY, List.of(
                new GrowthSegment(0, 20, 0.30f),
                new GrowthSegment(20, 60, 0.15f),
                new GrowthSegment(60, 150, 0.05f),
                new GrowthSegment(150, Integer.MAX_VALUE, 0.02f)
        ));
        GROWTH_CURVES.put(AttributeConst.INTELLIGENCE, List.of(
                new GrowthSegment(0, 100, 0.02f),
                new GrowthSegment(100, Integer.MAX_VALUE, 0.06f)
        ));
        GROWTH_CURVES.put(AttributeConst.WILLPOWER, List.of(
                new GrowthSegment(0, 100, 0.02f),
                new GrowthSegment(100, Integer.MAX_VALUE, 0.06f)
        ));
        GROWTH_CURVES.put(AttributeConst.FELLOWSHIP, List.of(
                new GrowthSegment(0, 100, 0.02f),
                new GrowthSegment(100, Integer.MAX_VALUE, 0.06f)
        ));

    }

    // ==================== Identity ====================
    private String chapterId;
    private String companyId;
    private String squadId;
    private BattleRole role;

    // ==================== Equipment ====================
    private WeaponItem rightHand;
    private WeaponItem leftHand;
    private ArmourItem armorKit;
    private GearItem extraSlot1;
    private GearItem extraSlot2;

    // ==================== Gene-seed & Mutations ====================
    private List<String> implantedOrgans;
    private List<Mutation> mutations;

    // ==================== Status ====================
    private boolean isAvailable;
    private boolean isWounded;
    private int consecutiveMissions;

    // ==================== Experience & Records ====================
    private int experience;
    private int level;
    private int kills;
    private int battlesSurvived;
    private String biography;

    // ==================== Constructor ====================
    public MarineUnit() {
        super();
        this.factionId = "imperium.astartes";
        this.implantedOrgans = new ArrayList<>();
        this.mutations = new ArrayList<>();
        this.isAvailable = true;
        this.isWounded = false;
        this.consecutiveMissions = 0;
        this.experience = 0;
        this.level = 0;
        this.kills = 0;
        this.battlesSurvived = 0;
    }

    // ==================== Generation ====================
    @Override
    public void generateAttributes() {
        if (worldOrigin == null) {
            throw new IllegalStateException("WorldOrigin must be set before generating attributes");
        }
        for (int i = 0; i < AttributeConst.COUNT; i++) {
            float value = Dice.roll2d10() + 20 + worldOrigin.getModifier(i);
            setAttribute(i, (int) value);
        }
        recalculateWounds();
    }

    private float getGrowthRate(int attribute, int currentLevel) {
        List<GrowthSegment> segments = GROWTH_CURVES.get(attribute);
        for (GrowthSegment segment : segments) {
            if (currentLevel >= segment.startLevel() && currentLevel < segment.endLevel()) {
                return segment.growthPerLevel();
            }
        }
        return 0f;
    }

    /**
     * Apply one level-up: all 8 attributes grow according to their curves.
     */
    public void applyLevelUp() {
        for (int i = 0; i < AttributeConst.COUNT; i++) {
            float growth = getGrowthRate(i, this.level);

            switch (i) {
                case AttributeConst.WEAPON_SKILL -> ws += growth;
                case AttributeConst.BALLISTIC_SKILL -> bs += growth;
                case AttributeConst.STRENGTH -> s += growth;
                case AttributeConst.TOUGHNESS -> t += growth;
                case AttributeConst.AGILITY -> ag += growth;
                case AttributeConst.INTELLIGENCE -> intelligence += growth;
                case AttributeConst.WILLPOWER -> wp += growth;
                case AttributeConst.FELLOWSHIP -> fel += growth;
            }
        }
        recalculateWounds();
        this.level++;
    }


    public void applyLevels(int totalLevels) {
        for (int i = 0; i < totalLevels; i++) {
            applyLevelUp();
        }
    }

    public void recalculateWounds() {
        int newMax = Math.round(2 * getT());
        float ratio = 1.0f;
        if (wounds > 0) {
            ratio = (float) currentWounds / wounds;
        }
        this.wounds = newMax;
        this.currentWounds = Math.round(newMax * ratio);
    }

    // ==================== Getters & Setters ====================
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getSquadId() { return squadId; }
    public void setSquadId(String squadId) { this.squadId = squadId; }

    public BattleRole getRole() { return role; }
    public void setRole(BattleRole role) { this.role = role; }

    public WeaponItem getRightHand() { return rightHand; }
    public void setRightHand(WeaponItem rightHand) { this.rightHand = rightHand; }

    public WeaponItem getLeftHand() { return leftHand; }
    public void setLeftHand(WeaponItem leftHand) { this.leftHand = leftHand; }

    public ArmourItem getArmorKit() { return armorKit; }
    public void setArmorKit(ArmourItem armorKit) { this.armorKit = armorKit; }

    public GearItem getExtraSlot1() { return extraSlot1; }
    public void setExtraSlot1(GearItem extraSlot1) { this.extraSlot1 = extraSlot1; }

    public GearItem getExtraSlot2() { return extraSlot2; }
    public void setExtraSlot2(GearItem extraSlot2) { this.extraSlot2 = extraSlot2; }

    public List<String> getImplantedOrgans() { return implantedOrgans; }
    public void setImplantedOrgans(List<String> implantedOrgans) { this.implantedOrgans = implantedOrgans; }

    public List<Mutation> getMutations() { return mutations; }
    public void setMutations(List<Mutation> mutations) { this.mutations = mutations; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isWounded() { return isWounded; }
    public void setWounded(boolean wounded) { isWounded = wounded; }

    public int getConsecutiveMissions() { return consecutiveMissions; }
    public void setConsecutiveMissions(int consecutiveMissions) { this.consecutiveMissions = consecutiveMissions; }
    public void incrementConsecutiveMissions() { this.consecutiveMissions++; }
    public void resetConsecutiveMissions() { this.consecutiveMissions = 0; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public void addExperience(int exp) { this.experience += exp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }

    public int getBattlesSurvived() { return battlesSurvived; }
    public void setBattlesSurvived(int battlesSurvived) { this.battlesSurvived = battlesSurvived; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}
