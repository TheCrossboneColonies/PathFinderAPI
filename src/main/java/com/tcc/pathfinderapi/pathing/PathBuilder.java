package com.tcc.pathfinderapi.pathing;

import org.bukkit.Location;

public abstract class PathBuilder {

    private Location start;
    private Location end;
    private int maxPathLength = Integer.MAX_VALUE;

    public PathBuilder (Location start, Location end) {

        this.start = start;
        this.end = end;
    }

    public Location getStart () { return this.start; }
    public Location getEnd () { return this.end; }
    public int getMaxPathLength () { return this.maxPathLength; }

    public PathBuilder setStart (Location start) {

        this.start = start;
        return this;
    }

    public PathBuilder setEnd (Location end) {

        this.end = end;
        return this;
    }

    /**
     * @param maxPathLength - parameter that tells pathfinder when to give up searching. Default: INTEGER.MAX_VALUE
     * @return
     */
    public PathBuilder setMaxPathLength (int maxPathLength) {

        this.maxPathLength = maxPathLength;
        return this;
    }

    public abstract PathFinder build();
}
