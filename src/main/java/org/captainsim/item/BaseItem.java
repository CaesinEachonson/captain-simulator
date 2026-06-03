package org.captainsim.item;

public abstract class BaseItem {
    protected String id;          // i18n key, e.g. "item.bolter", "item.power_armour"
    protected String name;        // display name (or could rely on i18n only)

    protected BaseItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
