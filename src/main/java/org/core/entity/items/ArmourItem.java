package org.core.entity.items;

import org.core.entity.base.BaseItem;
import org.core.entity.factory.ArmourFactory;

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
