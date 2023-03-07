package com.tcc.pathfinderapi.pathing.pathoptimizers;

import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import com.tcc.pathfinderapi.pathing.PathStepResult;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class WindowOptimizer implements PathOptimizer {

    /**
     * Denotes offset (in path index / blocks) to start with. Larger indicates more optimization, but more computation
     */
    private final int INITIAL_BLOCK_OFFSET = 1000;

    /**
     * Decimal < 1 denoting how block offset should change with each loop. A higher value means more computation and more optimization
     */
    private final float MULTIPLIER = 0.8F;


    /**
     * Original path that should be optimized
     */
    private LinkedList<Coordinate> fullPath;
    private World world;
    private int currentBlockOffset;

    private PathFinder pathFinder;

    public WindowOptimizer(LinkedList<Coordinate> fullPath, World world, PathFinder pathFinder){
        this.fullPath = fullPath;
        this.world = world;
        this.pathFinder = pathFinder;
        currentBlockOffset = INITIAL_BLOCK_OFFSET;
    }

    @Override
    public PathStepResponse stepOptimize() {

        // Sliding window to find mini paths
        while(currentBlockOffset > 3){ // Offset of 3 indicates a sub-path length of 3. There is no way to optimize a 3 block long path to become shorter, so this is the lower-bound

            // Index in inflectionPoints corresponding to start of current window
            int firstWindowIndex = 0;

            // While another valid window of size exists
            while(firstWindowIndex < fullPath.size() - 1){
                int lastWindowIndex = firstWindowIndex + Math.min(firstWindowIndex + currentBlockOffset, fullPath.size());
                int maxPathLength = lastWindowIndex - firstWindowIndex - 1;

                // Find most promising locations to path between
                int[] coordIndices = getBestPair(firstWindowIndex, lastWindowIndex);
                if(coordIndices == null) {
                    firstWindowIndex += currentBlockOffset / 2;
                    continue;
                }
                int startCoordIndex = coordIndices[0];
                int endCoordIndex = coordIndices[1];

                // Attempt to find shorter path between locations
                Coordinate startCoord = fullPath.get(startCoordIndex);
                Location start = new Location(world, startCoord.getX(), startCoord.getY(), startCoord.getZ());
                Coordinate endCoord = fullPath.get(endCoordIndex - 1); // Pathfinder is inclusive for destination, so do a -1.
                Location end = new Location(world, endCoord.getX(), endCoord.getY(), endCoord.getZ());
                List<Coordinate> shorterSubPath = findPath(start, end, maxPathLength);

                // If shorter path found, update fullPath and update inflectionPoints within range
                if(shorterSubPath != null){
                    // Direct access to sublist
                    List<Coordinate> subList = fullPath.subList(startCoordIndex, endCoordIndex);
                    subList.clear();
                    subList.addAll(shorterSubPath);
                }

                firstWindowIndex += currentBlockOffset / 2;
            }
            currentBlockOffset *= MULTIPLIER;
        }

        return null;
    }

    /**
     * Finds a shorter path. Returns null if cannot find shorter path.
     * @param loc1
     * @param loc2
     * @param maxPathLength
     * @return
     */
    private List<Coordinate> findPath(Location loc1, Location loc2, int maxPathLength){
        try {
            return pathFinder.toBuilder()
                    .setMaxPathLength(maxPathLength).build().run().getPath().get();
        } catch (InterruptedException | ExecutionException e) {
            // do nothing
            return null;
        }
    }

    /**
     *
     * @param lowerIndex
     * @param upperIndex
     * @return null if no matches
     */
    private int[] getBestPair(int lowerIndex, int upperIndex){
        Iterator<Coordinate> it = fullPath.listIterator(lowerIndex);

        // Rename upperIndex to upperLimit
        int upperLimit = upperIndex;

        int[] bestMatch = null;
        int bestScore = (int) (0.1 * (upperIndex - lowerIndex)); // Don't attempt to optimize unless it's possible to optimize path length by more than 10%

        for(; lowerIndex < upperIndex; ++lowerIndex){
            Coordinate prev = it.next();
            upperIndex = lowerIndex + 1;
            Iterator<Coordinate> upperIt = fullPath.listIterator(upperIndex);
            for(; upperIndex < upperLimit; ++upperIndex){
                Coordinate next = upperIt.next();

                // Calculate score
                int actualDistance = Math.abs(prev.getX() - next.getX()) + Math.abs(prev.getZ() - next.getZ()); // manhatten
                int pathDistance = upperIndex - lowerIndex;
                int score = pathDistance - actualDistance;

                if(score > bestScore){
                    bestScore = score;
                    bestMatch = new int[]{lowerIndex, upperIndex};
                }
            }
        }

        return bestMatch;
    }





    /**
     * Optimizes the section of path from lowerIndex (inclusive) to upperIndex (exclusive)
     * @param lowerIndex - must be > 0 and <= upperIndex
     * @param upperIndex - must be <= fullPath.size()
     * @return Number of blocks removed from the path section
     */
    private int optimizeWindow(int lowerIndex, int upperIndex){
        // Bounds check
        if(upperIndex > fullPath.size()) return 0;

        return -1;
    }

}
