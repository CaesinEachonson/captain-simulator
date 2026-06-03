package org.captainsim.item.factory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.captainsim.item.WeaponItem;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponFactory {

    private static final Map<String, WeaponItem> MELEE_WEAPONS = new ConcurrentHashMap<>();
    private static final Map<String, WeaponItem> RANGED_WEAPONS = new ConcurrentHashMap<>();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        loadFromFile("data/weapons_melee.json", MELEE_WEAPONS);
        loadFromFile("data/weapons_ranged.json", RANGED_WEAPONS);
        loaded = true;
    }

    private static void loadFromFile(String path, Map<String, WeaponItem> targetMap) {
        try (InputStream is = WeaponFactory.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Weapon data not found: " + path);
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, WeaponItem>>() {}.getType();
            Map<String, WeaponItem> data = gson.fromJson(json, mapType);
            targetMap.putAll(data);
        } catch (IOException e) {
            System.err.println("Failed to load weapon data: " + path);
        }
    }

    public static WeaponItem getMelee(String id) {
        if (!loaded) load();
        WeaponItem weapon = MELEE_WEAPONS.get(id);
        if (weapon == null) {
            throw new IllegalArgumentException("Melee weapon not found: " + id);
        }
        return weapon;
    }

    public static WeaponItem getRanged(String id) {
        if (!loaded) load();
        WeaponItem weapon = RANGED_WEAPONS.get(id);
        if (weapon == null) {
            throw new IllegalArgumentException("Ranged weapon not found: " + id);
        }
        return weapon;
    }

    public static WeaponItem getAny(String id) {
        if (!loaded) load();
        WeaponItem w = RANGED_WEAPONS.get(id);
        if (w != null) return w;
        w = MELEE_WEAPONS.get(id);
        if (w != null) return w;
        throw new IllegalArgumentException("Weapon not found: " + id);
    }

    public static Collection<WeaponItem> getAllMelee() {
        if (!loaded) load();
        return Collections.unmodifiableCollection(MELEE_WEAPONS.values());
    }

    public static Collection<WeaponItem> getAllRanged() {
        if (!loaded) load();
        return Collections.unmodifiableCollection(RANGED_WEAPONS.values());
    }

    public static Set<String> getAllMeleeIds() {
        if (!loaded) load();
        return MELEE_WEAPONS.keySet();
    }

    public static Set<String> getAllRangedIds() {
        if (!loaded) load();
        return RANGED_WEAPONS.keySet();
    }
}
