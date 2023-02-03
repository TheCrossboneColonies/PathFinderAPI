package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PathFinderTask {

    private BukkitTask task;
    private List<PathNode> path;
    private CompletableFuture<List<PathNode>> pathFuture;

    PathFinderTask(PathFinder pathFinder){

        pathFuture = new CompletableFuture<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                while(true) {
                    PathStepResponse response = pathFinder.step();

                    if (response == PathStepResponse.SUCCESSFUL) {
                        cancel();
                        pathFuture.complete(path);
                        return;
                    } else if (response == PathStepResponse.NO_PATH_EXISTS) {
                        cancel();
                        pathFuture.completeExceptionally(new PathException("No path found."));
                        return;
                    }
                }
            }
        }.runTaskAsynchronously(PathFinderAPI.getPlugin(PathFinderAPI.class));
    }

    /**
     *
     * @return a future to the complete path
     */
    public CompletableFuture<List<PathNode>> getPath(){
        return pathFuture;
    }

    public boolean cancel(){
        return PathFinderScheduler.cancelTask(this);
    }

    public int getTaskId(){
        return task.getTaskId();
    }

    BukkitTask getTask(){
        return task;
    }


}
