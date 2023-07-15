package com.tcc.pathfinderapi.pathing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import com.tcc.pathfinderapi.objects.Coordinate;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BlockManager {

    private static Cache<ChunkKey, ChunkSnapshot> chunkHolder;

    public BlockManager (ConfigManager configManager) {

        int milliseconds = configManager.getInt(ConfigNode.PERFORMANCE_CHUNK_INVALIDATION_TIME);
        chunkHolder = CacheBuilder.newBuilder().expireAfterWrite(milliseconds, TimeUnit.MILLISECONDS).build();
    }

    public static Material getBlockType (World world, Coordinate coordinate) { return getBlockType(world, coordinate.getX(), coordinate.getY(), coordinate.getZ()); }

    public static Material getBlockType (World world, int x, int y, int z) {
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        int chunkCoordinateX = (x & 0xF) + ((x >> 31) & 0x10);
        int chunkCoordinateZ = (z & 0xF) + ((z >> 31) & 0x10);

        ChunkSnapshot chunkSnapshot = getChunkSnapshot(world, chunkX, chunkZ);
        return chunkSnapshot.getBlockType(chunkCoordinateX, y, chunkCoordinateZ);
    }

    private static ChunkSnapshot getChunkSnapshot (World world, int chunkX, int chunkZ) {

        ChunkKey chunkKey = new ChunkKey(world.getName(), chunkX, chunkZ);

        ChunkSnapshot chunkSnapshot = chunkHolder.getIfPresent(chunkKey);
        if (chunkSnapshot != null) return chunkSnapshot;

        chunkSnapshot = world.getChunkAt(chunkX, chunkZ).getChunkSnapshot();
        chunkHolder.put(chunkKey, chunkSnapshot);
        return chunkSnapshot;
    }

    private static class ChunkKey {

        String worldName;
        int chunkX;
        int chunkZ;

        ChunkKey (@Nonnull String worldName, int chunkX, int chunkZ) {

            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals (Object object) {

            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            ChunkKey chunkKey = (ChunkKey) object;
            return chunkX == chunkKey.chunkX && chunkZ == chunkKey.chunkZ && worldName.equals(chunkKey.worldName);
        }

        @Override
        public int hashCode () { return Objects.hash(worldName, chunkX, chunkZ); }
    }
}
