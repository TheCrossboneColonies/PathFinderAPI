package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PathFinderTask {

    private BukkitTask task;

    private CompletableFuture<List<Coordinate>> pathFuture;

    PathFinderTask(PathFinder pathFinder){

        pathFuture = new CompletableFuture<>();

        pathFinder.onStart();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                while(true) {
                    PathStepResponse response = pathFinder.step();

                    if (response.getResult() == PathStepResult.SUCCESS) {
                        cancel();
                        pathFinder.onComplete();
                        pathFinder.onSuccess();
                        pathFuture.complete((List<Coordinate>) response.getMetaData("path"));
                        return;
                    } else if (response.getResult() == PathStepResult.ERROR) {
                        cancel();
                        pathFinder.onComplete();
                        pathFinder.onError();
                        pathFuture.completeExceptionally(new PathException((String) response.getMetaData("error_message")));
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
    public CompletableFuture<List<Coordinate>> getPath(){
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
