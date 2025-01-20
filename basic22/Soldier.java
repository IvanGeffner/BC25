package basic22;

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
        if (recovering) return;
        if (!rc.isActionReady()) return;

        if (rc.getRoundNum() > creationTurn) ResourcePatternManager.paintNearby();

        if (!Util.towerMax() && Map.isNearRuin(rc.getLocation())) return;
        if (rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY){
            if (rc.canAttack(rc.getLocation())){
                int x = rc.getLocation().x%4, y = rc.getLocation().y%4;
                int z = x*4 + y;
                PaintType targetPaint = ((5147 >>> z)&1) > 0 ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
                rc.attack(rc.getLocation(), targetPaint == PaintType.ALLY_SECONDARY);
            }
        }
    }

    static boolean tryPaint(MapLocation loc, PaintType t) throws GameActionException {
        if (!rc.canSenseLocation(loc)) return false;
        MapInfo m = rc.senseMapInfo(loc);
        //if (!m.isPassable() || !ResourcePatternManager.shouldPaint(loc)) return false;
        if (!Util.towerMax() && Map.isNearRuin(m.getMapLocation())) return false;
        if (m.getPaint().isEnemy() || m.getPaint() == t) return false;
        if (rc.canAttack(m.getMapLocation())){
            rc.attack(m.getMapLocation(), t == PaintType.ALLY_SECONDARY);
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
        if (rc.getChips() > Constants.NO_HEAL_CHIPS) return false;
        return (TowerManager.closestPaintTower != null && rc.getPaint() < Constants.CRITICAL_PAINT_SOLDIER);
    }

    void move() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (MicroManagerSoldier.doMicro()) return;
        MapLocation target = getTarget();
        pathfinding.moveTo(target);
    }

    MapLocation getTarget() throws GameActionException{
        if (recovering && TowerManager.closestPaintTower != null) return TowerManager.closestPaintTower;
        MapLocation tg = getClosestEnemyTower();
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
        /*if (closestRuin == null || rc.getLocation().distanceSquaredTo(closestRuin) > 8 || Util.towerMax()){
            paintSelf();
            paintNearby();
            return;
        }*/
        if (closestRuin != null && Map.hasEnemyPaint(closestRuin) && rc.canSenseLocation(closestRuin)){
            RuinManager.drawPatternEnhanced(closestRuin, TowerManager.getNextBuild());
            return;
        }
        else if (closestRuin != null && rc.getLocation().distanceSquaredTo(closestRuin) <= 8 && !Util.towerMax()) {
            int ruinType = Map.getPattern(closestRuin);
            //System.out.println("ruinType: " + ruinType);
            int trueType = RuinManager.getType(ruinType);
            switch (trueType) {
                case -1 -> chooseWisely(ruinType);
                default -> {
                    UnitType t = switch (trueType) {
                        case RuinManager.PAINT -> UnitType.LEVEL_ONE_PAINT_TOWER;
                        case RuinManager.MONEY -> UnitType.LEVEL_ONE_MONEY_TOWER;
                        default -> UnitType.LEVEL_ONE_DEFENSE_TOWER;
                    };
                    //System.out.println("Trying to complete tower of type " + t);
                    if (rc.canCompleteTowerPattern(t, closestRuin)) {
                        rc.completeTowerPattern(t, closestRuin);
                        //return;
                    }
                    else RuinManager.drawPatternEnhanced(closestRuin, trueType);
                }
            }
        }
        //paintSelf();
        paintNearby();
    }

    void chooseWisely(int ruinType) throws GameActionException{
        if ((ruinType & 8) > 0) paintRuinSafe();
        try {
            RuinManager.drawPatternEnhanced(closestRuin, TowerManager.getNextBuild());
        } catch(GameActionException e){
            e.printStackTrace();
            //System.out.println("My Location " + rc.getLocation());
            //System.out.println("Ruin Location " + closestRuin);
        }
    }

    void paintRuinSafe() throws GameActionException {
        MapLocation loc = closestRuin.add(Direction.NORTHEAST);
        if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).getPaint() == PaintType.EMPTY && rc.canAttack(loc)) rc.attack(loc, true);
        loc = closestRuin.add(Direction.SOUTHEAST);
        if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).getPaint() == PaintType.EMPTY && rc.canAttack(loc)) rc.attack(loc, true);
        loc = closestRuin.add(Direction.SOUTHWEST);
        if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).getPaint() == PaintType.EMPTY && rc.canAttack(loc)) rc.attack(loc, true);
        loc = closestRuin.add(Direction.NORTHWEST);
        if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).getPaint() == PaintType.EMPTY && rc.canAttack(loc)) rc.attack(loc, true);
    }

}
