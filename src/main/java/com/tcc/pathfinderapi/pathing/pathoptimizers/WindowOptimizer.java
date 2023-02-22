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
     * Sorted list of indices in fullPath representing changes in direction
     */
    private List<Integer> inflectionPoints = new ArrayList<>();

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

        // Initialize inflection points
        updateInflectionPoints(0, fullPath.size());
    }

    @Override
    public PathStepResponse stepOptimize() {

        // Inflection point check
        if(inflectionPoints.size() == 0) {
            PathStepResponse response = new PathStepResponse(PathStepResult.ERROR);
            response.addMetaData("error_message", "Inflection points not initialized! Length is 0.");
            return response;
        }

        // TODO: If LinkedList implementation is chosen, should probably use iterators

        // Sliding window to find mini paths
        while(currentBlockOffset > 3){ // Offset of 3 indicates a sub-path length of 3. There is no way to optimize a 3 block long path to become shorter, so this is the lower-bound

            // Index in inflectionPoints corresponding to start of current window
            int firstInflectionIndex = 0;

            // Index in fullPath corresponding to Coordinate of start (inclusive) of current window
            int lowerIndex = inflectionPoints.get(firstInflectionIndex);

            // Index in fullPath corresponding to Coordinate of end (exclusive) of current window
            int upperIndex = getUpperIndex(lowerIndex + currentBlockOffset + 1);

            // While another valid window of size exists
            while(upperIndex != -1){
                int maxPathLength = upperIndex - lowerIndex - 1;

                // Attempt to find shorter path between locations
                Coordinate startCoord = fullPath.get(lowerIndex);
                Location start = new Location(world, startCoord.getX(), startCoord.getY(), startCoord.getZ());
                Coordinate endCoord = fullPath.get(upperIndex - 1); // Pathfinder is inclusive for destination, so do a -1.
                Location end = new Location(world, endCoord.getX(), endCoord.getY(), endCoord.getZ());
                List<Coordinate> shorterSubPath = findPath(start, end, maxPathLength);

                // If shorter path found, update fullPath and update inflectionPoints within range
                if(shorterSubPath != null){
                    // Direct access to sublist
                    List<Coordinate> subList = fullPath.subList(lowerIndex, upperIndex);
                    subList.clear();
                    subList.addAll(shorterSubPath);
                    updateInflectionPoints(lowerIndex, upperIndex);
                }

                ++firstInflectionIndex;
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
     * Updates inflection points within the fullPath indices
     * @param lowerIndex
     * @param upperIndex
     */
    private void updateInflectionPoints(int lowerIndex, int upperIndex){

        // Find new inflection points in fullPath
        List<Integer> newInflectionPoints = getInflectionPoints(lowerIndex, upperIndex);

        int lowerInflectionIndex = inflectionPoints.indexOf(lowerIndex);

        // Remove old inflection points between bounds
        inflectionPoints.removeIf((value) -> value >= lowerIndex && value < upperIndex);

        //Add new inflection points
        inflectionPoints.addAll(lowerInflectionIndex, newInflectionPoints);

        // TODO: Assuming the above is implemented correctly, the list is already sorted! Test and remove this extra sorting
        // TODO: Should inflectionPoints become a LinkedList??
        // Resort the list
        Collections.sort(inflectionPoints);
    }

    /**
     * Retrieves inflection points given the current state of fullPath between the specified indices.
     * @param lowerIndex
     * @param upperIndex
     * @return sorted list of inflection points
     */
    private List<Integer> getInflectionPoints(int lowerIndex, int upperIndex){

        List<Integer> inflectionPoints = new ArrayList<>();

        // Always include start
        inflectionPoints.add(lowerIndex);

        // Initialize slope variables
        int prevRise = 0; // Track initial change in z
        int prevRun = 0; // Track initial change in x
        int currRise = 0;
        int currRun = 0;
        // Track previous direction to find corners
        boolean lastChangeX = false;

        // Create iterator (more efficient for LinkedList than .get())
        Iterator<Coordinate> it = fullPath.listIterator(lowerIndex);
        Coordinate prev = it.next();
        for(; lowerIndex < upperIndex; ++lowerIndex){
            Coordinate curr = it.next();

            if(curr.getX() - prev.getX() == 0){ // If changing in z direction
                // Found corner
                if(lastChangeX){
                    // Check for slope change (rise / run)
                    if(currRise != prevRise || currRun != prevRun){
                        // Found inflection point
                        inflectionPoints.add(lowerIndex - currRun - 1);

                        prevRise = currRise;

                        // Reset
                        currRise = 0;
                    }
                }

                lastChangeX = false;
                ++currRise;
            }

            if(curr.getZ() - prev.getZ() == 0){ // If changing in x direction
                // Found corner
                if(!lastChangeX){
                    // Check for slope change (rise / run)
                    if(currRise != prevRise || currRun != prevRun){
                        // Found inflection point
                        inflectionPoints.add(lowerIndex - currRise - 1);

                        prevRun = currRun;

                        // Reset
                        currRun = 0;
                    }
                }

                lastChangeX = true;
                ++currRun;
            }

            prev = curr;
        }

        // Always include end
        inflectionPoints.add(upperIndex - 1);

        return inflectionPoints;
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

    /**
     * Finds index of largest number less than value in sorted inflection point list. If none exists, returns -1
     * @param value
     * @return
     */
    private int getUpperIndex(int value){
        int left = 0;
        int right = inflectionPoints.size() - 1;
        int result = -1;
        while(left <= right){
            int mid = left + (right - left) / 2;
            if(inflectionPoints.get(mid) < value){
                result = mid;
                left = mid + 1;
            }
            else {
                right = mid - 1;
            }
        }
        if(result != -1) return inflectionPoints.get(result);
        return result;
    }
}
