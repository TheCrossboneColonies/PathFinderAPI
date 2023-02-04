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
                            CompletableFuture<List<Coordinate>> pathFuture = new Greedy(start, end).run().getPath();

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
                                for(Coordinate coord : list){
                                    Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
                                    loc.getBlock().setType(Material.GOLD_BLOCK);
                                }

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        for(Coordinate coord : list){
                                            Location loc = new Location(Bukkit.getWorld("world"), coord.getX(), coord.getY(), coord.getZ());
                                            loc.getBlock().setType(Material.STONE);
                                        }
                                    }
                                }.runTaskLater(plugin, 200L);

                            });



                        })
        );
    }

}
