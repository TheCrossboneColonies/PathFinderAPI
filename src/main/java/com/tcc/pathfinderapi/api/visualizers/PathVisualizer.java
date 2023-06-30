package com.tcc.pathfinderapi.api.visualizers;

import java.util.LinkedList;

import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.objects.Coordinate;

public interface PathVisualizer {
    
    /**
     * Called when the path is first generated.
     * Should be used to prepare the player/path for the journey, as well as initialize any data structures.
     * @param player The player who is being shown the path.
     * @param fullPath The full path that was generated.
     */
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath);

    /**
     * Called when the player passes an old relative area of the path.
     * Should be used to update the player/path from the old area, as well as update any data structures.
     * @param player The player who is being shown the path.
     * @param relativePath The old relative path that was generated.
     */
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath);

    /**
     * Called when the player reaches a new relative area of the path.
     * Should be used to update the player/path for the new area, as well as update any data structures.
     * @param player The player who is being shown the path.
     * @param relativePath The new relative path that was generated.
     */
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath);

    /**
     * Called when the player has completed the path.
     * Should be used to remove any lasting effects on the player/path, as well as clear any data structures.
     * @param player The player who is being shown the path.
     * @param fullPath The full path that was generated.
     */
    public void clearPath (Player player, LinkedList<Coordinate> fullPath);
}
