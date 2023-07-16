package com.tcc.pathfinderapi.messaging;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;

public class PathAPIMessager {

    private static PathFinderAPI pathFinderAPI;
    private static boolean debugEnabled;

    public PathAPIMessager (PathFinderAPI _pathFinderAPI, ConfigManager configManager) {

        pathFinderAPI = _pathFinderAPI;
        debugEnabled = configManager.getBoolean(ConfigNode.DEBUG_MODE_ENABLED);
    }

    public static void info (String message) { pathFinderAPI.getLogger().info(message); }
    public static void warn (String message) { pathFinderAPI.getLogger().warning(message); }
    public static void severe (String message) { pathFinderAPI.getLogger().severe(message); }
    public static void player (Player player, String message) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)); }

    /**
     * Log debug messages to show flow of a program.
     * @param message
     */
    public static void debug (String message) { if (debugEnabled) pathFinderAPI.getLogger().info("DEBUG: " + message); }
}
