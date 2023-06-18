package com.tcc.pathfinderapi.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.objects.Coordinate;
import com.tcc.pathfinderapi.pathing.PathFinder;
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

    private PathFinderAPI pathFinderAPI;
    public FindCommand(PathFinderAPI pathFinderAPI) { this.pathFinderAPI = pathFinderAPI; }

    public void registerCommand (CommandManager<CommandSender> commandManager) {

        commandManager.command(
            commandManager.commandBuilder("pathapi", ArgumentDescription.of("Find a path between two locations."))
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

                    pathFuture.whenComplete((integer, error) -> { if (pathFuture.isCompletedExceptionally()) { System.out.println("Could not find path."); } })
                        .thenAccept(list -> {

                            PathAPIMessager.debug("Path of length " + list.size() + " found in " + (System.currentTimeMillis() - startTime) + " ms.");
                            PathAPIMessager.debug("Optimizing path... ");

                            LinkedList<Coordinate> path = new LinkedList<>(list);
                            WindowOptimizer windowOptimizer = new WindowOptimizer(path, player.getWorld(), pathFinder);
                            PathAPIMessager.debug("Initial length: " + path.size() + ".");
                            windowOptimizer.optimize();

                            PathAPIMessager.debug("Final length: " + path.size() + ".");

                            new BukkitRunnable () {

                                int stepsPerTick = 50;
                                int loopCounter = 0;

                                @Override
                                public void run () {

                                    int firstIndex = stepsPerTick * loopCounter;

                                    for (int index = firstIndex; index < firstIndex + stepsPerTick && index < path.size(); ++index) {

                                        Coordinate coordinate = path.get(index);
                                        Location location = new Location(Bukkit.getWorld("world"), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                                        location.getBlock().setType(Material.GOLD_BLOCK);
                                    }

                                    for (int index = firstIndex; index < firstIndex + stepsPerTick && index < list.size(); ++index) {

                                        Coordinate coordinate = list.get(index);
                                        Location location = new Location(Bukkit.getWorld("world"), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                                        location.getBlock().setType(Material.STONE);
                                    }

                                    if (firstIndex >= path.size() && firstIndex >= list.size()) {

                                        cancel();

                                        new BukkitRunnable () {

                                            int stepsPerTick2 = 50;
                                            int loopCounter2 = 0;

                                            @Override
                                            public void run () {

                                                int firstIndex2 = stepsPerTick2 * loopCounter2;

                                                for (int index = firstIndex2; index < firstIndex2 + stepsPerTick2 && index < path.size(); ++index) {

                                                    Coordinate coordinate = path.get(index);
                                                    Location location = new Location(Bukkit.getWorld("world"), coordinate.getX(), coordinate.getY(), coordinate.getZ());
                                                    location.getBlock().setType(Material.STONE);
                                                }

                                                ++loopCounter2;
                                            }
                                        }.runTaskTimer(pathFinderAPI, 800L, 1L);
                                    }

                                    ++loopCounter;
                                }
                            }.runTaskTimer(pathFinderAPI, 0L, 1L);
                        });
                })
        );
    }
}
