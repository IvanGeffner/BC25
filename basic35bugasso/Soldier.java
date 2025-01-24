package basic35bugasso;

import battlecode.common.*;

public class Soldier extends Unit {

    boolean recovering = false;
    Soldier(RobotController rc) throws GameActionException {
        super(rc);
        Map.initialize();
        Map.fill();
    }

    void startTurn() throws GameActionException {
        updateClosestRuin();
        ResourcePatternManager.attackLoc = null;
    }

    void endTurn() throws GameActionException {
        super.endTurn();
    }

    void runTurn() throws GameActionException {
        attackTowers();
        completePatterns();
        tryWithdraw();
        if (shouldRecover()) recovering = true;
        if (rc.getPaint() >= UnitType.SOLDIER.paintCapacity - Constants.MIN_TRANSFER_PAINT) recovering = false;
        move();
        paint();
        tryWithdraw();
        completePatterns();
    }

    void paintNearby() throws GameActionException {
        if (rc.getPaint() < Constants.CRITICAL_PAINT_SOLDIER) return;
        if (!rc.isActionReady()) return;
        /*MapLocation myLoc = rc.getLocation();
        if (!rc.isActionReady()) return;
        MapInfo[] infos = rc.senseNearbyMapInfos(4);
        for (MapInfo m : infos){
            if (!Util.towerMax() && Map.isNearRuin(m.getMapLocation())) return;
            int x = m.getMapLocation().x, y = m.getMapLocation().y;
            PaintType targetPaint = ((x + y)%2 == 0 || (x + 7*y)%10 == 1) ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY;
            if (m.getPaint().isEnemy() || m.getPaint() == targetPaint) continue;
            if (rc.canAttack(m.getMapLocation())) rc.attack(m.getMapLocation(), targetPaint == PaintType.ALLY_SECONDARY);
        }*/
        MapLocation myLoc = rc.getLocation();
        tryPaint(myLoc);
        if (ResourcePatternManager.attackLoc != null) tryPaint(ResourcePatternManager.attackLoc);
        if(tryPaint(myLoc.translate(1,0))) return;
        if(tryPaint(myLoc.translate(0,1))) return;
        if(tryPaint(myLoc.translate(-1,0))) return;
        if(tryPaint(myLoc.translate(0,-1))) return;
        if(tryPaint(myLoc.translate(1,1))) return;
        if(tryPaint(myLoc.translate(1,-1))) return;
        if(tryPaint(myLoc.translate(-1,-1))) return;
        if(tryPaint(myLoc.translate(-1,1))) return;
        if(tryPaint(myLoc.translate(2,0))) return;
        if(tryPaint(myLoc.translate(0,2))) return;
        if(tryPaint(myLoc.translate(-2,0))) return;
        if(tryPaint(myLoc.translate(0,-2)))return;

        if (rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY){
            if (rc.canAttack(rc.getLocation())){
                int x = rc.getLocation().x%4, y = rc.getLocation().y%4;
                int z = x*4 + y;
                PaintType targetPaint = ((5147 >>> z)&1) > 0 ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
                rc.attack(rc.getLocation(), targetPaint == PaintType.ALLY_SECONDARY);
            }
        }
    }

    /*void paintSelf() throws GameActionException {
        if (!rc.canAttack(rc.getLocation())) return;
        MapLocation myLoc = rc.getLocation();
        if (!Util.towerMax() && Map.isNearRuin(myLoc)) return;
        int dx = myLoc.x;
        int dy = myLoc.y;
        PaintType targetPaint = ((dx + dy)%2 == 0 || (dx + 7*dy)%10 == 1) ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY;
        PaintType cp = rc.senseMapInfo(rc.getLocation()).getPaint();
        if (cp.isEnemy()) return;
        if (cp == targetPaint) return;
        rc.attack(rc.getLocation(), targetPaint == PaintType.ALLY_SECONDARY);
    }*/

