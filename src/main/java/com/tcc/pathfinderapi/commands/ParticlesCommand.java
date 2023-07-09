package com.tcc.pathfinderapi.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.api.Path;
import com.tcc.pathfinderapi.api.visualizers.ParticleVisualizer;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;

public class ParticlesCommand {
 
    public ParticlesCommand () {}

    public void registerCommand (CommandManager<CommandSender> commandManager) {

        commandManager.command(
            commandManager.commandBuilder("pathapi find", ArgumentDescription.of("Find a path between two locations of a world using a particle visualization."))
                .permission("pathapi.find.particles")
                .literal("particles")
                .argument(LocationArgument.of("start"))
                .argument(LocationArgument.of("end"))
                .senderType(Player.class)
                .handler(context -> {

                    Player player = (Player) context.getSender();
                    Location start = context.get("start");
                    Location end = context.get("end");

                    Path path =  new Path(new ParticleVisualizer(), player, start, end);
                    path.generatePath();
                })
        );
    }
}
