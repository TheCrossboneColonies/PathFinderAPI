package com.tcc.pathfinderapi.pathing.pathoptimizers;

import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class WindowOptimizer implements PathOptimizer {

    // Denotes offset (in path index / blocks) to start with. Larger indicates more optimization, but more computation.
    private final int INITIAL_BLOCK_OFFSET = 1024;

    // Decimal < 1 denoting how block offset should change with each loop. A higher value means more computation and more optimization.
    private final float MULTIPLIER = 0.25F; // Was 0.8F

    // Original path that should be optimized.
    private LinkedList<Coordinate> fullPath;
    private World world;

    private int currentBlockOffset;
    private PathFinder pathFinder;

    public WindowOptimizer (LinkedList<Coordinate> fullPath, World world, PathFinder pathFinder) {

        this.fullPath = fullPath;
        this.world = world;
        this.pathFinder = pathFinder;

        this.currentBlockOffset = this.INITIAL_BLOCK_OFFSET;
    }

    /**
     * In its current state, performs all optimizations in a single pass. Do NOT call this synchronously.
     */
    @Override
    public PathStepResponse optimize () {

        long startTime = System.currentTimeMillis();

        while (this.currentBlockOffset > 15) {

            int firstWindowIndex = 0;

            // TODO: REMOVE
            int initialLength = this.fullPath.size();

            // While another valid window of size exists.
            while (firstWindowIndex < this.fullPath.size() - 1) {

                int lastWindowIndex = Math.min(firstWindowIndex + currentBlockOffset, this.fullPath.size());

                int[] coordIndices = this.getBestPair(firstWindowIndex, lastWindowIndex);

                if (coordIndices == null) {

                    firstWindowIndex += currentBlockOffset / 2;
                    continue;
                }

                int startCoordIndex = coordIndices[0];
                int endCoordIndex = coordIndices[1];
                int maxPathLength = endCoordIndex - startCoordIndex - 1;

                Coordinate startCoordinate = this.fullPath.get(startCoordIndex);
                Location start = new Location(world, startCoordinate.getX(), startCoordinate.getY(), startCoordinate.getZ());

                Coordinate endCoordinate = this.fullPath.get(endCoordIndex - 1); // Pathfinder is inclusive for destination, so do a -1.
                Location end = new Location(world, endCoordinate.getX(), endCoordinate.getY(), endCoordinate.getZ());

                List<Coordinate> shorterSubPath = this.findPath(start, end, maxPathLength);

                if (shorterSubPath != null) {

                    List<Coordinate> subList = this.fullPath.subList(startCoordIndex, endCoordIndex);
                    subList.clear();
                    subList.addAll(shorterSubPath);
                }

                firstWindowIndex += currentBlockOffset / 2;
            }

            PathAPIMessager.debug("Window Length: " + currentBlockOffset + ", Removed " + (initialLength - fullPath.size()) + " Blocks");
            currentBlockOffset *= MULTIPLIER;
        }

        PathAPIMessager.debug("Optimized in " + (System.currentTimeMillis() - startTime) + "ms");
        return null;
    }

    /**
     * Finds a shorter path. Returns null if cannot find shorter path.
     */
    private List<Coordinate> findPath (Location locationOne, Location locationTwo, int maxPathLength) {

        try {

            return pathFinder.toBuilder()
                .setMaxPathLength(maxPathLength)
                .setStart(locationOne)
                .setEnd(locationTwo)
                .build()
                .run()
                .getPath()
                .get();
        } catch (InterruptedException | ExecutionException exception) { return null; }
    }

    /**
     * @param lowerIndex - inclusive
     * @param upperIndex - exclusive
     * @return null if no matches.
     */
    private int[] getBestPair (int lowerIndex, int upperIndex) {

        Iterator<Coordinate> iterator = fullPath.listIterator(lowerIndex);
        int upperLimit = upperIndex;

        int[] bestMatch = null;
        int bestScore = (int) (0.15 * (upperIndex - lowerIndex));

        for(; lowerIndex < upperLimit - 1; ++lowerIndex) {

            Coordinate previous = iterator.next();
            upperIndex = lowerIndex + 1;

            Iterator<Coordinate> upperIterator = fullPath.listIterator(upperIndex);

            for(; upperIndex < upperLimit; ++upperIndex) {

                Coordinate next = upperIterator.next();

                /**
                 * Calculate Score:
                 * 
                 * int diffX = prev.getX() - next.getX();
                 * int diffZ = prev.getZ() - next.getZ();
                 * float actualDistance = (float) Math.sqrt(diffX * diffX + diffZ * diffZ); // Euclidean
                 */
                int actualDistance = Math.abs(previous.getX() - next.getX()) + Math.abs(previous.getZ() - next.getZ()); // manhatten
                int pathDistance = upperIndex - lowerIndex;
                int score = pathDistance - actualDistance;

                if (score > bestScore) {

                    bestScore = score;
                    bestMatch = new int[]{lowerIndex, upperIndex};
                }
            }
        }

        return bestMatch;
    }
}
