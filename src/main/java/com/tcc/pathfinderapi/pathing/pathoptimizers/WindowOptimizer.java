package com.tcc.pathfinderapi.pathing.pathoptimizers;

import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WindowOptimizer implements PathOptimizer {

    private final int OPTIMIZATION_SEGMENT_LENGTH = 40;
    private LinkedList<Coordinate> inflectionPoints = new LinkedList<>();

    private PathFinder pathFinder;

    public WindowOptimizer(PathFinder pathFinder){
        this.pathFinder = pathFinder;
    }

    @Override
    public PathStepResponse stepOptimize() {

        // Find inflection points

        // Sliding window to find mini paths

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
}
