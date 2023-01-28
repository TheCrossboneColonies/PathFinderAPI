package com.tcc.pathfinderapi;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.tcc.pathfinderapi.configuration.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class PathFinderAPI extends JavaPlugin {

    private BukkitAudiences adventure;
    private ConfigManager configManager;

    @Override
    public void onEnable() {

        // Load config files
        configManager = new ConfigManager(this);
        configManager.loadConfigFiles();

        // TODO: Set up dependencies


        // TODO: Load data

        // TODO: Start timers


        // TODO: https://docs.adventure.kyori.net/platform/index.html
        // Create Kyori Adventure Audience to be used in this plugin
        adventure = BukkitAudiences.create(this);

        // Register commands
        try {
            CommandManager<CommandSender> manager = new BukkitCommandManager<>(
                    /* Owning plugin */ this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );

//            // Add no permission exception handler
//            BiConsumer<CommandSender, NoPermissionException> noPermExceptionHandler = (sender, ex) -> {
//                sender.sendMessage("You don't have perms dum dum!");
//            };
//            manager.registerExceptionHandler(NoPermissionException.class, noPermExceptionHandler);


            // Minecraft default exception handlers
            new MinecraftExceptionHandler<CommandSender>()
                    .withArgumentParsingHandler()
                    .withInvalidSenderHandler()
                    .withInvalidSyntaxHandler()
                    .withNoPermissionHandler()
                    .withCommandExecutionHandler()
                    .withDecorator(message -> Component.text("[PathAPI]").append(Component.space()).append(message))
                    .apply(manager, adventure::sender);

            // Add help command helper
            MinecraftHelp minecraftHelp = new MinecraftHelp<CommandSender>(
                    "/pathapi help",
                    adventure::sender,
                    manager
            );
            minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                    TextColor.color(0, 255, 0),
                    TextColor.color(0, 255, 255),
                    TextColor.color(255, 255, 0),
                    TextColor.color(120, 255, 120),
                    TextColor.color(200, 0, 200)));

            manager.command(
                    manager.commandBuilder("pathapi")
                            .literal("help")
                            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                            .handler(context -> {
                                minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender());
                            })
            );

            // Add a confirmation handler (for all commands)
            CommandConfirmationManager<CommandSender> confirmationManager = new CommandConfirmationManager<>(
                    30L, // User must confirm within 30
                    TimeUnit.SECONDS, // 30 seconds
                    // Code to run when confirmation manager needs action from the user
                    context -> context.getCommandContext().getSender().sendMessage("Confirmation required! Use /pathapi confirm"),
                    // Code to run when user attempts to confirm an action without any pending actions
                    sender -> sender.sendMessage("You don't have any pending commands")
            );
            confirmationManager.registerConfirmationProcessor(manager);
            manager.command(
            manager.commandBuilder("pathapi")
                    .literal("confirm")
                    .handler(confirmationManager.createConfirmationExecutionHandler())
                );

            // Add a test command that requires confirmation
            manager.command(
                    manager.commandBuilder("pathapi")
                            .permission("pathapi.test")
                            .literal("test")
                            .argument(StringArgument.optional("number", StringArgument.StringMode.SINGLE))
                            .senderType(Player.class)
                            .meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)

                            .handler(context -> {
                                Player player = (Player) context.getSender();
                                String testNum = context.getOrDefault("number", "0");

                                player.setVelocity(new Vector(0, 0.5, -1 * Integer.parseInt(testNum)));
                            })
            );

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Register events

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
