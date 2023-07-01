package com.tcc.pathfinderapi.api.visualizers;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.objects.Coordinate;

public class ParticleVisualizer implements PathVisualizer {

    @Override
    public void initalizePath (Player player, LinkedList<Coordinate> fullPath) {}

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) { player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1); }

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {

        Coordinate particleCoordinate = relativePath.get(7);
        DustOptions dustOptions = new DustOptions(Color.YELLOW, 1.0F);
        player.spawnParticle(Particle.REDSTONE, particleCoordinate.getX(), particleCoordinate.getY(), particleCoordinate.getZ(), 5, dustOptions);
    }

    @Override
    public void clearPath (Player player, LinkedList<Coordinate> fullPath) {}
}
