package com.tcc.pathfinderapi.api.visualizers;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.objects.Coordinate;

public class BlockVisualizer implements PathVisualizer {
    
    private HashMap<Coordinate, BlockData> blockData;

    @Override
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath) {

        this.blockData = new HashMap<Coordinate, BlockData>();

        for (Coordinate coordinate : fullPath) {

            Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            this.blockData.put(coordinate, block.getBlockData());
            block.setType(Material.GOLD_BLOCK);
        }
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void clearPath (Player player, LinkedList<Coordinate> fullPath) {

        for (Coordinate coordinate : fullPath) {

            Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            block.setBlockData(this.blockData.get(coordinate));
        }
    }
}
