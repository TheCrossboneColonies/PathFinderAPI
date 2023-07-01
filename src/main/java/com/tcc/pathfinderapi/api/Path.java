package com.tcc.pathfinderapi.api;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.api.visualizers.PathVisualizer;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import com.tcc.pathfinderapi.pathing.pathoptimizers.WindowOptimizer;

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

        PathFinder pathFinder = Greedy.getBuilder(this.start, this.end).build();
        CompletableFuture<List<Coordinate>> pathFuture = pathFinder.run().getPath();

        pathFuture.whenComplete((integer, error) -> { if (pathFuture.isCompletedExceptionally()) { System.out.println("Could not find path."); } })
            .thenAccept(list -> {

                LinkedList<Coordinate> path = new LinkedList<>(list);

                WindowOptimizer windowOptimizer = new WindowOptimizer(path, this.player.getWorld(), pathFinder);
                windowOptimizer.optimize();

                this.fullPath = path;
            });
    }

    public Path withPathVisualizer (PathVisualizer pathVisualizer) {

        this.pathVisualizer = pathVisualizer;
        RelativePathUpdater relativePathUpdater = relativePath -> this.relativePath = relativePath;
        PathVisualizerDispatcher pathVisualizerDispatcher = new PathVisualizerDispatcher(this.player, this.pathVisualizer, this.fullPath, relativePathUpdater);

        new Thread(pathVisualizerDispatcher).start();
        return this;
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
        this.relativePath = (LinkedList<Coordinate>) this.fullPath.subList(0, 12);
        this.relativePathUpdater = relativePathUpdater;
    }

    @Override
    public void run () {

        this.pathVisualizer.initalizePath(this.player, this.fullPath);
        this.pathVisualizer.interpretNewPath(this.player, this.relativePath);

        while (true) {

            Coordinate closetCoordinate = this.relativePath.getFirst();

            for (Coordinate coordinate : this.relativePath) {

                Location location = new Location(this.player.getWorld(), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                Location closestLocation = new Location(this.player.getWorld(), closetCoordinate.getX(), closetCoordinate.getY(), closetCoordinate.getZ());
                if (this.player.getLocation().distance(location) < this.player.getLocation().distance(closestLocation)) { closetCoordinate = coordinate; }
            }

            Location cloestLocation = new Location(this.player.getWorld(), closetCoordinate.getX(), closetCoordinate.getY(), closetCoordinate.getZ());
            if (this.player.getLocation().distance(cloestLocation) > 20) { break; }
            if (this.fullPath.getLast() == closetCoordinate) { break; }

            int closestCoordinateIndex = this.relativePath.indexOf(closetCoordinate);
            if (closestCoordinateIndex >= 8) {

                this.pathVisualizer.interpretOldPath(this.player, this.relativePath);

                int initialIndex = this.fullPath.indexOf(this.relativePath.getFirst());
                this.relativePath = (LinkedList<Coordinate>) this.fullPath.subList(initialIndex + closestCoordinateIndex, closestCoordinateIndex + 12);

                this.pathVisualizer.interpretNewPath(this.player, this.relativePath);
                this.relativePathUpdater.updateRelativePath(this.relativePath);
            }

            try { Thread.sleep(100); } 
            catch (InterruptedException interruptedException) { interruptedException.printStackTrace(); }
        }

        this.pathVisualizer.interpretNewPath(this.player, this.relativePath);
        this.pathVisualizer.clearPath(this.player, this.fullPath);
    }
}
