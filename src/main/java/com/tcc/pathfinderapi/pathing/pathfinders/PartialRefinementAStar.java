package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartialRefinementAStar implements PathFinder {

    private PathFinderAPI plugin;

    List<PathNode> path;

    public PartialRefinementAStar(PathFinderAPI plugin){
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<List<PathNode>> findPath(Location start, Location end) {
        // PRA* can technically return a very bad path quickly and then refine that path given more time.
        // Might need a way of structuring pathfinders that do and don't support path refinement.
        // Can we support this by just completing with a list, but keeping a reference to that list and modifying it? - how to then cancel refinement?

        CompletableFuture<List<PathNode>> pathFuture = new CompletableFuture<>();

        // TODO: How to allow canceling of pathfinding task before it is completed
        BukkitTask builderTask = new BukkitRunnable() {

            // Used to limit time path takes up per game tick
            final int SEARCH_STEPS_PER_SECOND = 50;

            @Override
            public void run() {

                int stepCounter = 0;

                for(; stepCounter < SEARCH_STEPS_PER_SECOND; ++stepCounter) {

                    PathStepResponse response = step();

                    if(response == PathStepResponse.SUCCESSFUL){
                        cancel();
                        pathFuture.complete(path);
                        return;
                    }
                    else if(response == PathStepResponse.NO_PATH_EXISTS){
                        cancel();
                        pathFuture.completeExceptionally(new PathException("No path found."));
                        return;
                    }


                }
            }

        }.runTaskTimer(plugin, 1L, 1L); // Delay starting task by 1 tick so user can configure the returned completable future

        return pathFuture;
    }


    private PathStepResponse step() {
        return null;
    }

}
