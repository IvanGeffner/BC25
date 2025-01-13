package basic6;

import battlecode.common.*;

public class Map {

    static int[][] map;

    static RobotController rc;

    /*
     Bit 0 -> Not Wall / Wall
     Bit 1 -> Not Ruin / Ruin
     Bit 2 -> Not Enemy / Enemy
     Bit 3 -> Not Ally / Ally
     Bit 4 -> []
     Bit 5-9 -> RuinType
     Bit 10-20 -> Round Enemy Paint
     Bit 21 -> 31 Round Nearby Ruins
     */


    //static final int NEARBY_TILES_WITH_ENEMY_PAINT = 16;
    //static final int NT_C = 0xFFFFFFFF - NEARBY_TILES_WITH_ENEMY_PAINT;
    static final int ENEMY_TOWER = 8;
    static final int ENEMY_TOWER_C = ~ENEMY_TOWER;
    static final int ALLY_TOWER = 4;
    static final int ALLY_TOWER_C = ~ALLY_TOWER;
    static final int RUIN = 2;
    static final int WALL = 1;
    static int RUIN_TYPE_TYPE_SHIFT = 5;
    static int RUIN_TYPE_C = 0xFFFFFC1F;

    static int ROUND_ENEMY_PAINT_SHIFT = 10;
    static int ROUND_ENEMY_PAINT_C = 0xFFE003FF;

    static int ROUND_RUIN_SHIFT = 21;
    static int ROUND_RUIN_C = 0x001FFFFF;

    static int MIN_ROUNDS_ENEMY_PAINT = 45;
    static int MIN_ROUNDS_RUIN = 45;


    static void initialize(){
        map = new int[MyRobot.W][MyRobot.H];
    }

    static void fill() throws GameActionException {
        TowerManager.updateClosest();
        rc = MyRobot.rc;
        MapLocation[] ruins = MyRobot.rc.senseNearbyRuins(GameConstants.VISION_RADIUS_SQUARED);
        for (MapLocation m : ruins){
            int x = m.x, y = m.y;
            map[x][y] |= RUIN;
            RobotInfo r = MyRobot.rc.senseRobotAtLocation(m);
            if (r != null){
                if (r.getTeam() == rc.getTeam()){
                    map[x][y] |= ALLY_TOWER;
                    map[x][y] &= ENEMY_TOWER_C;
                    TowerManager.update(r);

                    map[x-2][y-2] &= ROUND_RUIN_C;
                    map[x-2][y-1] &= ROUND_RUIN_C;
                    map[x-2][y] &= ROUND_RUIN_C;
                    map[x-2][y+1] &= ROUND_RUIN_C;
                    map[x-2][y+2] &= ROUND_RUIN_C;

                    map[x-1][y-2] &= ROUND_RUIN_C;
                    map[x-1][y-1] &= ROUND_RUIN_C;
                    map[x-1][y] &= ROUND_RUIN_C;
                    map[x-1][y+1] &= ROUND_RUIN_C;
                    map[x-1][y+2] &= ROUND_RUIN_C;

                    map[x][y-2] &= ROUND_RUIN_C;
                    map[x][y-1] &= ROUND_RUIN_C;
                    map[x][y] &= ROUND_RUIN_C;
                    map[x][y+1] &= ROUND_RUIN_C;
                    map[x][y+2] &= ROUND_RUIN_C;

                    map[x+1][y-2] &= ROUND_RUIN_C;
                    map[x+1][y-1] &= ROUND_RUIN_C;
                    map[x+1][y] &= ROUND_RUIN_C;
                    map[x+1][y+1] &= ROUND_RUIN_C;
                    map[x+1][y+2] &= ROUND_RUIN_C;

                    map[x+2][y-2] &= ROUND_RUIN_C;
                    map[x+2][y-1] &= ROUND_RUIN_C;
                    map[x+2][y] &= ROUND_RUIN_C;
                    map[x+2][y+1] &= ROUND_RUIN_C;
                    map[x+2][y+2] &= ROUND_RUIN_C;
                }
                else{
                    map[x][y] |= ENEMY_TOWER;
                    map[x][y] &= ALLY_TOWER_C;
                }
            }
            else {
                int ruinType = RuinManager.checkPattern(m);
                int c = (((map[x][y] & RUIN_TYPE_C) & ALLY_TOWER_C) & ENEMY_TOWER_C);
                //MyRobot.debugLine += ("Pattern at " + m + " is " + c + ".   ");
                c |= (ruinType << RUIN_TYPE_TYPE_SHIFT);
                if ((ruinType & 16) != 0){
                    c &= ROUND_ENEMY_PAINT_C;
                    c |= ((rc.getRoundNum() + MIN_ROUNDS_ENEMY_PAINT) << ROUND_ENEMY_PAINT_SHIFT);
                }
                else if ((ruinType & 8) == 0){
                    c &= ROUND_ENEMY_PAINT_C;
                }
                //if (((c >>> ROUND_ENEMY_PAINT_SHIFT) & 0xFFF) <= rc.getRoundNum()) Unit.updateClosestRuin(m);
                map[x][y] = c;

                int rd = ((rc.getRoundNum() + MIN_ROUNDS_RUIN) << ROUND_RUIN_SHIFT);

                map[x-2][y-2] |= rd;
                map[x-2][y-1] |= rd;
                map[x-2][y] |= rd;
                map[x-2][y+1] |= rd;
                map[x-2][y+2] |= rd;

                map[x-1][y-2] |= rd;
                map[x-1][y-1] |= rd;
                map[x-1][y] |= rd;
                map[x-1][y+1] |= rd;
                map[x-1][y+2] |= rd;

                map[x][y-2] |= rd;
                map[x][y-1] |= rd;
                map[x][y] |= rd;
                map[x][y+1] |= rd;
                map[x][y+2] |= rd;

                map[x+1][y-2] |= rd;
                map[x+1][y-1] |= rd;
                map[x+1][y] |= rd;
                map[x+1][y+1] |= rd;
                map[x+1][y+2] |= rd;

                map[x+2][y-2] |= rd;
                map[x+2][y-1] |= rd;
                map[x+2][y] |= rd;
                map[x+2][y+1] |= rd;
                map[x+2][y+2] |= rd;


            }
        }
    }

    static int getPattern(MapLocation loc){
        return  ((map[loc.x][loc.y] >>> RUIN_TYPE_TYPE_SHIFT) & 0x1F);
    }

    static boolean enemyPainted(MapLocation loc){
        int c = map[loc.x][loc.y];
        return ((c >>> ROUND_ENEMY_PAINT_SHIFT) & 0x7FF) <= MyRobot.rc.getRoundNum();
    }

    static boolean invalidTarget(MapLocation loc){
        int c = map[loc.x][loc.y];
        //MyRobot.debugLine += ("Code at " + loc + " is " + c + ".   ");
        //rc.setIndicatorString(MyRobot.debugLine);
        if (((c >>> ROUND_ENEMY_PAINT_SHIFT) & 0x7FF) > MyRobot.rc.getRoundNum()) return true;
        return (c & 12) > 0;
    }

    static boolean isNearRuin(MapLocation loc){
        int c = map[loc.x][loc.y];
        return (((c >>> ROUND_RUIN_SHIFT) & 0x7FF) > MyRobot.rc.getRoundNum());
    }

}
