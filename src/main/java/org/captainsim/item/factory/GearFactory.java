package org.captainsim.item.factory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.captainsim.item.GearItem;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GearFactory {

    private static Map<String, GearItem> gearMap = new HashMap<>();

    public static void init(InputStream jsonStream) {
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, GearItem>>() {}.getType();
        Map<String, GearItem> loaded = gson.fromJson(new InputStreamReader(jsonStream), mapType);
        if (loaded != null) {
            gearMap = loaded;
        }
    }

    public static GearItem create(String id) {
        GearItem template = gearMap.get(id);
        if (template == null) return null;
        GearItem copy = new GearItem(template.getId(), template.getName());
        copy.setTraits(new java.util.ArrayList<>(template.getTraits()));
        copy.setProperties(new java.util.HashMap<>(template.getProperties()));
        return copy;
    }

    public static GearItem getTemplate(String id) {
        return gearMap.get(id);
    }
}
