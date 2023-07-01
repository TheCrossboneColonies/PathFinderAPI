package com.tcc.pathfinderapi.api.visualizers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleVisualizer implements PathVisualizer {

    private boolean pathCompleted;
    private List<Coordinate> particleCoordinates;

    @Override
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = false;
        this.particleCoordinates = new ArrayList<Coordinate>();

        new BukkitRunnable() {

            @Override
            public void run () {

                while (!pathCompleted) {

                    for (Coordinate particleCoordinate : particleCoordinates) {

                        DustOptions dustOptions = new DustOptions(Color.YELLOW, 1.0F);
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
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) { this.particleCoordinates.add(relativePath.get(Math.min(7, relativePath.size() - 1))); }

    @Override
    public void clearPath (Player player, LinkedList<Coordinate> fullPath) { this.pathCompleted = true; }
}
