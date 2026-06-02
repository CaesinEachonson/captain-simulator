package org.core.entity.units.company;

public class ResourcePool {
    private int requisitionPoints;
    private int materials;
    private int influence;

    public ResourcePool() {
        this.requisitionPoints = 0;
        this.materials = 0;
        this.influence = 0;
    }

    public ResourcePool(int requisitionPoints, int materials, int influence) {
        this.requisitionPoints = requisitionPoints;
        this.materials = materials;
        this.influence = influence;
    }

    // ==================== Add ====================

    public void addRequisitionPoints(int amount) { this.requisitionPoints += amount; }
    public void addMaterials(int amount) { this.materials += amount; }
    public void addInfluence(int amount) { this.influence += amount; }

    // ==================== Spend ====================

    public boolean spendRequisitionPoints(int amount) {
        if (this.requisitionPoints < amount) return false;
        this.requisitionPoints -= amount;
        return true;
    }

    public boolean spendMaterials(int amount) {
        if (this.materials < amount) return false;
        this.materials -= amount;
        return true;
    }

    public boolean spendInfluence(int amount) {
        if (this.influence < amount) return false;
        this.influence -= amount;
        return true;
    }

    // ==================== Getters & Setters ====================

    public int getRequisitionPoints() { return requisitionPoints; }
    public void setRequisitionPoints(int requisitionPoints) { this.requisitionPoints = requisitionPoints; }

    public int getMaterials() { return materials; }
    public void setMaterials(int materials) { this.materials = materials; }

    public int getInfluence() { return influence; }
    public void setInfluence(int influence) { this.influence = influence; }
}
