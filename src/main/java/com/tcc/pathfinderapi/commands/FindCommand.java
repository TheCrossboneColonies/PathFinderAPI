package com.tcc.pathfinderapi.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import com.tcc.pathfinderapi.pathing.pathoptimizers.WindowOptimizer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FindCommand {

    private PathFinderAPI plugin;

    public FindCommand(PathFinderAPI plugin){
        this.plugin = plugin;
    }

    public void registerCommand(CommandManager<CommandSender> manager){

        manager.command(
                manager.commandBuilder("pathapi", ArgumentDescription.of("Find a path between 2 locations"))
                        .permission("pathapi.find")

                        .literal("find")
                        .argument(LocationArgument.of("start"))
                        .argument(LocationArgument.of("end"))
                        .senderType(Player.class)
                        .handler(context -> {
                            Player player = (Player) context.getSender();
                            Location start = context.get("start");
                            Location end = context.get("end");

                            long startTime = System.currentTimeMillis();
                            PathFinder pathFinder = Greedy.getBuilder(start, end).build();
                            CompletableFuture<List<Coordinate>> pathFuture = pathFinder.run().getPath();

                            // Run this code whether or not a path is found successfully
                            pathFuture.whenComplete((myInt, err) -> {
                                // Exception message
                                if(pathFuture.isCompletedExceptionally()){
                                    System.out.println("Could not find path");
                                }
                            })
                            // Run this code if a path is found successfully
                            .thenAccept(list -> {
                                PathAPIMessager.info("Path of length " + list.size() + " found in " + (System.currentTimeMillis() - startTime) + " ms");
                                PathAPIMessager.debug("Optimizing path... ");
                                LinkedList<Coordinate> path = new LinkedList<>(list);
                                WindowOptimizer optimizer = new WindowOptimizer(path, player.getWorld(), pathFinder);
                                PathAPIMessager.debug("Initial length: " + path.size());
                                optimizer.stepOptimize();

                                PathAPIMessager.debug("Final length: " + path.size());
                                // Perform console testPath
//                                initTestPath();
//                                int[] bestPair1 = getBestPair(testPath, 0, testPath.size());
//                                if(bestPair1 == null) PathAPIMessager.debug("No optimizations found");
//                                else {
//                                    printBestPoints(bestPair1);
//                                    int startCoordIndex = bestPair1[0];
//                                    int endCoordIndex = bestPair1[1];
//                                    PathAPIMessager.debug("Start: " + startCoordIndex + " End: " + endCoordIndex);
//                                    List<Coordinate> shorterPath = new ArrayList<Coordinate>();
//
//                                    for(int i = 2; i <= 7; ++i){
//                                        Coordinate coordinate = new Coordinate(0, 0, i);
//                                        shorterPath.add(coordinate);
//                                    }
//                                    List<Coordinate> subList = testPath.subList(startCoordIndex, endCoordIndex);
//                                    subList.clear();
//                                    subList.addAll(shorterPath);
//                                    PathAPIMessager.debug("New path: " + testPath);
//                                    printMap(getDefaultMap());
//                                }




                                // Run in main thread
//                                new BukkitRunnable() {
//
//                                    @Override
//                                    public void run() {
//
//                                        // Optimized is in gold
//                                        for(Coordinate coord : path){
//                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
//                                            loc.getBlock().setType(Material.GOLD_BLOCK);
//                                        }
//
////                                        for(Coordinate coord : list){
////                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
////                                            loc.getBlock().setType(Material.STONE);
////                                        }
//
//
//                                    }
//                                }.runTask(plugin);
//
//
//                                new BukkitRunnable() {
//                                    @Override
//                                    public void run() {
////                                        for(Coordinate coord : list){
////                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
////                                            loc.getBlock().setType(Material.STONE);
////                                        }
//
//                                        // Optimized
//                                        for(Coordinate coord : path){
//                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
//                                            loc.getBlock().setType(Material.STONE);
//                                        }
//
//                                    }
//                                }.runTaskLater(plugin, 200L);

                            });



                        })
        );
    }

    private int[] getBestPair(List<Coordinate> fullPath, int lowerIndex, int upperIndex){
        Iterator<Coordinate> it = fullPath.listIterator(lowerIndex);

        // Rename upperIndex to upperLimit
        int upperLimit = upperIndex;

        int[] bestMatch = null;
        int bestScore = (int) (0.1 * (upperIndex - lowerIndex)); // Don't attempt to optimize unless it's possible to optimize path length by more than 10%
        int bestPathDistance = Integer.MAX_VALUE;

        for(; lowerIndex < upperIndex; ++lowerIndex){
            Coordinate prev = it.next();
            upperIndex = lowerIndex + 1;
            Iterator<Coordinate> upperIt = fullPath.listIterator(upperIndex);
            for(; upperIndex < upperLimit; ++upperIndex){
                Coordinate next = upperIt.next();

                // Calculate score
                int actualDistance = Math.abs(prev.getX() - next.getX()) + Math.abs(prev.getZ() - next.getZ()); // manhatten
                int pathDistance = upperIndex - lowerIndex;
                int score = pathDistance - actualDistance;

                if(score > bestScore || (score == bestScore && pathDistance < bestPathDistance)){
                    bestScore = score;
                    bestMatch = new int[]{lowerIndex, upperIndex};
                    bestPathDistance = pathDistance;
                }
            }
        }

        return bestMatch;
    }

    private void printBestPoints(int[] bestPair) {
        char[][] map = getDefaultMap();

        // Add inflection points
        for(int index : bestPair){
            Coordinate coord = testPath.get(index);
            int x = coord.getX();
            int z = coord.getZ();
            map[x][z] = 'O';
        }

        printMap(map);
    }

    List<Coordinate> testPath = new ArrayList<>();

    private void initTestPath(){
        testPath.clear();
        // x, z pairs
        int[][] coordInts = {
                { 0, 0 },
                { 0, 1 },
                { 0, 2 },
                { 1, 2 },
                { 1, 3 },
                { 2, 3 },
                { 2, 4 },
                { 3, 4 },
                { 3, 5 },
                { 3, 6 },
                { 2, 6 },
                { 2, 7 },
                { 1, 7 },
                { 1, 8 },
                { 0, 8 },
                { 0, 9 },
                { 0, 10 },
        };

        for(int[] coordInt : coordInts){
            Coordinate coordinate = new Coordinate(coordInt[0], 0, coordInt[1]);
            testPath.add(coordinate);
        }

    }

    private char[][] getDefaultMap(){
        char[][] map = new char[11][11];
        for(int i = 0; i < 11; ++i){
            Arrays.fill(map[i], '.');
        }

        // Add path
        for(Coordinate coord : testPath){
            int x = coord.getX();
            int z = coord.getZ();
            map[x][z] = 'X';
        }
        return map;
    }



    private void printMap(char[][] map){
        for(char[] xline : map){
            StringBuilder line = new StringBuilder();
            for(char point : xline){
                line.append(point);
            }
            PathAPIMessager.debug(line.toString());
        }
    }




}
