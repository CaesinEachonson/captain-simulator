package org.captainsim.company;

import org.captainsim.item.BaseItem;

import java.util.*;

public class ProductionManager {

    public enum ProductionStatus {
        QUEUED,
        IN_PROGRESS,
        COMPLETED
    }

    public static class ProductionOrder {
        private final String id;
        private BaseItem template;       // 生产模板
        private final int cost;                // 材料消耗
        private final int totalTurns;          // 所需总回合数
        private int remainingTurns;            // 剩余回合数
        private ProductionStatus status;

        public ProductionOrder(String id, BaseItem template, int cost, int totalTurns) {
            this.id = id;
            this.template = template;
            this.cost = cost;
            this.totalTurns = totalTurns;
            this.remainingTurns = totalTurns;
            this.status = ProductionStatus.QUEUED;
        }

        public String getId() { return id; }
        public BaseItem getTemplate() { return template; }
        public int getCost() { return cost; }
        public int getTotalTurns() { return totalTurns; }
        public int getRemainingTurns() { return remainingTurns; }
        public ProductionStatus getStatus() { return status; }

        public void start() { this.status = ProductionStatus.IN_PROGRESS; }

        /**
         * Advance one turn. Returns true if production is now complete.
         */
        public boolean advance() {
            if (status != ProductionStatus.IN_PROGRESS) return false;
            remainingTurns--;
            if (remainingTurns <= 0) {
                remainingTurns = 0;
                status = ProductionStatus.COMPLETED;
                return true;
            }
            return false;
        }
    }

    private final Queue<ProductionOrder> queue;
    private ProductionOrder currentOrder;
    private int maxQueueSize;

    public ProductionManager() {
        this.queue = new LinkedList<>();
        this.currentOrder = null;
        this.maxQueueSize = 10;
    }

    public ProductionManager(int maxQueueSize) {
        this();
        this.maxQueueSize = maxQueueSize;
    }

    /**
     * Queue a new production order. Returns false if the queue is full.
     */
    public boolean enqueue(BaseItem template, int cost, int turns, ResourcePool resources) {
        if (queue.size() >= maxQueueSize && currentOrder != null) {
            return false;
        }

        if (!resources.spendMaterials(cost)) {
            return false;
        }

        ProductionOrder order = new ProductionOrder(
                UUID.randomUUID().toString(),
                template,
                cost,
                turns
        );

        queue.add(order);
        return true;
    }

    /**
     * Advance the production line by one turn.
     * Returns the completed order, or null if nothing completed this turn.
     */
    public ProductionOrder advanceTurn() {
        if (currentOrder == null) {
            startNextOrder();
        }

        if (currentOrder == null) {
            return null;
        }

        boolean completed = currentOrder.advance();
        if (completed) {
            ProductionOrder finished = currentOrder;
            currentOrder = null;
            startNextOrder();
            return finished;
        }

        return null;
    }

    private void startNextOrder() {
        if (!queue.isEmpty()) {
            currentOrder = queue.poll();
            currentOrder.start();
        }
    }

    /**
     * Cancel a queued order by id. Returns false if not found.
     */
    public boolean cancelOrder(String orderId) {
        Iterator<ProductionOrder> iter = queue.iterator();
        while (iter.hasNext()) {
            ProductionOrder order = iter.next();
            if (order.getId().equals(orderId)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current order in progress.
     */
    public Optional<ProductionOrder> getCurrentOrder() {
        return Optional.ofNullable(currentOrder);
    }

    /**
     * Get all queued orders (not including the one in progress).
     */
    public List<ProductionOrder> getQueuedOrders() {
        return List.copyOf(queue);
    }

    /**
     * Check if the production line is idle.
     */
    public boolean isIdle() {
        return currentOrder == null && queue.isEmpty();
    }

    /**
     * Get the total number of queued orders + current order.
     */
    public int getTotalOrders() {
        int count = queue.size();
        if (currentOrder != null) count++;
        return count;
    }

    public int getMaxQueueSize() { return maxQueueSize; }
    public void setMaxQueueSize(int maxQueueSize) { this.maxQueueSize = maxQueueSize; }
}
