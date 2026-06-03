package org.captainsim.squad;

import org.captainsim.company.Company;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SquadFactory {

    private static final Map<SquadType, List<String>> HONOR_TITLE_POOL = new HashMap<>();
    private static final Map<SquadType, Integer> TYPE_COUNTER = new HashMap<>();

    static {
        loadHonorTitles();
    }

    private static void loadHonorTitles() {
        try (InputStream is = SquadFactory.class.getClassLoader()
                .getResourceAsStream("data/squad_honor_titles.json")) {
            if (is == null) {
                // No titles file — leave pool empty, squads will have no honor titles
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> titles = gson.fromJson(json, listType);
            // Shuffle so each game gets a different assignment
            Collections.shuffle(titles, new Random());
            // Split titles among squad types
            distributeTitles(titles);
        } catch (IOException e) {
            // Silently fall back — squads will have no honor titles
        }
    }

    private static void distributeTitles(List<String> titles) {
        Iterator<String> it = titles.iterator();

        HONOR_TITLE_POOL.put(SquadType.COMMAND_SQUAD, pickTitles(it, 10));
        HONOR_TITLE_POOL.put(SquadType.TACTICAL_SQUAD, pickTitles(it, 20));
        HONOR_TITLE_POOL.put(SquadType.ASSAULT_SQUAD, pickTitles(it, 15));
        HONOR_TITLE_POOL.put(SquadType.DEVASTATOR_SQUAD, pickTitles(it, 15));
        HONOR_TITLE_POOL.put(SquadType.STERNGUARD_SQUAD, pickTitles(it, 10));
        HONOR_TITLE_POOL.put(SquadType.VANGUARD_SQUAD, pickTitles(it, 10));
        HONOR_TITLE_POOL.put(SquadType.TERMINATOR_SQUAD, pickTitles(it, 10));
        HONOR_TITLE_POOL.put(SquadType.TERMINATOR_ASSAULT_SQUAD, pickTitles(it, 5));
        HONOR_TITLE_POOL.put(SquadType.SCOUT_SQUAD, pickTitles(it, 5));
    }

    private static List<String> pickTitles(Iterator<String> it, int count) {
        List<String> picked = new ArrayList<>(count);
        for (int i = 0; i < count && it.hasNext(); i++) {
            picked.add(it.next());
        }
        return picked;
    }

    /**
     * Create a squad of the given type for the given company.
     * ID is auto-generated from type and the running counter.
     * Honor title is picked from the pool.
     */
    public static Squad createSquad(SquadType squadType, Company company) {
        int number = TYPE_COUNTER.merge(squadType, 1, Integer::sum);
        String id = squadType.getAbbreviation() + "-" + number;
        String honorTitle = pickHonorTitle(squadType);

        int maxMembers = switch (squadType) {
            case TERMINATOR_SQUAD, TERMINATOR_ASSAULT_SQUAD -> 5;
            default -> 10;
        };

        return new Squad(id, squadType, company, honorTitle, maxMembers);
    }

    /**
     * Create a full set of squads for a standard battle company (9 squads).
     */
    public static List<Squad> createBattleCompanySquads(Company company) {
        List<Squad> squads = new ArrayList<>(10);

        squads.add(createSquad(SquadType.COMMAND_SQUAD, company));

        for (int i = 0; i < 5; i++) {
            squads.add(createSquad(SquadType.TACTICAL_SQUAD, company));
        }
        for (int i = 0; i < 2; i++) {
            squads.add(createSquad(SquadType.ASSAULT_SQUAD, company));
        }
        for (int i = 0; i < 2; i++) {
            squads.add(createSquad(SquadType.DEVASTATOR_SQUAD, company));
        }

        return squads;
    }

    private static String pickHonorTitle(SquadType type) {
        List<String> pool = HONOR_TITLE_POOL.get(type);
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}

