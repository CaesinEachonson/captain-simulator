package org.captainsim.chapter.traits;

import java.util.List;

public class ChapterTraitRegistry {

    public static final ChapterTrait MASTER_OF_ARTIFICE = new ChapterTrait(
            "trait.master_of_artifice", true,
            stats -> stats.modifyProductionEfficiency(20)
    );

    public static final ChapterTrait UNYIELDING_ZEAL = new ChapterTrait(
            "trait.unyielding_zeal", true,
            stats -> stats.modifyMoraleResistance(25)
    );

    public static final ChapterTrait WEAKENED_GENE_SEED = new ChapterTrait(
            "trait.weakened_gene_seed", false,
            stats -> stats.modifyRecruitQuality(-20)
    );

    public static final ChapterTrait SUSPECT_LOYALTY = new ChapterTrait(
            "trait.suspect_loyalty", false,
            stats -> { stats.modifyDiplomacyBonus(-15); stats.modifyRequisitionSpeed(-30); }
    );

    public static final ChapterTrait PRIDE_FALLS = new ChapterTrait(
            "trait.pride_falls", false,
            stats -> stats.modifyDiplomacyBonus(-999) // flagged as blocker
    );

    public static List<ChapterTrait> getAllAdvantages() {
        return List.of(MASTER_OF_ARTIFICE, UNYIELDING_ZEAL /* ... */);
    }

    public static List<ChapterTrait> getAllDisadvantages() {
        return List.of(WEAKENED_GENE_SEED, SUSPECT_LOYALTY, PRIDE_FALLS /* ... */);
    }
}

