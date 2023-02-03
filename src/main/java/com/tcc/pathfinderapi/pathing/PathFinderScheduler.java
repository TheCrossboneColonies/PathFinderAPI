package com.tcc.pathfinderapi.pathing;

import java.util.HashMap;
import java.util.Map;

public class PathFinderScheduler {

    private static Map<Integer, PathFinderTask> pathFinderTaskHolder = new HashMap<>();

    public static boolean cancelTask(PathFinderTask task){
        return cancelTask(task.getTaskId());
    }

    public static boolean cancelTask(int taskId){
        PathFinderTask task = pathFinderTaskHolder.get(taskId);
        if(task == null) return false;

        task.getTask().cancel();
        return true;
    }

    public static PathFinderTask run(PathFinder pathFinder){
        PathFinderTask task = new PathFinderTask(pathFinder);
        pathFinderTaskHolder.put(task.getTaskId(), task);
        return task;
    }

}
