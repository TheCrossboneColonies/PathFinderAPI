package com.tcc.pathfinderapi.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.pathfinders.Greedy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
                            CompletableFuture<List<Coordinate>> pathFuture = Greedy.getBuilder(start, end).build().run().getPath();

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
                                // Perform console testPath
                                initTestPath();
                                testInfPoints = getInflectionPoints(testPath, 0 , testPath.size());
                                printInflectionPoints();

                                // Run in main thread
//                                new BukkitRunnable() {
//
//                                    @Override
//                                    public void run() {
//                                        for(Coordinate coord : list){
//                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
//                                            loc.getBlock().setType(Material.GOLD_BLOCK);
//                                        }
//
//                                        for(Integer index : getInflectionPoints(list, 0, list.size())){
//                                            Coordinate coord = list.get(index);
//                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
//                                            loc.getBlock().setType(Material.STONE);
//                                        }
//                                    }
//                                }.runTask(plugin);
//
//
//                                new BukkitRunnable() {
//                                    @Override
//                                    public void run() {
//                                        for(Coordinate coord : list){
//                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
//                                            loc.getBlock().setType(Material.STONE);
//                                        }
//                                    }
//                                }.runTaskLater(plugin, 200L);

                            });



                        })
        );
    }

    List<Coordinate> testPath = new ArrayList<>();
    List<Integer> testInfPoints;
    private void initTestPath(){
        testPath.clear();
        // x, z pairs
        int[][] coordInts = {
                { 0, 0 },
                { 0, 1 },
                { 0, 2 },
                { 0, 3 },
                { 1, 3 },
                { 1, 4 },
                { 2, 4 },
                { 2, 5 },
                { 3, 5 },
                { 4, 5 },
                { 4, 6 },
                { 5, 6 },
                { 5, 7 },
                { 6, 7 },
                { 6, 8 },
                { 6, 9 },
                { 6, 10 },
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

    private void printInflectionPoints(){

        char[][] map = getDefaultMap();

        // Add inflection points
        for(Integer index : testInfPoints){
            Coordinate coord = testPath.get(index);
            int x = coord.getX();
            int z = coord.getZ();
            map[x][z] = 'X';
        }

        printMap(map);
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

    private List<Integer> getInflectionPoints(List<Coordinate> fullPath, int lowerIndex, int upperIndex){

        List<Integer> inflectionPoints = new ArrayList<>();

        // Always include start
        inflectionPoints.add(lowerIndex);

        // Initialize slope variables
        int prevRise = 0; // Track initial change in z
        int prevRun = 0; // Track initial change in x
        int currRise = 0;
        int currRun = 0;
        // Track previous direction to find corners
        boolean lastChangeX = false;

        // Create iterator (more efficient for LinkedList than .get())
        Iterator<Coordinate> it = fullPath.listIterator(lowerIndex);
        Coordinate prev = it.next();
        for(; lowerIndex < upperIndex - 1; ++lowerIndex){
            Coordinate curr = it.next();

            if(curr.getX() - prev.getX() == 0){ // If changing in z direction
                // Found corner
                if(lastChangeX){
                    // Check for slope change (rise / run)
                    if(currRise != prevRise || currRun != prevRun){
                        // Found inflection point
                        inflectionPoints.add(lowerIndex - currRun);

                        prevRise = currRise;

                        // Reset
                        currRise = 0;
                    }
                }

                lastChangeX = false;
                ++currRise;
            }

            if(curr.getZ() - prev.getZ() == 0){ // If changing in x direction
                // Found corner
                if(!lastChangeX){
                    // Check for slope change (rise / run)
                    if(currRise != prevRise || currRun != prevRun){
                        // Found inflection point
                        inflectionPoints.add(lowerIndex - currRise);

                        prevRun = currRun;

                        // Reset
                        currRun = 0;
                    }
                }

                lastChangeX = true;
                ++currRun;
            }

            // PRINT CURRENT STATE OF VARIABLES
            char[][] map = getDefaultMap();

            map[prev.getX()][prev.getZ()] = 'P';
            map[prev.getX()][prev.getZ()] = 'C';
            PathAPIMessager.debug("PRINTING STATE FOR LOOP " + lowerIndex
                    + "\nPrevRise: " + prevRise
                    + "\nPrevRun: " + prevRun
                    + "\nCurrRise: " + currRise
                    + "\nCurrRun: " + currRun
                    + "\nLastX: " + lastChangeX);
            printMap(map);


            prev = curr;
        }

        // Always include end
        inflectionPoints.add(upperIndex - 1);

        return inflectionPoints;
    }

}
