package com.tcc.pathfinderapi.pathing;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathNode {

    private int x;
    private int y;
    private int z;
    private List<PathNode> neighbors;

    // TODO: Some nodes have warps associated with them to different locations, worlds, or servers. How to store this?
    // There will be millions of PathNode objects and only a handful of nodes with the above data.

    public PathNode (Location location) {

        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.neighbors = new ArrayList<>();
    }

    public PathNode (int x, int y, int z) {

        this.x = x;
        this.y = y;
        this.z = z;
        this.neighbors = new ArrayList<>();
    }

    public int getX () { return this.x; }
    public int getY () { return this.y; }
    public int getZ () { return this.z; }

    /**
     * @return unmodifiable list of neighbors for this node.
     */
    public List<PathNode> getNeighbors () { return Collections.unmodifiableList(neighbors); }
    public void addNeighbor (PathNode neighbor) { this.neighbors.add(neighbor); }
    public boolean removeNeighbor (PathNode neighbor) { return this.neighbors.remove(neighbor); }
}
