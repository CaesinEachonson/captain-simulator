package org.captainsim.chapter;

import org.captainsim.chapter.enums.*;
import org.captainsim.chapter.traits.ChapterStats;
import org.captainsim.chapter.traits.ChapterTrait;
import org.captainsim.unit.marine.mutation.Mutation;
import org.captainsim.unit.marine.mutation.MutationRegistry;
import org.captainsim.attribute.WorldType;
import org.captainsim.company.Company;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Chapter {
    // === Identity ===
    private String name;
    private String parentChapter;
    private String battleCry;
    private ChapterType type;
    private WorldType homeworldType;
    private String homeworldName;

    // === Gene-seed ===
    private PurityLevel geneSeedPurity;
    private StabilityLevel geneSeedStability;
    private List<Mutation> fixedMutations;           // selected from Registry during creation
    private List<Mutation> randomMutationCandidates;  // subset of Registry, rolled per marine


    // === Strength ===
    private ChapterStrength strength;
    private int currentMarineCount;

    // === Traits ===
    private List<ChapterTrait> advantages;
    private List<ChapterTrait> disadvantages;
    private ChapterStats stats;

    // === Command Hierarchy ===
    private ChapterCommand command;

    // === Diplomacy & Appearance ===
    private ChapterRelations relations;
    private String colorSchemeJson;

    // === Culture ===
    private RecruitmentRitual recruitmentRitual;
    private PsychicDiscipline psychicDiscipline;

    // === Companies ===
    private List<Company> companies;
    private Company playerCompany;

    private Chapter() {}

    public static ChapterBuilder builder() {
        return new ChapterBuilder();
    }

    // ========== Behavioral Methods ==========

    public List<String> rollRandomMutationsForMarine() {
        List<String> mutations = new ArrayList<>();
        if (randomMutationCandidates == null || randomMutationCandidates.isEmpty()) {
            return mutations;
        }
        float roll = ThreadLocalRandom.current().nextFloat();
        if (roll < geneSeedStability.getMutationChancePerMarine()) {
            // Pick one randomly from the candidate pool
            int index = ThreadLocalRandom.current().nextInt(randomMutationCandidates.size());
            mutations.add(randomMutationCandidates.get(index).getId());
        }
        return mutations;
    }

    public int getPlayerCompanyMaxSize() {
        return Math.min(100, strength.getMaxMarines() / 10);
    }

    // ========== Builder ==========

    public static class ChapterBuilder {
        private final Chapter chapter = new Chapter();

        public ChapterBuilder name(String name) {
            chapter.name = name;
            return this;
        }

        public ChapterBuilder parentChapter(String parent) {
            chapter.parentChapter = parent;
            return this;
        }

        public ChapterBuilder battleCry(String cry) {
            chapter.battleCry = cry;
            return this;
        }

        public ChapterBuilder type(ChapterType type) {
            chapter.type = type;
            return this;
        }

        public ChapterBuilder homeworldType(WorldType ht) {
            chapter.homeworldType = ht;
            return this;
        }

        public ChapterBuilder homeworldName(String name) {
            chapter.homeworldName = name;
            return this;
        }

        public ChapterBuilder geneSeed(PurityLevel purity, StabilityLevel stability) {
            chapter.geneSeedPurity = purity;
            chapter.geneSeedStability = stability;
            return this;
        }

        public ChapterBuilder fixedMutations(List<Mutation> mutations) {
            // Validate count matches purity level
            int required = chapter.geneSeedPurity.getRequiredFixedMutations();
            if (mutations.size() != required) {
                throw new IllegalArgumentException(
                        "Purity level " + chapter.geneSeedPurity +
                                " requires exactly " + required + " fixed mutations, got " + mutations.size()
                );
            }
            chapter.fixedMutations = new ArrayList<>(mutations);
            return this;
        }
        public ChapterBuilder randomMutationCandidates(List<Mutation> candidates) {
            chapter.randomMutationCandidates = new ArrayList<>(candidates);
            return this;
        }

        public ChapterBuilder strength(ChapterStrength strength) {
            chapter.strength = strength;
            return this;
        }

        public ChapterBuilder advantages(List<ChapterTrait> adv) {
            chapter.advantages = new ArrayList<>(adv);
            return this;
        }

        public ChapterBuilder disadvantages(List<ChapterTrait> dis) {
            chapter.disadvantages = new ArrayList<>(dis);
            return this;
        }

        public ChapterBuilder command(ChapterCommand cmd) {
            chapter.command = cmd;
            return this;
        }

        public ChapterBuilder relations(ChapterRelations rel) {
            chapter.relations = rel;
            return this;
        }

        public ChapterBuilder colorScheme(String json) {
            chapter.colorSchemeJson = json;
            return this;
        }

        public ChapterBuilder recruitment(RecruitmentRitual ritual) {
            chapter.recruitmentRitual = ritual;
            return this;
        }

        public ChapterBuilder psychic(PsychicDiscipline discipline) {
            chapter.psychicDiscipline = discipline;
            return this;
        }

        public Chapter build() {
            Objects.requireNonNull(chapter.name, "Chapter name must not be null");
            if (chapter.type == ChapterType.HOMEWORLD && chapter.homeworldType == null) {
                throw new IllegalStateException("Homeworld-type chapter must specify a homeworld type");
            }
            // If fixedMutations not explicitly set, default based on purity
            if (chapter.fixedMutations == null) {
                chapter.fixedMutations = new ArrayList<>();
            }
            // If randomMutationCandidates not explicitly set, default to registry
            if (chapter.randomMutationCandidates == null) {
                chapter.randomMutationCandidates = MutationRegistry.getRandomMutationCandidates();
            }
            chapter.currentMarineCount = chapter.strength.getMaxMarines();
            chapter.stats = new ChapterStats();
            applyTraits();
            return chapter;
        }

        private void applyTraits() {
            for (ChapterTrait t : chapter.advantages) {
                t.getEffect().accept(chapter.stats);
            }
            for (ChapterTrait t : chapter.disadvantages) {
                t.getEffect().accept(chapter.stats);
            }
            // Recruitment ritual modifies recruit quality
            chapter.stats.modifyRecruitQuality(chapter.recruitmentRitual.getStrengthBonus());
        }
    }

    // ========== Getters ==========

    public String getName() { return name; }
    public String getParentChapter() { return parentChapter; }
    public String getBattleCry() { return battleCry; }
    public ChapterType getType() { return type; }
    public Optional<WorldType> getHomeworldType() { return Optional.ofNullable(homeworldType); }
    public Optional<String> getHomeworldName() { return Optional.ofNullable(homeworldName); }
    public PurityLevel getGeneSeedPurity() { return geneSeedPurity; }
    public StabilityLevel getGeneSeedStability() { return geneSeedStability; }
    public List<Mutation> getFixedMutations() { return Collections.unmodifiableList(fixedMutations); }
    public ChapterStrength getStrength() { return strength; }
    public List<ChapterTrait> getAdvantages() { return Collections.unmodifiableList(advantages); }
    public List<ChapterTrait> getDisadvantages() { return Collections.unmodifiableList(disadvantages); }
    public ChapterStats getStats() { return stats; }
    public ChapterCommand getCommand() { return command; }
    public ChapterRelations getRelations() { return relations; }
    public String getColorSchemeJson() { return colorSchemeJson; }
    public RecruitmentRitual getRecruitmentRitual() { return recruitmentRitual; }
    public PsychicDiscipline getPsychicDiscipline() { return psychicDiscipline; }
    public Company getPlayerCompany() { return playerCompany; }

    void setPlayerCompany(Company company) { this.playerCompany = company; }
}

