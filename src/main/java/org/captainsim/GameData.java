package org.captainsim;

import org.captainsim.campaign.Campaign;
import org.captainsim.company.Company;
import org.captainsim.item.ArmourItem;
import org.captainsim.item.WeaponItem;
import org.captainsim.item.factory.GearFactory;
import org.captainsim.squad.Squad;
import org.captainsim.unit.marine.MarineUnit;
import org.captainsim.util.EquipmentDistributor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameData {

    private static GameData instance;

    private Company company;
    private Campaign currentCampaign;


    // Private — use getInstance()
    private GameData() {}

    public static GameData getInstance() {
        if (instance == null) throw new IllegalStateException("GameData not initialized. Call init() first.");
        return instance;
    }

    /**
     * Loads all data files and initializes factories.
     * Does NOT generate a company — that's done separately.
     */
    public static void init() throws Exception {
        if (instance != null) return;

        Map<String, WeaponItem> meleeWeapons = loadWeapons("data/weapons_melee.json");
        Map<String, WeaponItem> rangedWeapons = loadWeapons("data/weapons_ranged.json");
        Map<String, ArmourItem> armours = loadArmours("data/armour.json");

        GearFactory.init(GameData.class.getClassLoader().getResourceAsStream("data/gear.json"));
        EquipmentDistributor.init(meleeWeapons, rangedWeapons, armours);

        instance = new GameData();
    }

    // ==================== State ====================

    public void setCompany(Company company) { this.company = company; }
    public Company getCompany() { return company; }
    public List<Squad> getSquads() {
        return company != null ? company.getSquads() : List.of();
    }
    public boolean hasCompany() { return company != null; }

    // ==================== Data Loading ====================

    private static Map<String, WeaponItem> loadWeapons(String path) throws Exception {
        try (InputStream is = GameData.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException(path + " not found");
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, WeaponItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    private static Map<String, ArmourItem> loadArmours(String path) throws Exception {
        try (InputStream is = GameData.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException(path + " not found");
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, ArmourItem>>() {}.getType();
            return gson.fromJson(json, mapType);
        }
    }

    public static List<String> loadNames() throws Exception {
        try (InputStream is = GameData.class.getClassLoader().getResourceAsStream("data/marine_names.json")) {
            if (is == null) throw new FileNotFoundException("data/marine_names.json not found");
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(json, listType);
        }
    }

    public int getTotalMarines() {
        return company != null ? company.getSquads().stream().mapToInt(Squad::getSize).sum() : 0;
    }

    public int getAvailableMarines() {
        if (company == null) return 0;
        return (int) company.getSquads().stream()
                .flatMap(s -> s.getAllMarines().stream())
                .filter(MarineUnit::isAvailable)
                .count();
    }

    public Campaign getCurrentCampaign() { return currentCampaign; }
    public void setCurrentCampaign(Campaign campaign) { this.currentCampaign = campaign; }
    public boolean hasActiveCampaign() {
        return currentCampaign != null && currentCampaign.isActive();
    }

}
