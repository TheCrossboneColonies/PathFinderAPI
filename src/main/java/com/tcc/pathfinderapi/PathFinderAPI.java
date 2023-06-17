package com.tcc.pathfinderapi;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.tcc.pathfinderapi.commands.FindCommand;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import com.tcc.pathfinderapi.messaging.PathAPIMessager;
import com.tcc.pathfinderapi.pathing.BlockManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class PathFinderAPI extends JavaPlugin {

    private BukkitAudiences adventure;
    private ConfigManager configManager;
    
    public PathFinderAPI () {

        this.adventure = BukkitAudiences.create(this);
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigFiles();
    }

    @Override
    public void onEnable () {

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

            MinecraftHelp<CommandSender> minecraftHelp = new MinecraftHelp<CommandSender>(
                "/pathapi help",
                this.adventure::sender,
                commandManager
            );

            minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                TextColor.color(0, 255, 0),
                TextColor.color(0, 255, 255),
                TextColor.color(255, 255, 0),
                TextColor.color(120, 255, 120),
                TextColor.color(200, 0, 200))
            );

            commandManager.command(
                commandManager.commandBuilder("pathapi")
                    .literal("help")
                    .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                    .handler(context -> { minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender()); })
            );

            CommandConfirmationManager<CommandSender> commandConfirmationManager = new CommandConfirmationManager<>(
                30L,
                TimeUnit.SECONDS,
                context -> context.getCommandContext().getSender().sendMessage("Confirmation required! Use '/pathapi confirm'."),
                sender -> sender.sendMessage("You don't have any pending commands")
            );

            commandConfirmationManager.registerConfirmationProcessor(commandManager);

            commandManager.command(
                commandManager.commandBuilder("pathapi")
                    .literal("confirm")
                    .handler(commandConfirmationManager.createConfirmationExecutionHandler())
            );

            new FindCommand(this).registerCommand(commandManager);
        } catch (Exception exception) {

            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable () {

        this.adventure.close();
        this.adventure = null;
    }
}
