package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.pathing.PathNode;
import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartialRefinementAStar implements PathFinder {


    @Override
    public CompletableFuture<List<PathNode>> findPath(Location start, Location end) {
        // PRA* can technically return a very bad path quickly and then refine that path given more time.
        // Might need a way of structuring pathfinders that do and don't support path refinement.
        return null;
    }
}
