package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.pathing.PathNode;
import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PathFinder {

    /**
     *
     * @param start
     * @param end
     * @return a future to the complete path
     */
    public CompletableFuture<List<PathNode>> findPath(Location start, Location end);

}
