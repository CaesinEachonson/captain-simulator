package org.captainsim.unit;

import org.captainsim.attribute.WorldType;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.WeaponItem;

import static org.captainsim.attribute.AttributeConst.*;

public abstract class BaseUnit {

    // ==================== 1. Identity ====================

    protected String name;
    protected String factionId;
    protected WorldType worldOrigin;

    // ==================== 2. Core Attributes (8) ====================

    protected float ws;
    protected float bs;
    protected float s;
    protected float t;
    protected float ag;
    protected float intelligence;
    protected float wp;
    protected float fel;

    // ==================== 3. Derived Combat Stats ====================

    protected int wounds;
    protected int currentWounds;

    // ==================== 4. Equipment ====================
    protected WeaponItem rightHand;
    protected WeaponItem leftHand;
    protected ArmourItem armorKit;

    // ==================== Constructor ====================

    protected BaseUnit() {
    }

    // ==================== Abstract Methods ====================

    public abstract void generateAttributes();

    // ==================== Attribute Accessors ====================

    public int getAttribute(int attribute) {
        return switch (attribute) {
            case WEAPON_SKILL -> Math.round(ws);
            case BALLISTIC_SKILL -> Math.round(bs);
            case STRENGTH -> Math.round(s);
            case TOUGHNESS -> Math.round(t);
            case AGILITY -> Math.round(ag);
            case INTELLIGENCE -> Math.round(intelligence);
            case WILLPOWER -> Math.round(wp);
            case FELLOWSHIP -> Math.round(fel);
            default -> throw new IllegalArgumentException("Unknown attribute: " + attribute);
        };
    }

    public void setAttribute(int attribute, int value) {
        switch (attribute) {
            case WEAPON_SKILL:  this.ws = value; break;
            case BALLISTIC_SKILL:  this.bs = value; break;
            case STRENGTH:   this.s = value; break;
            case TOUGHNESS:   this.t = value; break;
            case AGILITY:  this.ag = value; break;
            case INTELLIGENCE: this.intelligence = value; break;
            case WILLPOWER:  this.wp = value; break;
            case FELLOWSHIP: this.fel = value; break;
        }
    }

    // ==================== Getters & Setters ====================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFactionId() { return factionId; }
    public void setFactionId(String factionId) { this.factionId = factionId; }

    public WorldType getWorldOrigin() { return worldOrigin; }
    public void setWorldOrigin(WorldType worldOrigin) { this.worldOrigin = worldOrigin; }

    // Core Attributes
    public int getWs() { return Math.round(ws); }
    public void setWs(int ws) { this.ws = ws; }
    public int getBs() { return Math.round(bs); }
    public void setBs(int bs) { this.bs = bs; }
    public int getS() { return Math.round(s); }
    public void setS(int s) { this.s = s; }
    public int getT() { return Math.round(t); }
    public void setT(int t) { this.t = t; }
    public int getAg() { return Math.round(ag); }
    public void setAg(int ag) { this.ag = ag; }
    public int getIntelligence() { return Math.round(intelligence); }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public int getWp() { return Math.round(wp); }
    public void setWp(int wp) { this.wp = wp; }
    public int getFel() { return Math.round(fel); }
    public void setFel(int fel) { this.fel = fel; }

    // Derived Stats
    public int getWounds() { return wounds; }
    public void setWounds(int wounds) { this.wounds = wounds; }
    public int getCurrentWounds() { return currentWounds; }
    public void setCurrentWounds(int currentWounds) { this.currentWounds = currentWounds; }

    // Equipments
    public WeaponItem getRightHand() { return rightHand; }
    public void setRightHand(WeaponItem rightHand) { this.rightHand = rightHand; }
    public WeaponItem getLeftHand() { return leftHand; }
    public void setLeftHand(WeaponItem leftHand) { this.leftHand = leftHand; }
    public ArmourItem getArmorKit() { return armorKit; }
    public void setArmorKit(ArmourItem armorKit) { this.armorKit = armorKit; }
}
