package org.core.entity.units.chapter.traits;

import java.util.function.Consumer;

public class ChapterTrait {
    private final String id;                    // i18n key, e.g. "trait.master_of_artifice"
    private final boolean isAdvantage;
    private final Consumer<ChapterStats> effect;

    public ChapterTrait(String id, boolean isAdvantage, Consumer<ChapterStats> effect) {
        this.id = id;
        this.isAdvantage = isAdvantage;
        this.effect = effect;
    }

    public String getId() { return id; }
    public boolean isAdvantage() { return isAdvantage; }
    public Consumer<ChapterStats> getEffect() { return effect; }
}
