package com.tcc.pathfinderapi.pathing;

import java.util.HashMap;
import java.util.Map;

public class PathFinderScheduler {

    private static Map<Integer, PathFinderTask> pathFinderTaskHolder = new HashMap<>();

    public static boolean cancelTask (PathFinderTask pathFinderTask) { return cancelTask(pathFinderTask.getTaskID()); }

    public static boolean cancelTask (int taskID) {
        
        PathFinderTask pathFinderTask = pathFinderTaskHolder.get(taskID);
        if (pathFinderTask == null) return false;

        pathFinderTask.getTask().cancel();
        return true;
    }

    public static PathFinderTask run (PathFinder pathFinder) {

        PathFinderTask pathFinderTask = new PathFinderTask(pathFinder);
        pathFinderTaskHolder.put(pathFinderTask.getTaskID(), pathFinderTask);
        return pathFinderTask;
    }
}
