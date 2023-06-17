package com.tcc.pathfinderapi.pathing;

import org.bukkit.Location;

public abstract class PathFinder {

    private Location start;
    private Location end;
    private int maxPathLength;

    public PathFinder (PathBuilder pathBuilder) {

        this.start = pathBuilder.getStart();
        this.end = pathBuilder.getEnd();
        this.maxPathLength = pathBuilder.getMaxPathLength();
    }

    public Location getStart () { return this.start; }
    public Location getEnd () { return this.end; }
    public int getMaxPathLength () { return this.maxPathLength; }

    public final PathFinderTask run () { return PathFinderScheduler.run(this); }

    protected void onStart () {}
    protected void onComplete () {}
    protected void onSuccess () {}
    protected void onError () {}

    public abstract PathBuilder toBuilder();
    protected abstract PathStepResponse step();
}
