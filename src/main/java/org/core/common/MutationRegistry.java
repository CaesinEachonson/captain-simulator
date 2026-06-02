package org.core.common;

import java.util.*;

/**
 * Registry of all available mutations in the game.
 * Both fixed mutations (purity-based selection) and random mutations
 * (stability-based chance) draw from this single pool.
 */
public class MutationRegistry {

    private static final Map<String, Mutation> REGISTRY = new LinkedHashMap<>();

    static {
        // === Gene-seed defects (fixed mutation candidates) ===
        register("mutation.betchers_gland",
                new Mutation("mutation.betchers_gland", null, false));  // TODO: add modifier
        register("mutation.sus_an_membrane",
                new Mutation("mutation.sus_an_membrane", null, false));
        register("mutation.mucranoid",
                new Mutation("mutation.mucranoid", null, false));
        register("mutation.ossmodula",
                new Mutation("mutation.ossmodula", null, false));
        register("mutation.catalystic_node",
                new Mutation("mutation.catalystic_node", null, false));
        register("mutation.preomnor",
                new Mutation("mutation.preomnor", null, false));
        register("mutation.omophagea",
                new Mutation("mutation.omophagea", null, false));
        register("mutation.melanochrome",
                new Mutation("mutation.melanochrome", null, false));
        register("mutation.neuroglottis",
                new Mutation("mutation.neuroglottis", null, false));
        register("mutation.larramans_organ",
                new Mutation("mutation.larramans_organ", null, false));

        // === Additional random mutations ===
        register("mutation.mutant_flesh",
                new Mutation("mutation.mutant_flesh", null, true));
        register("mutation.deteriorated_lenses",
                new Mutation("mutation.deteriorated_lenses", null, false));
        register("mutation.extra_heart",
                new Mutation("mutation.extra_heart", null, false));
        register("mutation.deformed_limb",
                new Mutation("mutation.deformed_limb", null, true));
        register("mutation.secondary_catalytic_node",
                new Mutation("mutation.secondary_catalytic_node", null, false));

        // TODO: populate with full Warhammer 40k gene-seed mutation table
    }

    private static void register(String id, Mutation mutation) {
        REGISTRY.put(id, mutation);
    }

    public static Mutation get(String id) {
        Mutation m = REGISTRY.get(id);
        if (m == null) {
            throw new IllegalArgumentException("Unknown mutation: " + id);
        }
        return m;
    }

    public static List<Mutation> getAll() {
        return List.copyOf(REGISTRY.values());
    }

    /**
     * Returns mutations that are typical gene-seed deficiencies
     * (suitable as fixed mutation choices during chapter creation).
     */
    public static List<Mutation> getFixedMutationCandidates() {
        return List.of(
                get("mutation.betchers_gland"),
                get("mutation.sus_an_membrane"),
                get("mutation.mucranoid"),
                get("mutation.ossmodula"),
                get("mutation.catalystic_node"),
                get("mutation.preomnor"),
                get("mutation.omophagea"),
                get("mutation.melanochrome"),
                get("mutation.neuroglottis"),
                get("mutation.larramans_organ")
        );
    }

    /**
     * Returns mutations that can appear randomly on individual marines
     * based on the chapter's stability level.
     */
    public static List<Mutation> getRandomMutationCandidates() {
        return List.of(
                get("mutation.mutant_flesh"),
                get("mutation.deteriorated_lenses"),
                get("mutation.extra_heart"),
                get("mutation.deformed_limb"),
                get("mutation.secondary_catalytic_node")
        );
    }
}
