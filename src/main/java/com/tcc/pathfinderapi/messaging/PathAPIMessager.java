package com.tcc.pathfinderapi.messaging;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;
import org.bukkit.Bukkit;

public class PathAPIMessager {

    private static PathFinderAPI plugin;
    private static boolean debugEnabled;

    public PathAPIMessager(PathFinderAPI plugin, ConfigManager configMang){
        this.plugin = plugin;
        debugEnabled = configMang.getBoolean(ConfigNode.DEBUG_MODE_ENABLED);
    }


    public static void info(String message){
        plugin.getLogger().info(message);
    }

    public static void warn(String message){
        plugin.getLogger().warning(message);
    }

    public static void severe(String message){
        plugin.getLogger().severe(message);
    }

    /**
     * Log debug messages to show flow of a program.
     * @param message
     */
    public static void debug(String message){
        if(debugEnabled) plugin.getLogger().info("DEBUG: " + message);
    }
}
