package com.tcc.pathfinderapi.api.visualizers;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.objects.Coordinate;

public class BlockVisualizer implements PathVisualizer {
    
    private Map<Coordinate, BlockData> blockData;
    private ConfigManager configManager = ConfigManager.getInstance();;

    @Override
    public void initializePath (Player player, LinkedList<Coordinate> fullPath) {

        this.blockData = new HashMap<Coordinate, BlockData>();

        for (Coordinate coordinate : fullPath) {

            new BukkitRunnable() {

                @Override
                public void run () {

                    Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    blockData.put(coordinate, block.getBlockData());

                    block.setType(Material.matchMaterial(configManager.getString(ConfigNode.BLOCK_VISUALIZER_BLOCK_TYPE)));
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("PathFinderAPI"), this.configManager.getInt(ConfigNode.BLOCK_VISUALIZER_BLOCK_DELAY) * fullPath.indexOf(coordinate));
        }
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void endPath (Player player, LinkedList<Coordinate> fullPath) {

        for (Coordinate coordinate : fullPath) {

            Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            block.setBlockData(this.blockData.get(coordinate));
        }
    }
}
