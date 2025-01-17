package basic14;

import battlecode.common.GameConstants;
import battlecode.common.UnitType;

public class Constants {

    static int INF = 1000000000;

    static int MIN_TRANSFER_PAINT = 5;

    static int CRITICAL_PAINT_SOLDIER = 35;
    static int CRITICAL_PAINT_MOPPER = 55;
    static int CRITICAL_PAINT_SPLASHER = 55;
    static int EXPLORE_TURNS = 20;

    static int NO_HEAL_CHIPS = 6000;

    static int MIN_SOLDIER_MOVEMENT = UnitType.SOLDIER.attackCost + GameConstants.PENALTY_ENEMY_TERRITORY + 1;

    static int MIN_PAINT_MOPPER_ATTACK = 5;

}
