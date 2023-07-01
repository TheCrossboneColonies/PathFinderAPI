package com.tcc.pathfinderapi.api;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.tcc.pathfinderapi.PathFinderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.api.visualizers.PathVisualizer;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import com.tcc.pathfinderapi.pathing.pathoptimizers.WindowOptimizer;
import org.bukkit.scheduler.BukkitRunnable;

public class Path {

    private PathVisualizer pathVisualizer;
    private Player player;
    private Location start;
    private Location end;

    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;

    public Path (PathVisualizer pathVisualizer, Player player, Location start, Location end) {

        this.pathVisualizer = pathVisualizer;
        this.player = player;
        this.start = start;
        this.end = end;
    }

    public void generatePath () {

        PathFinder pathFinder = Greedy.getBuilder(this.start, this.end).build();
        CompletableFuture<List<Coordinate>> pathFuture = pathFinder.run().getPath();

        pathFuture.whenComplete((integer, error) -> { if (pathFuture.isCompletedExceptionally()) { System.out.println("Could not find path."); } })
                .thenAccept(list -> {

                    LinkedList<Coordinate> path = new LinkedList<>(list);

                    WindowOptimizer windowOptimizer = new WindowOptimizer(path, this.player.getWorld(), pathFinder);
                    windowOptimizer.optimize();

                    this.fullPath = path;
                    this.activatePathVisualizer();
                });
    }

    private void activatePathVisualizer () {

        RelativePathUpdater relativePathUpdater = relativePath -> this.relativePath = relativePath;
        PathVisualizerDispatcher pathVisualizerDispatcher = new PathVisualizerDispatcher(this.player, this.pathVisualizer, this.fullPath, relativePathUpdater);

        int taskID = Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PathFinderAPI"), pathVisualizerDispatcher).getTaskId();
        PathFinderAPI.scheduledTaskIDs.add(taskID);
    }

    public Player getPlayer () { return this.player; }
    public Location getStart () { return this.start; }
    public Location getEnd () { return this.end; }

    public PathVisualizer getPathVisualizer () { return this.pathVisualizer; }
    public LinkedList<Coordinate> getFullPath () { return this.fullPath; }
    public LinkedList<Coordinate> getRelativePath () { return this.relativePath; }
}

interface RelativePathUpdater { public void updateRelativePath (LinkedList<Coordinate> relativePath); }
class PathVisualizerDispatcher implements Runnable {

    private Player player;
    private PathVisualizer pathVisualizer;
    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;
    private RelativePathUpdater relativePathUpdater;

    public PathVisualizerDispatcher (Player player, PathVisualizer pathVisualizer, LinkedList<Coordinate> fullPath, RelativePathUpdater relativePathUpdater) {

        this.player = player;
        this.pathVisualizer = pathVisualizer;

        this.fullPath = fullPath;
        this.relativePath = new LinkedList<Coordinate>(this.fullPath.subList(0, 12));
        this.relativePathUpdater = relativePathUpdater;
    }

    @Override
    public void run () {

        this.pathVisualizer.initalizePath(this.player, this.fullPath);
        this.pathVisualizer.interpretNewPath(this.player, this.relativePath);

        RelativePathUpdater relativePathUpdater = relativePath -> {

            this.relativePath = relativePath;
            this.relativePathUpdater.updateRelativePath(relativePath);
        };

        RelativePathAwaiter relativePathAwaiter = new RelativePathAwaiter(this.player, this.pathVisualizer, this.fullPath, this.relativePath, relativePathUpdater);
        int taskID = Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("PathFinderAPI"), relativePathAwaiter).getTaskId();
        PathFinderAPI.scheduledTaskIDs.add(taskID);
    }
}

class RelativePathAwaiter implements Runnable {

    private Player player;
    private PathVisualizer pathVisualizer;
    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;
    private RelativePathUpdater relativePathUpdater;

    public RelativePathAwaiter (Player player, PathVisualizer pathVisualizer, LinkedList<Coordinate> fullPath, LinkedList<Coordinate> relativePath, RelativePathUpdater relativePathUpdater) {

        this.player = player;
        this.pathVisualizer  = pathVisualizer;
        this.fullPath = fullPath;
        this.relativePath = relativePath;
        this.relativePathUpdater = relativePathUpdater;
    }

    @Override
    public void run () {

        while (true) {

            Coordinate closetCoordinate = this.relativePath.getFirst();

            for (Coordinate coordinate : this.relativePath) {

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                Location closestLocation = new Location(this.player.getWorld(), closetCoordinate.getX(), closetCoordinate.getY(), closetCoordinate.getZ());
                if (this.player.getLocation().distance(location) < this.player.getLocation().distance(closestLocation)) { closetCoordinate = coordinate; }
            }

            Location closestLocation = new Location(this.player.getWorld(), closetCoordinate.getX(), closetCoordinate.getY(), closetCoordinate.getZ());
            if (this.player.getLocation().distance(closestLocation) > 20) { break; }
            if (this.fullPath.getLast() == closetCoordinate) { break; }

            int closestCoordinateIndex = this.relativePath.indexOf(closetCoordinate);
            if (closestCoordinateIndex >= Math.min(7, this.relativePath.size() - 1)) {

                int taskID = new BukkitRunnable() {

                    @Override
                    public void run () {

                        pathVisualizer.interpretOldPath(player, relativePath);

                        int initialIndex = fullPath.indexOf(relativePath.getFirst());
                        relativePath = new LinkedList<Coordinate>(fullPath.subList(initialIndex + closestCoordinateIndex, Math.min(initialIndex + closestCoordinateIndex + 12, fullPath.size())));

                        pathVisualizer.interpretNewPath(player, relativePath);
                        relativePathUpdater.updateRelativePath(relativePath);
                    }
                }.runTask(Bukkit.getPluginManager().getPlugin("PathFinderAPI")).getTaskId();

                PathFinderAPI.scheduledTaskIDs.add(taskID);
            }

            try { Thread.sleep(100); }
            catch (InterruptedException interruptedException) { interruptedException.printStackTrace(); }
        }

        int taskID = new BukkitRunnable() {

            @Override
            public void run () {

                pathVisualizer.interpretOldPath(player, relativePath);
                pathVisualizer.clearPath(player, fullPath);
            }
        }.runTask(Bukkit.getPluginManager().getPlugin("PathFinderAPI")).getTaskId();

        PathFinderAPI.scheduledTaskIDs.add(taskID);
    }
}
