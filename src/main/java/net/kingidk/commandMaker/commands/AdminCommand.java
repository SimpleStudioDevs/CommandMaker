package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import net.kingidk.commandMaker.commands.subcommands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kingidk.commandMaker.Util.FilterUtil.filter;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final CommandMaker plugin;

    public AdminCommand(CommandMaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("commandmaker.admin")) {
            sender.sendMessage(Component.text("You do not have permission!", NamedTextColor.RED));
            return true;
        }

        // Send help message to player
        if (args.length == 0) {
            TextComponent link = Component.text("Check out https://kingidk.net")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.UNDERLINED, true)
                    .clickEvent(ClickEvent.openUrl("https://kingidk.net"));

            TextComponent message = Component.text("CommandMaker", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .appendNewline()
                    .append(Component.text("For a list of commands, use /cm help", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false))
                    .appendNewline()
                    .append(Component.text("Need help setting up, or want to make more complex commands?", NamedTextColor.AQUA))
                    .appendNewline()
                    .append(link);

            sender.sendMessage(message);
            return true;
        }

        // Defer each command task to their own methods
        switch (args[0].toLowerCase()) {
            case "reload" -> ReloadSubCommand.runCommand(sender, plugin);
            case "create" -> CreateSubCommand.runCommand(sender, args, plugin);
            case "delete" -> DeleteSubCommand.runCommand(sender, args, plugin);
            case "edit" -> EditSubCommand.runCommand(sender, args, plugin);
            case "enable" -> EnableSubCommand.runCommand(sender, args, plugin);
            case "disable" -> DisableSubCommand.runCommand(sender, args, plugin);
            case "argument" -> ArgumentSubCommand.runCommand(sender, args, plugin);
            case "help" -> HelpSubCommand.runCommand(sender);
            case "list" -> ListSubCommand.runCommand(sender, plugin);

            default -> sender.sendMessage(Component.text("Unknown command!", NamedTextColor.RED));
        }


        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return filter(args[0], List.of("disable", "argument", "help", "reload", "enable", "edit", "list", "delete", "create"));
        }

        Configuration config = plugin.getConfig();

        switch (args[0]) {
            case "disable" -> {
                if (args.length == 2) {
                    return filter(args[1], config.getStringList("config.enabled-commands"));
                }
            }
            case "enable", "delete" -> {
                if (args.length == 2) {
                    Set<String> keys = plugin.getCommandKeys();
                    return filter(args[1], keys);
                }
            }
            case "argument" -> { return ArgumentSubCommand.tabComplete(args, plugin); }
            case "edit" -> { return EditSubCommand.tabComplete(args, plugin); }
        }
        return List.of();
    }
}
