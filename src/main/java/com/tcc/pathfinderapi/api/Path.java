package com.tcc.pathfinderapi.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
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

/**
 * This class is used to create a callback updater method for the 
 * PathVisualizerDispatcher back to the Path.
 */
interface RelativePathUpdater { public void updateRelativePath (LinkedList<Coordinate> relativePath); }

/**
 * This class is used to dispatch a PathVisualizer to a given path
 * on a separate thread as to not freeze server activities.
 */
class PathVisualizerDispatcher implements Runnable {

    private Player player;
    private PathVisualizer pathVisualizer;
    private ConfigManager configManager;
    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;
    private RelativePathUpdater relativePathUpdater;

    public PathVisualizerDispatcher (Player player, PathVisualizer pathVisualizer, LinkedList<Coordinate> fullPath, RelativePathUpdater relativePathUpdater) {

        this.player = player;
        this.pathVisualizer = pathVisualizer;

        this.configManager = ConfigManager.getInstance();

        this.fullPath = fullPath;
        this.relativePath = new LinkedList<Coordinate>(this.fullPath.subList(0, this.configManager.getInt(ConfigNode.PERFORMANCE_RELATIVE_RADIUS)));
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

/**
 * This class is used to handle the updating and handling of
 * a relative path, which needs to be constantly monitored
 * on a separate thread as to not freeze server activities.
 */
class RelativePathAwaiter implements Runnable {

    private Player player;
    private PathVisualizer pathVisualizer;
    private ConfigManager configManager;
    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;
    private RelativePathUpdater relativePathUpdater;

    public RelativePathAwaiter (Player player, PathVisualizer pathVisualizer, LinkedList<Coordinate> fullPath, LinkedList<Coordinate> relativePath, RelativePathUpdater relativePathUpdater) {

        this.player = player;
        this.pathVisualizer  = pathVisualizer;

        this.configManager = ConfigManager.getInstance();
        this.fullPath = fullPath;
        this.relativePath = relativePath;
        this.relativePathUpdater = relativePathUpdater;
    }

    @Override
    public void run () {

        while (true) {

            Coordinate closestCoordinate = this.relativePath.getFirst();

            for (Coordinate coordinate : this.relativePath) {

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                Location closestLocation = new Location(this.player.getWorld(), closestCoordinate.getX(), closestCoordinate.getY(), closestCoordinate.getZ());
                if (this.player.getLocation().distance(location) < this.player.getLocation().distance(closestLocation)) { closestCoordinate = coordinate; }
            }

            Location closestLocation = new Location(this.player.getWorld(), closestCoordinate.getX(), closestCoordinate.getY(), closestCoordinate.getZ());
            if (this.player.getLocation().distance(closestLocation) > 20) { break; }
            if (this.fullPath.getLast() == closestCoordinate) { break; }

            int closestCoordinateIndex = this.relativePath.indexOf(closestCoordinate);
            LinkedList<Coordinate> newRelativePath = new LinkedList<>();

            for (Coordinate coordinate : this.fullPath) {

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                if (this.player.getLocation().distance(location) <= configManager.getInt(ConfigNode.PERFORMANCE_RELATIVE_RADIUS) && this.fullPath.indexOf(coordinate) >= this.fullPath.indexOf(closestCoordinate)) { newRelativePath.add(coordinate); } // TODO Change 12
            }

            if (!Arrays.equals(this.relativePath.toArray(), newRelativePath.toArray())) {

                int taskID = new BukkitRunnable() {

                    @Override
                    public void run () {

                        pathVisualizer.interpretOldPath(player, relativePath);
                        pathVisualizer.interpretNewPath(player, newRelativePath);
                        relativePathUpdater.updateRelativePath(newRelativePath);
                        relativePath = newRelativePath;
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
