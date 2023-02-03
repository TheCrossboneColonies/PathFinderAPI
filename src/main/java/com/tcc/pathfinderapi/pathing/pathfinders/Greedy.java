package com.tcc.pathfinderapi.pathing.pathfinders;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.errorHandling.PathException;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.PathStepResponse;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Greedy extends PathFinder {

//    // Algorithm variables
//    private World world;
//    private RoadCoordinate currentBackTraceNode;
//    private PriorityQueue<RoadCoordinate> closedList;
//    private PriorityQueue<RoadCoordinate> openList;
//    private PathBuildStage currentStage = PathBuildStage.SEARCH;
//
//    /**
//     * Stores all roadcoordinates so neighbors can be easily found
//     */
//    private Map<Long, RoadCoordinate> masterList;
//
//    // Configuration variables
//    private final double LIQUID_PENALTY = 20;
//    private final int LIQUID_RADIUS = 2;
//    private final double CLIFF_PENALTY = 20;
//    private final int CLIFF_RADIUS = 2;

    public Greedy(Location start, Location end){
        super(start, end);

        // Check start and end in same world
        // Note: This is temporary until we get multi-world support
        if(!checkSameWorld(start, end)) throw new RuntimeException("Start and end locations not in same world!");

//        this.world = start.getWorld();
//        //Start and end swapped so path is generated from start to end (due to backtracing)
//        this.start = new RoadCoordinate(end.getBlockX(), end.getBlockY(), end.getBlockZ(), false);
//        this.end = new RoadCoordinate(start.getBlockX(), start.getBlockY(), start.getBlockZ(), false);
//
//        //Pathing variables
//        closedList = new PriorityQueue<RoadCoordinate>();
//        openList = new PriorityQueue<RoadCoordinate>();
//        masterList = new HashMap<Long, RoadCoordinate>();
//        masterList.put(RoadManager.coordinateToLong(this.start.coordLoc), this.start);
//        masterList.put(RoadManager.coordinateToLong(this.end.coordLoc), this.end);
//        this.start.h = heuristicDistance(this.end);
//        openList.add(this.start);

    }


    @Override
    protected PathStepResponse step() {
        return null;
    }

    private boolean checkSameWorld(Location loc1, Location loc2){
        return loc1.getWorld() == loc2.getWorld();
    }
}
