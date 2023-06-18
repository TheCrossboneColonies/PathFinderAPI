package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.*;
import org.bukkit.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class Greedy extends PathFinder {

    private RoadCoordinate end;
    private RoadCoordinate start;
    private World world;

    private RoadCoordinate currentBackTraceNode;
    private PriorityQueue<RoadCoordinate> closedList;
    private PriorityQueue<RoadCoordinate> openList;

    private PathBuildStage currentStage = PathBuildStage.SEARCH;
    private int stepCount = 0;

    private LinkedList<Coordinate> fullPath = new LinkedList<>();
    private Map<Long, RoadCoordinate> masterList;

    private final double LIQUID_PENALTY = 20;
    private final int LIQUID_RADIUS;
    private final double CLIFF_PENALTY = 20;
    private final int CLIFF_RADIUS;

    public Greedy (GreedyBuilder greedyBuilder) {
        
        super(greedyBuilder);

        // Note: This is temporary until we get multi-world support.
        if (!this.checkSameWorld(greedyBuilder.getStart(), greedyBuilder.getEnd())) throw new RuntimeException("Start and end locations not in same world!");

        this.LIQUID_RADIUS = greedyBuilder.LIQUID_RADIUS;
        this.CLIFF_RADIUS = greedyBuilder.CLIFF_RADIUS;

        this.world = greedyBuilder.getStart().getWorld();
        this.start = new RoadCoordinate(greedyBuilder.getStart().getBlockX(), greedyBuilder.getStart().getBlockY(), greedyBuilder.getStart().getBlockZ());
        this.end = new RoadCoordinate(greedyBuilder.getEnd().getBlockX(), greedyBuilder.getEnd().getBlockY(), greedyBuilder.getEnd().getBlockZ());

        this.closedList = new PriorityQueue<RoadCoordinate>();
        this.openList = new PriorityQueue<RoadCoordinate>();
        this.masterList = new HashMap<Long, RoadCoordinate>();
        this.masterList.put(coordinateToLong(this.start.coordinateLocation), this.start);
        this.masterList.put(coordinateToLong(this.end.coordinateLocation), this.end);

        this.start.heuristic = this.heuristicDistance(this.end);
        this.openList.add(this.start);
    }

    /**
     * Obtain a new {@link GreedyBuilder GreedyBuilder}.
     */
    public static GreedyBuilder getBuilder (Location start, Location end) { return new GreedyBuilder(start, end); }

    /**
     * Obtain a new {@link GreedyBuilder GreedyBuilder} using current instance.
     */
    @Override
    public PathBuilder toBuilder () {

        return new GreedyBuilder(super.getStart(), super.getEnd())
            .setCliffRadius(CLIFF_RADIUS)
            .setLiquidRadius(LIQUID_RADIUS);

        // TODO: Add Limits
    }


    @Override
    protected PathStepResponse step () {

        if (this.currentStage == PathBuildStage.SEARCH) {

            this.stepSearch();
            return new PathStepResponse(PathStepResult.CONTINUE);
        } else if (this.currentStage == PathBuildStage.BACKTRACE) {

            this.stepBacktrace();
            return new PathStepResponse(PathStepResult.CONTINUE);
        } else if (this.currentStage == PathBuildStage.SUCCESS) {

            PathStepResponse pathStepResponse = new PathStepResponse(PathStepResult.SUCCESS);
            pathStepResponse.addMetaData("path", fullPath);
            return pathStepResponse;
        }

        PathStepResponse pathStepResponse = new PathStepResponse(PathStepResult.ERROR);
        pathStepResponse.addMetaData("error_message", "Path Couldn't be Found");
        return pathStepResponse;
    }

    private boolean stepSearch () {

        if (this.openList.isEmpty()) {

            this.currentStage = PathBuildStage.ERROR;
            return true;
        }

        Predicate<Integer> keepSearchingCheck = (stepCount) -> {

            int MIN_STEP_COUNT = Math.abs(this.start.coordinateLocation.getX() - this.end.coordinateLocation.getX()) + Math.abs(this.start.coordinateLocation.getY() - this.end.coordinateLocation.getY()) + Math.abs(this.start.coordinateLocation.getZ() - this.end.coordinateLocation.getZ());
            return stepCount <= 10 * MIN_STEP_COUNT;
        };

        if (!keepSearchingCheck.test(stepCount)) {

            this.currentStage = PathBuildStage.ERROR;
            return true;
        }

        ++stepCount;

        RoadCoordinate n = openList.peek();

        if (n == this.end) {
            
            this.currentBackTraceNode = this.end;
            this.currentStage = PathBuildStage.BACKTRACE;
            return true;
        }

        for (RoadCoordinate m : n.getNeighbors(masterList)) {

            if (!this.openList.contains(m) && !this.closedList.contains(m)) {

                m.parent = n;
                m.heuristic = this.heuristicDistance(m);
                openList.add(m);
            }
        }

        this.openList.remove(n);
        this.closedList.add(n);
        return false;
    }

    private boolean stepBacktrace () {

        if (this.fullPath.size() > this.getMaxPathLength()) {

            this.currentStage = PathBuildStage.ERROR;
            return true;
        }

        if (!this.currentBackTraceNode.equals(this.start)) {

            this.fullPath.addFirst(this.currentBackTraceNode.coordinateLocation);
            this.currentBackTraceNode = this.currentBackTraceNode.parent;
            return false;
        } else {

            this.fullPath.addFirst(this.currentBackTraceNode.coordinateLocation);
            this.currentStage = PathBuildStage.SUCCESS;
            return true;
        }
    }

    private double heuristicDistance (RoadCoordinate currentLocation) {

        double weight = currentLocation.coordinateLocation.distance(this.end.coordinateLocation);

        currentLocation.isNearLiquid = this.isCoordNearLiquid(currentLocation);
        if (currentLocation.isNearLiquid) { weight += this.LIQUID_PENALTY; }

        currentLocation.isNearCliff = this.isCoordNearCliff(currentLocation);
        if (currentLocation.isNearCliff) { weight += CLIFF_PENALTY; }

        return  weight;
    }

    /**
     * Likely a laggy operation as chunks need to be loaded to check the block type.
     */
    private boolean isCoordNearLiquid (RoadCoordinate roadCoordinate) {

        if (roadCoordinate.parent == null || roadCoordinate.parent.isNearLiquid) {

            for (int ix = roadCoordinate.coordinateLocation.getX() - LIQUID_RADIUS; ix <= roadCoordinate.coordinateLocation.getX() + LIQUID_RADIUS; ++ix) {

                for (int iy = roadCoordinate.coordinateLocation.getY() - LIQUID_RADIUS; iy <= roadCoordinate.coordinateLocation.getY() + LIQUID_RADIUS; ++iy) {

                    for (int iz = roadCoordinate.coordinateLocation.getZ() - LIQUID_RADIUS; iz <= roadCoordinate.coordinateLocation.getZ() + LIQUID_RADIUS; ++iz) {

                        Material material = BlockManager.getBlockType(world, ix, iy, iz);
                        if (material == Material.WATER || material == Material.LAVA) return true;
                    }
                }
            }
        } else {

            for (int ix = roadCoordinate.coordinateLocation.getX() - LIQUID_RADIUS; ix <= roadCoordinate.coordinateLocation.getX() + LIQUID_RADIUS; ++ix) {

                boolean isWithinParentX = Math.abs(ix - roadCoordinate.parent.coordinateLocation.getX()) <= LIQUID_RADIUS;

                for (int iy = roadCoordinate.coordinateLocation.getY() - LIQUID_RADIUS; iy <= roadCoordinate.coordinateLocation.getY() + LIQUID_RADIUS; ++iy) {

                    boolean isWithinParentY = Math.abs(iy - roadCoordinate.parent.coordinateLocation.getY()) <= LIQUID_RADIUS;

                    for (int iz = roadCoordinate.coordinateLocation.getZ() - LIQUID_RADIUS; iz <= roadCoordinate.coordinateLocation.getZ() + LIQUID_RADIUS; ++iz) {

                        boolean isWithinParentZ = Math.abs(iz - roadCoordinate.parent.coordinateLocation.getZ()) <= LIQUID_RADIUS;

                        if (isWithinParentX && isWithinParentY && isWithinParentZ) continue;
                        Material material = BlockManager.getBlockType(world, ix, iy, iz);
                        if (material == Material.WATER || material == Material.LAVA) return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Likely a laggy operation as chunks need to be loaded to check block type.
     * Checks for a column of air blocks 3 below the block up to 2 above.
     */
    private boolean isCoordNearCliff (RoadCoordinate roadCoordinate) {

        for (int xOffset = -1 * CLIFF_RADIUS; xOffset <= CLIFF_RADIUS; ++xOffset) {

            for (int zOffset = -1 * CLIFF_RADIUS; zOffset <= CLIFF_RADIUS; ++zOffset) {

                boolean columnIsAir = true;

                for (int yOffset = -3; yOffset <= 2; ++yOffset) {

                    Material material = BlockManager.getBlockType(world, roadCoordinate.coordinateLocation.getX() + xOffset, roadCoordinate.coordinateLocation.getY() + yOffset, roadCoordinate.coordinateLocation.getZ() + zOffset);
                    if (!material.isAir()) {

                        columnIsAir = false;
                        break;
                    }
                }

                if(columnIsAir) return true;
            }
        }

        return false;
    }

    // Use RoadCoordinates to avoid use of redundant data found in Bukkit's Location object.
    private class RoadCoordinate implements Comparable<RoadCoordinate> {

        Coordinate coordinateLocation;
        boolean isNearLiquid = false;
        boolean isNearCliff = false;

        double heuristic;
        RoadCoordinate parent = null;

        private List<RoadCoordinate> neighbors;

        RoadCoordinate (int x, int y, int z) { this.coordinateLocation = new Coordinate(x, y, z); }

        List<RoadCoordinate> getNeighbors(Map<Long, RoadCoordinate> coordinateMap) {

            if (this.neighbors != null) return neighbors;
            List<RoadCoordinate> neighbors = new ArrayList<>();

            RoadCoordinate posX = getOrCreateCoordinateIncline(coordinateLocation.getX() + 1, coordinateLocation.getY(), coordinateLocation.getZ(), coordinateMap);
            if (posX != null) neighbors.add(posX);

            RoadCoordinate negX = getOrCreateCoordinateIncline(coordinateLocation.getX() - 1, coordinateLocation.getY(), coordinateLocation.getZ(), coordinateMap);
            if (negX != null) neighbors.add(negX);

            RoadCoordinate posZ = getOrCreateCoordinateIncline(coordinateLocation.getX(), coordinateLocation.getY(), coordinateLocation.getZ() + 1, coordinateMap);
            if (posZ != null) neighbors.add(posZ);

            RoadCoordinate negZ = getOrCreateCoordinateIncline(coordinateLocation.getX(), coordinateLocation.getY(), coordinateLocation.getZ() - 1, coordinateMap);
            if (negZ != null) neighbors.add(negZ);

            this.neighbors = neighbors;
            return this.neighbors;
        }

        @Override
        public int compareTo (RoadCoordinate otherRoadCoordinate) { return Double.compare(this.heuristic, otherRoadCoordinate.heuristic); }
    }

    /**
     * Variation of getOrCreateCoordinate that finds coordinate one above or one below specified y value
     * - since it's impossible for a more than one coordinate to exist in a span of 3 blocks with only difference being in y.
     */
    @Nullable
    private RoadCoordinate getOrCreateCoordinateIncline (int x, int y, int z, Map<Long, RoadCoordinate> coordinateMap) {

        for (int adjustedY = y + 1; adjustedY >= y - 1; --adjustedY) {

            RoadCoordinate roadCoordinate = getOrCreateCoordinate(x, adjustedY, z, coordinateMap);
            if (roadCoordinate != null) return roadCoordinate;
        }

        return null;
    }

    private RoadCoordinate getOrCreateCoordinate (int x, int y, int z, Map<Long, RoadCoordinate> coordinateMap) {

        Long currentLong = this.coordinateToLong(x, y, z);
        RoadCoordinate currentRoadCoordinate = coordinateMap.get(currentLong);

        if (currentRoadCoordinate == null) {

            if(this.isValidSolidCoordinate(x, y, z)) {

                currentRoadCoordinate = new RoadCoordinate(x, y, z);
                coordinateMap.put(currentLong, currentRoadCoordinate);
            }
        }

        return currentRoadCoordinate;
    }

    private boolean isValidSolidCoordinate (int x, int y, int z) {

        Material locationMaterial = BlockManager.getBlockType(world, x, y, z);
        Material oneAboveMaterial = BlockManager.getBlockType(world, x, y + 1, z);
        Material twoAboveMaterial = BlockManager.getBlockType(world, x, y + 2, z);

        if ((locationMaterial.isSolid() || locationMaterial == Material.WATER || locationMaterial == Material.LAVA) && !locationMaterial.toString().contains("LEAVES")) {

            if (!oneAboveMaterial.isSolid() && oneAboveMaterial != Material.WATER && oneAboveMaterial != Material.LAVA) {

                if (!twoAboveMaterial.isSolid() && twoAboveMaterial != Material.WATER && twoAboveMaterial != Material.LAVA) {

                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkSameWorld (Location locationOne, Location locationTwo) { return locationOne.getWorld() == locationTwo.getWorld(); }

    private Long coordinateToLong (final Coordinate coordinateLocation) { return ((long) coordinateLocation.getX() & 67108863) << 38 | ((long) coordinateLocation.getY() & 4095) << 26 | ((long) coordinateLocation.getZ() & 67108863); }
    private Long coordinateToLong (final int x, final int y, final int z) { return ((long) x & 67108863) << 38 | ((long) y & 4095) << 26 | ((long) z & 67108863); }

    public static class GreedyBuilder extends PathBuilder {

        private int LIQUID_RADIUS = 2;
        private int CLIFF_RADIUS = 2;

        public GreedyBuilder (Location start, Location end) { super(start, end); }

        public GreedyBuilder setLiquidRadius (int radius) {

            this.LIQUID_RADIUS = radius;
            return this;
        }

        public GreedyBuilder setCliffRadius (int radius) {

            this.CLIFF_RADIUS = radius;
            return this;
        }

        @Override
        public Greedy build () { return new Greedy(this); }
    }

    private enum PathBuildStage {
        
        SEARCH, BACKTRACE, ERROR, SUCCESS;
    }
}
