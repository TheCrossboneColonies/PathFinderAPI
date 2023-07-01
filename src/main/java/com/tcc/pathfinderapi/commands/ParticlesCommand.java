package com.tcc.pathfinderapi.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcc.pathfinderapi.api.Path;
import com.tcc.pathfinderapi.api.visualizers.ParticleVisualizer;
import com.tcc.pathfinderapi.api.visualizers.PathVisualizer;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;

public class ParticlesCommand {
 
    public ParticlesCommand () {}

    public void registerCommand (CommandManager<CommandSender> commandManager) {

        commandManager.command(
            commandManager.commandBuilder("pathapi", ArgumentDescription.of("Find a path between two locations."))
                .permission("pathapi.find.particles")
                .literal("find particles")
                .argument(LocationArgument.of("start"))
                .argument(LocationArgument.of("end"))
                .senderType(Player.class)
                .handler(context -> {

                    Player player = (Player) context.getSender();
                    Location start = context.get("start");
                    Location end = context.get("end");

                    PathVisualizer particleVisualizer = new ParticleVisualizer();
                    new Path(player, start, end).withPathVisualizer(particleVisualizer);
                })
        );
    }
}