    boolean tryPaint(MapLocation loc) throws GameActionException {
        if (!rc.canSenseLocation(loc)) return false;
        MapInfo m = rc.senseMapInfo(loc);
        if (m.isWall() || m.hasRuin() || !ResourcePatternManager.shouldPaint(loc)) return false;
        if (!Util.towerMax() && Map.isNearRuin(m.getMapLocation())) return false;
        int x = m.getMapLocation().x%4, y = m.getMapLocation().y%4;
        int z = x*4 + y;
        //PaintType targetPaint = ((x + y)%2 == 0 || (x + 7*y)%10 == 1) ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY;
        PaintType targetPaint = ((5147 >>> z)&1) > 0 ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
        if (m.getPaint().isEnemy() || m.getPaint() == targetPaint) return false;
        if (rc.canAttack(m.getMapLocation())){
            rc.attack(m.getMapLocation(), targetPaint == PaintType.ALLY_SECONDARY);
            return true;
        }
        return false;
    }

    MapLocation getClosestEmptyTile(){
        MapInfo[] infos = rc.senseNearbyMapInfos();
        MapLocation ans = null;
        int bestDist = -1;
        for (MapInfo mi : infos){
            if (mi.getPaint() != PaintType.EMPTY) continue;
            if (Map.isNearRuin(mi.getMapLocation())) continue;
            if (!mi.isPassable()) continue;
            int newD = rc.getLocation().distanceSquaredTo(mi.getMapLocation());
            if (bestDist < 0 || newD < bestDist){
                ans = mi.getMapLocation();
                bestDist = newD;
            }
        }
        return ans;
    }

    void attackTowers() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : robots){
            if (!r.getType().isTowerType()) continue;
            if (rc.canAttack(r.getLocation())) rc.attack(r.getLocation());
        }
    }

    MapLocation getClosestEnemyTower() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation bestLoc = null;
        int bestDist = 0;
        for (RobotInfo r : robots){
            if (!r.getType().isTowerType()) continue;
            int dist = r.getLocation().distanceSquaredTo(rc.getLocation());
            if (bestLoc == null || dist < bestDist){
                bestLoc = r.getLocation();
                bestDist = dist;
            }
        }
        return bestLoc;
    }

    boolean shouldRecover(){
        if (rc.getRoundNum() > Constants.MIN_ROUNDS_NO_RECOVERY && rc.getNumberTowers() > Constants.MIN_TOWERS_NO_RECOVERY) return false;
        return (rc.getPaint() < Constants.CRITICAL_PAINT_SOLDIER);
    }

    void move() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (MicroManagerSoldier.doMicro()) return;
        MapLocation target = getTarget();
        pathfinding.moveTo(target);
    }

    MapLocation getTarget() throws GameActionException{
        MapLocation tg = null;
        if (recovering && !suicide && TowerManager.closestPaintTower != null){
            tg = getRecoveryLoc();
            if (tg != null) return tg;
        }
        tg = getClosestEnemyTower();
        if (tg != null) return tg;
        if (closestRuin != null && !Util.towerMax()) return closestRuin;
        if (rc.getRoundNum() > creationTurn){
        tg = ResourcePatternManager.getBestTarget();
        if (tg != null){
            //if (ResourcePatternManager.attackLoc != null) //rc.setIndicatorDot(ResourcePatternManager.attackLoc, 200, 0, 0);
            //if (ResourcePatternManager.center != null) //rc.setIndicatorDot(ResourcePatternManager.center, 0, 0, 200);
            return tg;
        }
        }
        if (rc.getRoundNum() > 200){
            MapLocation loc = getClosestEmptyTile();
            if (loc != null) return loc;
        }
        return explore.getExplore3Target();
    }


    void paint() throws GameActionException {
        attackTowers();
        if (closestRuin != null && Map.hasEnemyPaint(closestRuin)){
            RuinManager.drawPatternEnhanced(closestRuin, TowerManager.getNextBuild());
            return;
        }
        else if (closestRuin != null && rc.getLocation().distanceSquaredTo(closestRuin) <= 8 && !Util.towerMax()) {
            int x = TowerManager.getNextBuild();
            UnitType t = switch (x) {
                case RuinManager.PAINT -> UnitType.LEVEL_ONE_PAINT_TOWER;
                case RuinManager.MONEY -> UnitType.LEVEL_ONE_MONEY_TOWER;
                default -> UnitType.LEVEL_ONE_DEFENSE_TOWER;
            };
            if (rc.canCompleteTowerPattern(t, closestRuin)) {
                rc.completeTowerPattern(t, closestRuin);
            }
            RuinManager.drawPatternEnhanced(closestRuin, TowerManager.getNextBuild());
        }
        paintNearby();
    }

}
