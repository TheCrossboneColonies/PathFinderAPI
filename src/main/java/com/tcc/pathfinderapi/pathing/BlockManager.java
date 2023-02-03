package com.tcc.pathfinderapi.pathing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BlockManager {

    private Cache<ChunkKey, ChunkSnapshot> chunkHolder;

    public BlockManager(ConfigManager configMang){
        int milliseconds = configMang.getInt(ConfigNode.PERFORMANCE_CHUNK_INVALIDATION_TIME);
        CacheBuilder.newBuilder().expireAfterWrite(milliseconds, TimeUnit.MILLISECONDS).build();
    }

    public Material getBlockType(World world, int x, int y, int z){
        // Get chunk coordinates as single 8-byte long
        int chunkX = (int) Math.floor(x / 16.0);
        int chunkZ = (int) Math.floor(z / 16.0);

        ChunkSnapshot snapshot = getChunkSnapshot(world, chunkX, chunkZ);
        // Return block type using location relative to chunk origin
        return snapshot.getBlockType(x % 16, y, z % 16);
    }


    private ChunkSnapshot getChunkSnapshot(World world, int chunkX, int chunkZ){
        ChunkKey key = new ChunkKey(world.getName(), chunkX, chunkZ);

        ChunkSnapshot snapshot = chunkHolder.getIfPresent(key);
        if(snapshot != null) return snapshot;

        // Fetch new copy
        snapshot = world.getChunkAt(chunkX, chunkZ).getChunkSnapshot();
        chunkHolder.put(key, snapshot);
        return snapshot;
    }


    private class ChunkKey {

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
