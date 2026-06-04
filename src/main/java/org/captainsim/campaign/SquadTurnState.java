package org.captainsim.combat;

/**
 * Tracks a single squad's actions within one player turn.
 */
public class SquadTurnState {

    private boolean moved;       // did standard move (+6)
    private boolean advanced;    // did advance move (+9, forfeits shoot/charge)
    private boolean shot;        // has already shot this turn
    private boolean charged;     // has already charged this turn

    // ----- Move actions -----

    public void doHold() {
        // not moving is the default, just mark as "handled for movement phase"
        this.moved = true; // "moved" conceptually = we resolved movement
    }

    public void doStandardMove() {
        this.moved = true;
    }

    public void doAdvance() {
        this.advanced = true;
        this.moved = true;
    }

    // ----- Combat actions -----

    public void doShoot() {
        this.shot = true;
    }

    public void doCharge() {
        this.charged = true;
    }

    // ----- Query: what can this squad do? -----

    /** Can perform standard move this turn? */
    public boolean canMoveStandard() {
        return !moved && !advanced;
    }

    /** Can perform advance move this turn? */
    public boolean canAdvance() {
        return !moved && !advanced;
    }

    /** Can shoot this turn? */
    public boolean canShoot() {
        return moved && !advanced && !shot && !charged;
    }

    /** Can charge this turn? */
    public boolean canCharge() {
        return moved && !advanced && !charged;
    }

    /** Has this squad done everything it can this turn? */
    public boolean isDone() {
        return advanced || charged;
    }

    /** Has the movement phase action been resolved? */
    public boolean hasMoved() {
        return moved;
    }

    // ----- Display -----

    public String getStatusText() {
        if (advanced) return "ADVANCED";
        if (charged)  return "CHARGED";
        if (shot)     return "SHOT";
        if (moved)    return "MOVED";
        return "READY";
    }
}
