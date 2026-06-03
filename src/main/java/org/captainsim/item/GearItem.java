package org.captainsim.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GearItem extends BaseItem {
    private List<String> traits = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();

    public GearItem() {
        super("", "");
    }

    public GearItem(String id, String name) {
        super(id, name);
    }

    public GearItem(String id, String name, List<String> traits) {
        super(id, name);
        this.traits = traits;
    }

    // ==================== Traits ====================
    public List<String> getTraits() { return traits; }
    public void setTraits(List<String> traits) { this.traits = traits; }
    public boolean hasTrait(String trait) { return traits.contains(trait); }

    // ==================== Properties ====================
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }

    public int getIntProperty(String key, int defaultValue) {
        Object val = properties.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
