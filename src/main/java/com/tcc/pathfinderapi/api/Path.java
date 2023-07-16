package com.tcc.pathfinderapi.api;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;

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

    private Player player;
    private Location start;
    private Location end;

    private PathVisualizer pathVisualizer;
    private LinkedList<Coordinate> fullPath;
    private LinkedList<Coordinate> relativePath;

    public Path (Player player, Location start, Location end) {

        this.player = player;
        this.start = start;
        this.end = end;

        this.pathVisualizer = null;
    }

    public Path (Player player, Location start, Location end, PathVisualizer pathVisualizer) {

        this.player = player;
        this.start = start;
        this.end = end;

        this.pathVisualizer = pathVisualizer;
    }

    public void generatePath () {

        PathFinder pathFinder = Greedy.getBuilder(this.start, this.end).build();
        CompletableFuture<List<Coordinate>> pathFuture = pathFinder.run().getPath();

        pathFuture.whenComplete((integer, error) -> { if (pathFuture.isCompletedExceptionally()) { PathAPIMessager.debug("Could not find path."); } })
                .thenAccept(unoptimizedPath -> {

                    LinkedList<Coordinate> path = new LinkedList<>(unoptimizedPath);

                    WindowOptimizer windowOptimizer = new WindowOptimizer(path, this.player.getWorld(), pathFinder);
                    windowOptimizer.optimize();

                    this.fullPath = path;
                    if (this.pathVisualizer != null) { this.activatePathVisualizer(); }
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

    /**
     * @return The {@link PathVisualizer} responsible for interpreting this path.
     */
    public PathVisualizer getPathVisualizer () { return this.pathVisualizer; }

    /**
     * @return The full path that was generated from the start to the end.
     */
    public LinkedList<Coordinate> getFullPath () { return this.fullPath; }

    /**
     * @return The relative path that is currently being shown to the player, 
     * which is a smaller section of the full path that is more relevant to the player.
     */
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

        this.pathVisualizer.initializePath(this.player, this.fullPath);
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

        // Maintains the relative path until the player either abandons the path or completes it.
        while (true) {

            double closestRelativeDistance = 401;
            int closestRelativeCoordinateIndex = 0;

            ListIterator<Coordinate> relativePathIterator = this.relativePath.listIterator();
            while (relativePathIterator.hasNext()) {

                int coordinateIndex = relativePathIterator.nextIndex();
                Coordinate coordinate = relativePathIterator.next();

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());

                if (this.player.getLocation().distanceSquared(location) < closestRelativeDistance) {

                    closestRelativeDistance = this.player.getLocation().distanceSquared(location);
                    closestRelativeCoordinateIndex = coordinateIndex;
                }
            }

            if (closestRelativeDistance > 400) { break; }
            if (closestRelativeCoordinateIndex == this.relativePath.size() - 1) { break; }
            int relativeRadius = (int) Math.pow(configManager.getInt(ConfigNode.PERFORMANCE_RELATIVE_RADIUS), 2);

            ListIterator<Coordinate> fullPathIterator = this.fullPath.listIterator();
            LinkedList<Coordinate> newRelativePath = new LinkedList<>();
            while (fullPathIterator.hasNext()) {

                int coordinateIndex = fullPathIterator.nextIndex();
                Coordinate coordinate = fullPathIterator.next();

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());

                if (this.player.getLocation().distanceSquared(location) <= relativeRadius) {

                    if (coordinateIndex >= closestRelativeCoordinateIndex) { newRelativePath.add(coordinate); }
                }
            }

            if (!Arrays.equals(this.relativePath.toArray(), newRelativePath.toArray())) {

                // Schedules the handling of the new relative path on the main thread.
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

        // Schedules the final handling of the path on the main thread.
        int taskID = new BukkitRunnable() {

            @Override
            public void run () {

                pathVisualizer.interpretOldPath(player, relativePath);
                pathVisualizer.endPath(player, fullPath);
            }
        }.runTask(Bukkit.getPluginManager().getPlugin("PathFinderAPI")).getTaskId();

        PathFinderAPI.scheduledTaskIDs.add(taskID);
    }
}
