package com.tcc.pathfinderapi.api.visualizers;

import java.util.*;

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
    private List<Coordinate> oldParticleCoordinates;
    private Map<Coordinate, Double> oldParticleHeightAdditions;
    private ConfigManager configManager = ConfigManager.getInstance();

    @Override
    public void initializePath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = false;
        this.particleCoordinates = new ArrayList<Coordinate>();
        this.oldParticleCoordinates = new ArrayList<Coordinate>();
        this.oldParticleHeightAdditions = new HashMap<Coordinate, Double>();

        new BukkitRunnable() {

            @Override
            public void run () {

                while (!pathCompleted) {

                    for (Coordinate particleCoordinate : particleCoordinates) {

                        double heightAddition = oldParticleHeightAdditions.getOrDefault(particleCoordinate, 1.25);
                        if (oldParticleCoordinates.contains(particleCoordinate)) { heightAddition += 0.025; }
                        if (heightAddition > 2.0) { heightAddition = 1.25; }
                        oldParticleHeightAdditions.put(particleCoordinate, heightAddition);

                        DustOptions dustOptions = new DustOptions(configManager.getColor(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_COLOR), 1.0F);
                        player.spawnParticle(Particle.REDSTONE, particleCoordinate.getX(), particleCoordinate.getY() + heightAddition, particleCoordinate.getZ(), 50, dustOptions);
                    }

                    oldParticleCoordinates = particleCoordinates;
                    try { Thread.sleep(100); }
                    catch (InterruptedException interruptedException) { interruptedException.printStackTrace(); }
                }
            }
        }.runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("PathFinderAPI"));
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) { this.particleCoordinates.clear(); }

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {

        int leadIndex = Math.min(this.configManager.getInt(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_LEAD), relativePath.size() - 1);
        this.particleCoordinates.add(relativePath.get(leadIndex));
    }

    @Override
    public void endPath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = true;
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
