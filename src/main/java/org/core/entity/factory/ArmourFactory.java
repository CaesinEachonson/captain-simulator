package org.core.entity.factory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.core.entity.items.ArmourItem;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArmourFactory {

    private static final Map<String, ArmourItem> ARMOURS = new ConcurrentHashMap<>();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        try (InputStream is = ArmourFactory.class.getClassLoader().getResourceAsStream("data/armour.json")) {
            if (is == null) {
                System.err.println("Armour data not found: data/armour.json");
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, ArmourItem>>() {}.getType();
            Map<String, ArmourItem> data = gson.fromJson(json, mapType);
            ARMOURS.putAll(data);
            loaded = true;
        } catch (IOException e) {
            System.err.println("Failed to load armour data: " + e.getMessage());
        }
    }

    public static ArmourItem get(String id) {
        if (!loaded) load();
        ArmourItem armour = ARMOURS.get(id);
        if (armour == null) {
            throw new IllegalArgumentException("Armour not found: " + id);
        }
        return armour;
    }

    public static Collection<ArmourItem> getAll() {
        if (!loaded) load();
        return Collections.unmodifiableCollection(ARMOURS.values());
    }

    public static Set<String> getAllIds() {
        if (!loaded) load();
        return ARMOURS.keySet();
    }
}

