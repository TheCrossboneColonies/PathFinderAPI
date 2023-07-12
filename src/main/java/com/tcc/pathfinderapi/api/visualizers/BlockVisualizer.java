package com.tcc.pathfinderapi.api.visualizers;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.objects.Coordinate;

public class BlockVisualizer implements PathVisualizer {
    
    private Map<Coordinate, BlockData> blockData;
    private ConfigManager configManager = ConfigManager.getInstance();;

    @Override
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath) {

        this.blockData = new HashMap<Coordinate, BlockData>();
        List<List<Coordinate>> partitions;

        if (this.configManager.getInt(ConfigNode.BLOCK_VISUALIZER_MAX_BLOCKS) == 0) { partitions = Collections.singletonList(fullPath); }
        else { partitions = Lists.partition(fullPath, this.configManager.getInt(ConfigNode.BLOCK_VISUALIZER_MAX_BLOCKS)); }

        for (List<Coordinate> partition : partitions) {

            new BukkitRunnable() {

                @Override
                public void run () {

                    for (Coordinate coordinate : partition) {

                        Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                        blockData.put(coordinate, block.getBlockData());

                        block.setType(Material.matchMaterial(configManager.getString(ConfigNode.BLOCK_VISUALIZER_BLOCK_TYPE)));
                    }
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("PathFinderAPI"), partitions.indexOf(partition) * 20);
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
