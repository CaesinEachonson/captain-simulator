package org.captainsim.item;

import org.captainsim.item.factory.WeaponFactory;

import java.util.ArrayList;
import java.util.List;

public class WeaponItem extends BaseItem {
    private int damage;
    private int armourPenetration;
    private int ammo;
    private int range;
    private int attacks;
    private List<String> traits;

    // JSON deserialization constructor
    public WeaponItem() {
        super(null, null);
    }

    /**
     * Construct a weapon by its ID. Loads all data from WeaponFactory automatically.
     */
    public WeaponItem(String id) {
        super(id, null);
        WeaponItem template = WeaponFactory.getAny(id);
        this.damage = template.damage;
        this.armourPenetration = template.armourPenetration;
        this.ammo = template.ammo;
        this.range = template.range;
        this.attacks = template.attacks;
        this.traits = new ArrayList<>(template.traits);
        this.name = template.name;
    }

    public int getDamage() { return damage; }
    public int getArmourPenetration() { return armourPenetration; }
    public int getAmmo() { return ammo; }
    public int getRange() { return range; }
    public int getAttacks() { return attacks; }
    public List<String> getTraits() { return traits; }
}
