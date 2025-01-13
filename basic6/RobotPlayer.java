package basic6;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {


    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        MyRobot myRobot = switch (rc.getType()) {
            case MOPPER -> new Mopper(rc);
            case SOLDIER -> new Soldier(rc);
            case SPLASHER -> new Splasher(rc);
            default -> new Tower(rc);
        };

        while (true) {
            myRobot.startTurn();
            myRobot.runTurn();
            myRobot.endTurn();
            Clock.yield();
        }
    }
}
