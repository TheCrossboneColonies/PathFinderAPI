package com.tcc.pathfinderapi.messaging;

import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.configuration.ConfigNode;

public class PathAPIMessager {

    private static PathFinderAPI pathFinderAPI;
    private static boolean debugEnabled;

    public PathAPIMessager (PathFinderAPI pathFinderAPI_, ConfigManager configManager) {

        pathFinderAPI = pathFinderAPI_;
        debugEnabled = configManager.getBoolean(ConfigNode.DEBUG_MODE_ENABLED);
    }


    public static void info (String message) { pathFinderAPI.getLogger().info(message); }
    public static void warn (String message) { pathFinderAPI.getLogger().warning(message); }
    public static void severe (String message) { pathFinderAPI.getLogger().severe(message); }

    /**
     * Log debug messages to show flow of a program.
     * @param message
     */
    public static void debug (String message) { if (debugEnabled) pathFinderAPI.getLogger().info("DEBUG: " + message); }
}
