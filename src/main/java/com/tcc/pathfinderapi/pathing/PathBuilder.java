package com.tcc.pathfinderapi.pathing;

import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import org.bukkit.Location;

public abstract class PathBuilder {

    private Location start;
    private Location end;
    private int maxPathLength = Integer.MAX_VALUE;

    public PathBuilder(Location start, Location end){
        this.start = start;
        this.end = end;
    }

    public Location getStart(){
        return start;
    }

    public Location getEnd(){
        return end;
    }

    public int getMaxPathLength() { return maxPathLength; }

    public PathBuilder setStart(Location start){
        this.start = start;
        return this;
    }

    public PathBuilder setEnd(Location end){
        this.end = end;
        return this;
    }

    /**
     *
     * @param maxPathLength - parameter that tells pathfinder when to give up searching. Default: INTEGER.MAX_VALUE (No path length limit)
     * @return
     */
    public PathBuilder setMaxPathLength(int maxPathLength){
        this.maxPathLength = maxPathLength;
        return this;
    }

    public abstract PathFinder build();

}
