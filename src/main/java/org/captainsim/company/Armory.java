package org.captainsim.company;

import org.captainsim.item.WeaponItem;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.GearItem;

import java.util.*;

public class Armory {
    private Map<String, WeaponItem> weapons;       // id → weapon
    private Map<String, ArmourItem> armours;       // id → armour
    private Map<String, GearItem> gears;           // id → gear item

    public Armory() {
        this.weapons = new HashMap<>();
        this.armours = new HashMap<>();
        this.gears = new HashMap<>();
    }

    // ==================== Storage ====================

    public void storeWeapon(WeaponItem weapon) {
        weapons.put(weapon.getId(), weapon);
    }

    public void storeArmour(ArmourItem armour) {
        armours.put(armour.getId(), armour);
    }

    public void storeGear(GearItem gear) {
        gears.put(gear.getId(), gear);
    }

    // ==================== Retrieval ====================

    public WeaponItem getWeapon(String id) {
        return weapons.get(id);
    }

    public ArmourItem getArmour(String id) {
        return armours.get(id);
    }

    public GearItem getGear(String id) {
        return gears.get(id);
    }

    // ==================== Removal ====================

    public WeaponItem removeWeapon(String id) {
        return weapons.remove(id);
    }

    public ArmourItem removeArmour(String id) {
        return armours.remove(id);
    }

    public GearItem removeGear(String id) {
        return gears.remove(id);
    }

    // ==================== Counts ====================

    public int getWeaponCount() { return weapons.size(); }
    public int getArmourCount() { return armours.size(); }
    public int getGearCount() { return gears.size(); }

    // ==================== Item Lists ====================

    public Collection<WeaponItem> getAllWeapons() { return Collections.unmodifiableCollection(weapons.values()); }
    public Collection<ArmourItem> getAllArmours() { return Collections.unmodifiableCollection(armours.values()); }
    public Collection<GearItem> getAllGears() { return Collections.unmodifiableCollection(gears.values()); }
}
