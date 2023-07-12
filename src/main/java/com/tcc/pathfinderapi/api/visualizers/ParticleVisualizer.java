package com.tcc.pathfinderapi.api.visualizers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleVisualizer implements PathVisualizer {

    private boolean pathCompleted;
    private List<Coordinate> particleCoordinates;
    private ConfigManager configManager = ConfigManager.getInstance();;

    @Override
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = false;
        this.particleCoordinates = new ArrayList<Coordinate>();

        new BukkitRunnable() {

            @Override
            public void run () {

                while (!pathCompleted) {

                    for (Coordinate particleCoordinate : particleCoordinates) {

                        DustOptions dustOptions = new DustOptions(configManager.getColor(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_COLOR), 1.0F);
                        player.spawnParticle(Particle.REDSTONE, particleCoordinate.getX(), particleCoordinate.getY() + 2.5, particleCoordinate.getZ(), 50, dustOptions);
                    }

                    try { Thread.sleep(100); }
                    catch (InterruptedException interruptedException) { interruptedException.printStackTrace(); }
                }
            }
        }.runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("PathFinderAPI"));
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) {

        this.particleCoordinates.clear();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) { 
        
        for (int i = 1; i < relativePath.size(); i++) {

            if (i % this.configManager.getInt(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_EVERY) == 0) {

                this.particleCoordinates.add(relativePath.get(i));
            }
        }
    }

    @Override
    public void clearPath (Player player, LinkedList<Coordinate> fullPath) { this.pathCompleted = true; }
}
