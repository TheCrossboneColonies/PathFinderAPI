package com.tcc.pathfinderapi.pathing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class BlockManager {

    private static Cache<ChunkKey, ChunkSnapshot> chunkHolder;

    public BlockManager(ConfigManager configMang){
        int milliseconds = configMang.getInt(ConfigNode.PERFORMANCE_CHUNK_INVALIDATION_TIME);
        chunkHolder = CacheBuilder.newBuilder().expireAfterWrite(milliseconds, TimeUnit.MILLISECONDS).build();
    }

    public static Material getBlockType(World world, Coordinate coord){
        return getBlockType(world, coord.getX(), coord.getY(), coord.getZ());
    }

    public static Material getBlockType(World world, int x, int y, int z){

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        ChunkSnapshot snapshot = getChunkSnapshot(world, chunkX, chunkZ);
        // Return block type using location relative to chunk origin
        return snapshot.getBlockType(x % 16, y, z % 16);
    }


    private static ChunkSnapshot getChunkSnapshot(World world, int chunkX, int chunkZ){
        ChunkKey key = new ChunkKey(world.getName(), chunkX, chunkZ);

        ChunkSnapshot snapshot = chunkHolder.getIfPresent(key);
        if(snapshot != null) return snapshot;

        // Fetch new copy
        snapshot = world.getChunkAt(chunkX, chunkZ).getChunkSnapshot();
        chunkHolder.put(key, snapshot);
        return snapshot;
    }



    private static class ChunkKey {

        String worldName;
        int chunkX;
        int chunkZ;

        ChunkKey(@Nonnull String worldName, int chunkX, int chunkZ){
            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkKey chunkKey = (ChunkKey) o;
            return chunkX == chunkKey.chunkX && chunkZ == chunkKey.chunkZ && worldName.equals(chunkKey.worldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldName, chunkX, chunkZ);
        }
    }

}
