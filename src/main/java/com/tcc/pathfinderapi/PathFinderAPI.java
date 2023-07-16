package com.tcc.pathfinderapi;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import com.tcc.pathfinderapi.commands.BlocksCommand;
import com.tcc.pathfinderapi.commands.ParticlesCommand;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.pathing.BlockManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.function.Function;

public final class PathFinderAPI extends JavaPlugin {

    private BukkitAudiences adventure;
    private ConfigManager configManager;
    public static ArrayList<Integer> scheduledTaskIDs = new ArrayList<Integer>();
    
    public PathFinderAPI () {

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigFiles();
    }

    @Override
    public void onEnable () {

        this.adventure = BukkitAudiences.create(this);
        new PathAPIMessager(this, this.configManager);
        new BlockManager(this.configManager);

        try {
            CommandManager<CommandSender> commandManager = new BukkitCommandManager<>(
                this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
            );

            new MinecraftExceptionHandler<CommandSender>()
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler()
                .withCommandExecutionHandler()
                .withDecorator(message -> Component.text("[PathAPI]").append(Component.space()).append(message))
                .apply(commandManager, this.adventure::sender);

            new BlocksCommand().registerCommand(commandManager);
            new ParticlesCommand().registerCommand(commandManager);
        } catch (Exception exception) {

            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable () {

        this.adventure.close();
        this.adventure = null;

        BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
        for (int scheduledTaskID : scheduledTaskIDs) { bukkitScheduler.cancelTask(scheduledTaskID); }
    }
}
