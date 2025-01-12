package rush;

import battlecode.common.MapLocation;

public class Pathfinding {

        //static RobotController rc;
        static BugNav bugNav;

        Pathfinding(){
            //Pathfinding.rc = MyRobot.rc;
            bugNav = new BugNav();
        }

        void moveTo(MapLocation target){

            MyRobot.rc.setIndicatorString("Going to " + target.toString());
            bugNav.moveTo(target);
        }
}
