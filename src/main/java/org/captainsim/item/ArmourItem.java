package org.captainsim.item;

import org.captainsim.item.factory.ArmourFactory;

import java.util.ArrayList;
import java.util.List;

public class ArmourItem extends BaseItem {
    private int armourValue;
    private List<String> traits;

    // JSON deserialization constructor
    public ArmourItem() {
        super(null, null);
    }

    /**
     * Construct armour by its ID. Loads all data from ArmourFactory automatically.
     */
    public ArmourItem(String id) {
        super(id, null);
        ArmourItem template = ArmourFactory.get(id);
        this.armourValue = template.armourValue;
        this.traits = new ArrayList<>(template.traits);
        this.name = template.name;
    }

    public int getArmourValue() { return armourValue; }
    public List<String> getTraits() { return traits; }
}
