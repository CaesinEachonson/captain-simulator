package org.core.entity.units.chapter.traits;

public class ChapterStats {
    private int commandPoints;
    private int requisitionSpeed;
    private int productionEfficiency;
    private int moraleResistance;
    private int diplomacyBonus;
    private int recruitQuality;
    private int mutationResistance;

    public void modifyCommandPoints(int delta) { this.commandPoints += delta; }
    public void modifyRequisitionSpeed(int delta) { this.requisitionSpeed += delta; }
    public void modifyProductionEfficiency(int delta) { this.productionEfficiency += delta; }
    public void modifyMoraleResistance(int delta) { this.moraleResistance += delta; }
    public void modifyDiplomacyBonus(int delta) { this.diplomacyBonus += delta; }
    public void modifyRecruitQuality(int delta) { this.recruitQuality += delta; }
    public void modifyMutationResistance(int delta) { this.mutationResistance += delta; }

    // getters...
    public int getCommandPoints() { return commandPoints; }
    public int getRequisitionSpeed() { return requisitionSpeed; }
    public int getProductionEfficiency() { return productionEfficiency; }
    public int getMoraleResistance() { return moraleResistance; }
    public int getDiplomacyBonus() { return diplomacyBonus; }
    public int getRecruitQuality() { return recruitQuality; }
    public int getMutationResistance() { return mutationResistance; }
}

