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
        this.pathVisualizer.initalizePath(this.player, this.fullPath);
        
        return this;
    }

    public Player getPlayer () { return this.player; }
    public Location getStart () { return this.start; }
    public Location getEnd () { return this.end; }

    public PathVisualizer getPathVisualizer () { return this.pathVisualizer; }
    public LinkedList<Coordinate> getFullPath () { return this.fullPath; }
    public LinkedList<Coordinate> getRelativePath () { return this.relativePath; }
}
