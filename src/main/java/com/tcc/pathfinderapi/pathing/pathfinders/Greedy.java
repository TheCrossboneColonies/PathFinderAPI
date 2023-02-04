package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import com.tcc.pathfinderapi.pathing.PathStepResult;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Greedy extends PathFinder {

    // Algorithm variables
    private RoadCoordinate end;
    private RoadCoordinate start;
    private World world;
    private RoadCoordinate currentBackTraceNode;
    private PriorityQueue<RoadCoordinate> closedList;
    private PriorityQueue<RoadCoordinate> openList;

    private PathBuildStage currentStage = PathBuildStage.SEARCH;
    private int stepCount = 0;
    private long startSearchTime;
    private LinkedList<Coordinate> fullPath = new LinkedList<>();

    /**
     * Stores all roadcoordinates so neighbors can be easily found
     */
    private Map<Long, RoadCoordinate> masterList;

    // Configuration variables
    private final double LIQUID_PENALTY = 20;
    private final int LIQUID_RADIUS = 2;
    private final double CLIFF_PENALTY = 20;
    private final int CLIFF_RADIUS = 2;

    public Greedy(Location start, Location end){
        super(start, end);

        // Check start and end in same world
        // Note: This is temporary until we get multi-world support
        if(!checkSameWorld(start, end)) throw new RuntimeException("Start and end locations not in same world!");

        this.world = start.getWorld();
        //Start and end swapped so path is generated from start to end (due to backtracing)
        this.start = new RoadCoordinate(start.getBlockX(), start.getBlockY(), start.getBlockZ(), false);
        this.end = new RoadCoordinate(end.getBlockX(), end.getBlockY(), end.getBlockZ(), false);

        //Pathing variables
        closedList = new PriorityQueue<RoadCoordinate>();
        openList = new PriorityQueue<RoadCoordinate>();
        masterList = new HashMap<Long, RoadCoordinate>();
        masterList.put(coordinateToLong(this.start.coordLoc), this.start);
        masterList.put(coordinateToLong(this.end.coordLoc), this.end);
        this.start.h = heuristicDistance(this.end);
        openList.add(this.start);

    }

    @Override
    protected void onStart(){
        startSearchTime = System.currentTimeMillis();
    }


    @Override
    protected PathStepResponse step() {
        if(currentStage == PathBuildStage.SEARCH){
            stepSearch();
            return new PathStepResponse(PathStepResult.CONTINUE);
        }
        else if(currentStage == PathBuildStage.BACKTRACE){
            stepBacktrace();
            return new PathStepResponse(PathStepResult.CONTINUE);
        }
        else if(currentStage == PathBuildStage.SUCCESS) {
            PathStepResponse response = new PathStepResponse(PathStepResult.SUCCESS);
            response.addMetaData("path", fullPath);
            return response;
        }

        PathStepResponse response = new PathStepResponse(PathStepResult.ERROR);
        response.addMetaData("error_message", "Path could not be found");
        return response;
    }

    private boolean stepSearch() {

        if(openList.isEmpty()) {
            PathAPIMessager.debug(ChatColor.translateAlternateColorCodes('&', "&cError: No path found! Open list empty"));
            currentStage = PathBuildStage.ERROR;
            return true; //Error, no path found
        }

        Predicate<Integer> keepSearchingCheck = (stepCount) -> {
            int MIN_STEP_COUNT = Math.abs(this.start.coordLoc.getX() - this.end.coordLoc.getX())
                    + Math.abs(this.start.coordLoc.getY() - this.end.coordLoc.getY())
                    + Math.abs(this.start.coordLoc.getZ() - this.end.coordLoc.getZ());
            return stepCount <= 10 * MIN_STEP_COUNT;
        };

        //Check if step count has surpassed the limit
        if(!keepSearchingCheck.test(stepCount)) {
            PathAPIMessager.debug(ChatColor.translateAlternateColorCodes('&', "&cError: No path found! Too many steps taken"));
            currentStage = PathBuildStage.ERROR;
            return true; //Error, no path found
        }
        ++stepCount;

        RoadCoordinate n = openList.peek();

        if(n == end){
            PathAPIMessager.debug(ChatColor.translateAlternateColorCodes('&', "&aPath found! Backtracing started." +
                    " Path search time: " + (System.currentTimeMillis() - startSearchTime) + " ms"));
            currentBackTraceNode = end;
            currentStage = PathBuildStage.BACKTRACE;

            return true; //Path found successfully!
        }

        //Loop through neighbors. Loop order depends on which direction we want to prioritize (direction we want is checked last)
        for(RoadCoordinate m : n.getNeighbors(masterList)){
            //If undiscovered
            if(!openList.contains(m) && !closedList.contains(m)){
                m.parent = n;
                m.h = heuristicDistance(m);
                openList.add(m);
            }

        }

        openList.remove(n);
        closedList.add(n);

        return false;
    }

    private boolean stepBacktrace() {
        final int OPTIMIZATION_SEGMENT_LENGTH = 40;
        if(!currentBackTraceNode.equals(start)) {


            fullPath.addFirst(currentBackTraceNode.coordLoc);

            //TODO: Optimize path length





            currentBackTraceNode = currentBackTraceNode.parent;
            return false;
        }
        else {
            //Add start location
            fullPath.addFirst(currentBackTraceNode.coordLoc);

            //TODO: Optimize path length one last time


            PathAPIMessager.debug(ChatColor.translateAlternateColorCodes('&', "&cBacktracing complete! Path length: " + fullPath.size()));

            currentStage = PathBuildStage.SUCCESS;

            return true;
        }
    }

    /*
     * PRIVATE HELPER FUNCTIONS...
     */


    /**
     * Approximate weight
     * @param currentLoc
     * @return
     */
    private double heuristicDistance(RoadCoordinate currentLoc) {

        double weight = currentLoc.coordLoc.distance(end.coordLoc);

        //Give punishment for water
        currentLoc.isNearLiquid = isCoordNearLiquid(currentLoc);
        if(currentLoc.isNearLiquid) {
            weight += LIQUID_PENALTY;
        }

        //Give punishment for cliff (non air blocks only)
        if(!currentLoc.isAir) {
            currentLoc.isNearCliff = isCoordNearCliff(currentLoc);
            if(currentLoc.isNearCliff) {
                weight += CLIFF_PENALTY;
            }
        }

        return  weight;
    }

    /**
     * Likely a laggy operation as chunks need to be loaded to check the block type
     * @param coord - coordinate to check
     * @return
     */
    private boolean isCoordNearLiquid(RoadCoordinate coord) {
        if(coord.parent == null || coord.parent.isNearLiquid) {
            for(int xOffset = -1 * LIQUID_RADIUS; xOffset <= LIQUID_RADIUS; ++xOffset) {
                for(int yOffset = -1 * LIQUID_RADIUS; yOffset <= LIQUID_RADIUS; ++yOffset) {
                    for(int zOffset = -1 * LIQUID_RADIUS; zOffset <= LIQUID_RADIUS; ++zOffset) {
                        Location loc = new Location(world, coord.coordLoc.getX() + xOffset,
                                coord.coordLoc.getY() + yOffset, coord.coordLoc.getZ() + zOffset);
                        if(loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.LAVA) return true;
                    }
                }
            }
        }
        //Parent is NOT near liquid - check just a few blocks to ensure still not near liquid
        //Assumes parent has no coordinate difference of > 1 in x, y, or z
        else {
            int xDiff = coord.coordLoc.getX() - coord.parent.coordLoc.getX();
            int yDiff = coord.coordLoc.getY() - coord.parent.coordLoc.getY();
            int zDiff = coord.coordLoc.getZ() - coord.parent.coordLoc.getZ();
            //offsets must be greater than radius from parent
            for(int xOffset = -1 * LIQUID_RADIUS; xOffset <= LIQUID_RADIUS; ++xOffset) {
                boolean isWithinParentX = Math.abs(xDiff + xOffset) <= LIQUID_RADIUS;
                for(int yOffset = -1 * LIQUID_RADIUS; yOffset <= LIQUID_RADIUS; ++yOffset) {
                    boolean isWithinParentY = Math.abs(yDiff + yOffset) <= LIQUID_RADIUS;
                    for(int zOffset = -1 * LIQUID_RADIUS; zOffset <= LIQUID_RADIUS; ++zOffset) {
                        boolean isWithinParentZ = Math.abs(zDiff + zOffset) <= LIQUID_RADIUS;
                        if(isWithinParentX && isWithinParentY && isWithinParentZ) continue;
                        Location loc = new Location(world, coord.coordLoc.getX() + xOffset,
                                coord.coordLoc.getY() + yOffset, coord.coordLoc.getZ() + zOffset);
                        if(loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.LAVA) return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Likely a laggy operation as chunks need to be loaded to check block type.
     * Checks for a column of air blocks 3 below the block up to 2 above
     * @param coord
     * @return
     */
    private boolean isCoordNearCliff(RoadCoordinate coord) {
        for(int xOffset = -1 * CLIFF_RADIUS; xOffset <= CLIFF_RADIUS; ++xOffset) {
            for(int zOffset = -1 * CLIFF_RADIUS; zOffset <= CLIFF_RADIUS; ++zOffset) {
                boolean columnIsAir = true;
                for(int yOffset = -3; yOffset <= 2; ++yOffset) {
                    Location loc = new Location(world, coord.coordLoc.getX() + xOffset,
                            coord.coordLoc.getY() + yOffset, coord.coordLoc.getZ() + zOffset);
                    if(!loc.getBlock().getType().isAir()) {
                        columnIsAir = false;
                        break;
                    }
                }
                if(columnIsAir) return true;
            }
        }
        return false;
    }

    //Use RoadCoordinates to avoid use of redundant data found in Bukkit's Location object
    private class RoadCoordinate implements Comparable<RoadCoordinate>{
        Coordinate coordLoc;

        boolean isNearLiquid = false;
        boolean isNearCliff = false;
        boolean isAir = false;

        double h;

        RoadCoordinate parent = null; //Previous location used for backtracking

        private List<RoadCoordinate> neighbors;

        RoadCoordinate(int x, int y, int z, boolean isAir){
            coordLoc = new Coordinate(x, y, z);
            this.isAir = isAir;
        }

        //Copy constructor
        RoadCoordinate(RoadCoordinate coord){
            coordLoc = new Coordinate(coord.coordLoc.getX(), coord.coordLoc.getY(), coord.coordLoc.getZ());
            this.isAir = coord.isAir;
        }

        List<RoadCoordinate> getNeighbors(Map<Long, RoadCoordinate> coordMap){
            if(neighbors != null) return neighbors;
            List<RoadCoordinate> neighbors = new ArrayList<RoadCoordinate>();

            //Get neighbors top down
            RoadCoordinate posX = getOrCreateCoordinateIncline(coordLoc.getX() + 1, coordLoc.getY(), coordLoc.getZ(), coordMap);
            if(posX != null) neighbors.add(posX);
            RoadCoordinate negX = getOrCreateCoordinateIncline(coordLoc.getX() - 1, coordLoc.getY(), coordLoc.getZ(), coordMap);
            if(negX != null) neighbors.add(negX);
            RoadCoordinate posZ = getOrCreateCoordinateIncline(coordLoc.getX(), coordLoc.getY(), coordLoc.getZ() + 1, coordMap);
            if(posZ != null) neighbors.add(posZ);
            RoadCoordinate negZ = getOrCreateCoordinateIncline(coordLoc.getX(), coordLoc.getY(), coordLoc.getZ() - 1, coordMap);
            if(negZ != null) neighbors.add(negZ);


            this.neighbors = neighbors;
            return this.neighbors;
        }


        @Override
        public int compareTo(RoadCoordinate other) {
            return Double.compare(this.h, other.h);
        }

    }

    /**
     * Variation of getOrCreateCoordinate that finds coordinate one above or one below specified y value
     * - since it's impossible for a more than one coordinate to exist in a span of 3 blocks with only difference being in y
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Nullable
    private RoadCoordinate getOrCreateCoordinateIncline(int x, int y, int z, Map<Long, RoadCoordinate> coordMap) {
        for(int adjustedY = y + 1; adjustedY >= y - 1; --adjustedY) {
            RoadCoordinate coord = getOrCreateCoordinate(x, adjustedY, z, coordMap);
            if(coord != null) return coord;
        }
        return null;
    }

    private RoadCoordinate getOrCreateCoordinate(int x, int y, int z, Map<Long, RoadCoordinate> coordMap) {
        Long currentLong = coordinateToLong(x, y, z);
        RoadCoordinate currentCoord = coordMap.get(currentLong);

        if(currentCoord == null) {
            if(isValidSolidCoordinate(x, y, z)) {
                currentCoord = new RoadCoordinate(x, y, z, false);
                coordMap.put(currentLong, currentCoord);
            }
        }
        return currentCoord;
    }


    private boolean isValidSolidCoordinate(int x, int y, int z) {
        Location loc = new Location(world, x, y, z);
        Location oneAbove = new Location(world, x, y + 1, z);
        Location twoAbove = new Location(world, x, y + 2, z);
        Material locMat = loc.getBlock().getType();
        Material oneAboveMat = oneAbove.getBlock().getType();
        Material twoAboveMat = twoAbove.getBlock().getType();
        if((locMat.isSolid() || locMat == Material.WATER || locMat == Material.LAVA) && !locMat.toString().contains("LEAVES")) {
            if(!oneAboveMat.isSolid() && oneAboveMat != Material.WATER && oneAboveMat != Material.LAVA) {
                if(!twoAboveMat.isSolid() && twoAboveMat != Material.WATER && twoAboveMat != Material.LAVA) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean checkSameWorld(Location loc1, Location loc2){
        return loc1.getWorld() == loc2.getWorld();
    }

    private Long coordinateToLong(final Coordinate coordLoc) {
        return ((long) coordLoc.getX() & 67108863) << 38 | ((long) coordLoc.getY() & 4095)
                << 26 | ((long) coordLoc.getZ() & 67108863);
    }

    private Long coordinateToLong(final int x, final int y, final int z) {
        return ((long) x & 67108863) << 38 | ((long) y & 4095) << 26 | ((long) z & 67108863);
    }

    private Coordinate longToCoordinate(long l) {
        return new Coordinate((int)(l >> 38), (int)(l << 26 >> 52), (int)(l << 38 >> 38));
    }

    private enum PathBuildStage {
        SEARCH, BACKTRACE, ERROR, SUCCESS;
    }
}
