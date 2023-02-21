package com.tcc.pathfinderapi.pathing.pathoptimizers;

import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import com.tcc.pathfinderapi.pathing.PathStepResult;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    private List<Integer> inflectionPoints = new LinkedList<>();

    /**
     * Original path that should be optimized
     */
    private List<Coordinate> fullPath;
    private int currentBlockOffset;

    private PathFinder pathFinder;

    public WindowOptimizer(List<Coordinate> fullPath, PathFinder pathFinder){
        this.fullPath = fullPath;
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
        // FINAL THOUGHTS - Use ArrayList, check effectiveness, and then decide whether more time should be spent making it efficient
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

                // TODO: Attempt to find shorter path between locations

                // TODO: If shorter path found, update fullPath and update inflectionPoints within range

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

        int initialRise = 0; // Track initial change in z
        int initialRun = 0; // Track initial change in x
        int riseCounter = 0; // Track current change in z
        int runCounter = 0; // Track current change in x


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
