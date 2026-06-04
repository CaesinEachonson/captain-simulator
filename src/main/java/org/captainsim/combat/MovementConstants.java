package org.captainsim.combat;

/**
 * Movement distance constants for manual battle control.
 * All values are in grid cells (1 cell = 1 unit of distance).
 */
public final class MovementConstants {

    /** Standard move: unit can still shoot and charge after moving. */
    public static final int STANDARD_MOVE = 20;

    /** Advance (accelerated) move: unit cannot shoot or charge after moving. */
    public static final int ADVANCE_MOVE = 30;

    /** Maximum distance (Manhattan) for a successful charge into melee. */
    public static final int CHARGE_RANGE = 2;

    private MovementConstants() {}
}
