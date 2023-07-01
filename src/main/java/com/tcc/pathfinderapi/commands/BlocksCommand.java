package com.tcc.pathfinderapi.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import com.tcc.pathfinderapi.api.Path;
import com.tcc.pathfinderapi.api.visualizers.BlockVisualizer;
import com.tcc.pathfinderapi.api.visualizers.PathVisualizer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FindCommand {

    public FindCommand () {}

    public void registerCommand (CommandManager<CommandSender> commandManager) {

        commandManager.command(
            commandManager.commandBuilder("pathapi", ArgumentDescription.of("Find a path between two locations."))
                .permission("pathapi.find.blocks")
                .literal("find blocks")
                .argument(LocationArgument.of("start"))
                .argument(LocationArgument.of("end"))
                .senderType(Player.class)
                .handler(context -> {

                    Player player = (Player) context.getSender();
                    Location start = context.get("start");
                    Location end = context.get("end");

                    PathVisualizer blockVisualizer = new BlockVisualizer();
                    new Path(player, start, end).withPathVisualizer(blockVisualizer);
                })
        );
    }
}
