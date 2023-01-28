package com.tcc.pathfinderapi.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import com.tcc.pathfinderapi.PathFinderAPI;
import com.tcc.pathfinderapi.pathing.PathNode;
import com.tcc.pathfinderapi.pathing.pathfinders.PartialRefinementAStar;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindCommand {

    private PathFinderAPI plugin;

    public FindCommand(PathFinderAPI plugin){
        this.plugin = plugin;
    }

    public void registerCommand(CommandManager<CommandSender> manager){
        manager.command(
                manager.commandBuilder("pathapi")
                        .permission("pathapi.connect")
                        .literal("connect")
                        .argument(LocationArgument.of("start"))
                        .argument(LocationArgument.of("end"))
                        .senderType(Player.class)
                        .handler(context -> {
                            Player player = (Player) context.getSender();
                            Location start = context.get("start");
                            Location end = context.get("end");

                            CompletableFuture<List<PathNode>> pathFuture = new PartialRefinementAStar(plugin).findPath(start, end);

                            // Run this code whether or not a path is found successfully
                            pathFuture.whenComplete((myInt, err) -> {
                                // Exception message
                                if(pathFuture.isCompletedExceptionally()){
                                    System.out.println("Could not find path");
                                }
                            })
                            // Run this code if a path is found successfully
                            .thenAccept(list -> {
                                System.out.println(list);
                            });



                        })
        );
    }

}
