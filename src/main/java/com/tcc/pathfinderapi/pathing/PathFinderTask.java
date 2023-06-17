package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PathFinderTask {

    private BukkitTask bukkitTask;
    private CompletableFuture<List<Coordinate>> pathFuture;

    PathFinderTask (PathFinder pathFinder) {

        this.pathFuture = new CompletableFuture<>();
        pathFinder.onStart();

        this.bukkitTask = new BukkitRunnable() {

            @Override
            public void run () {
            
                while (true) {

                    PathStepResponse pathStepResponse = pathFinder.step();

                    if (pathStepResponse.getResult() == PathStepResult.SUCCESS) {

                        this.cancel();
                        pathFinder.onComplete();
                        pathFinder.onSuccess();
                        pathFuture.complete((List<Coordinate>) pathStepResponse.getMetaData("path"));

                        return;
                    } else if (pathStepResponse.getResult() == PathStepResult.ERROR) {

                        this.cancel();
                        pathFinder.onComplete();
                        pathFinder.onError();
                        pathFuture.completeExceptionally(new PathException((String) pathStepResponse.getMetaData("error_message")));

                        return;
                    }
                }
            }
        }.runTaskAsynchronously(PathFinderAPI.getPlugin(PathFinderAPI.class));
    }

    /**
     * @return a future to the complete path.
     */
    public CompletableFuture<List<Coordinate>> getPath () { return this.pathFuture; }

    public boolean cancel () { return PathFinderScheduler.cancelTask(this); }
    public int getTaskID () { return this.bukkitTask.getTaskId(); }
    BukkitTask getTask () { return this.bukkitTask; }
}
