package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AdminCommand implements CommandExecutor {
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

        if (args.length == 0) {
            TextComponent link = Component.text("Check out https://kingidk.net")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.UNDERLINED, true)
                    .clickEvent(ClickEvent.openUrl("https://kingidk.net"));

            TextComponent message = Component.text("CommandMaker Version 1.3.2", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .appendNewline()
                    .append(Component.text("To reload config: /commandmaker reload", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false))
                    .appendNewline()
                    .append(Component.text("Need help setting up, or want to make more complex commands?", NamedTextColor.AQUA))
                    .appendNewline()
                    .append(link);

            sender.sendMessage(message);
            return true;
        }


        switch (args[0].toLowerCase()) {
            case "reload" -> reloadCommand(sender);
            case "create" -> createCommand(sender, args);
            case "edit" -> editCommand(sender, args);
        }


        return true;
    }


    public void reloadCommand(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("CommandMaker has been reloaded!", NamedTextColor.GREEN));
    }

    public void createCommand(CommandSender sender, String[] args) {
        // /cm create <name>
        if (args.length > 2) {
            sender.sendMessage(Component.text("Too many arguments! /cm create <name>", NamedTextColor.RED));
            return;
        }

        String commandSection = "commands." + args[1].toLowerCase();

        plugin.getConfig().createSection(commandSection);
        plugin.saveConfig();

        plugin.reload();
        sender.sendMessage(Component.text("Added command \"" + args[1] + "\"", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm edit " + args[1] + "' to edit the command" , NamedTextColor.GREEN));

    }

    public void editCommand(CommandSender sender, String[] args) {
        // /cm edit <name> <setting>
        //      0     1      2

        switch (args.length) {
            case 1 -> {
                sender.sendMessage(Component.text("Please specify a command name", NamedTextColor.RED));
                return; }
            case 2 -> {
                sender.sendMessage(Component.text("Please specify a setting to edit", NamedTextColor.RED));
                return;
            }
            default -> {}
        }

        Configuration config =  plugin.getConfig();

        String name = args[1];
        String setting = args[2];

        Set<String> possibleCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
        if  (!possibleCommands.contains(name)) {
            sender.sendMessage(Component.text("That command does not exist!", NamedTextColor.RED));
            return;
        }

        Set<String> possibleSettings = Set.of("action", "permission", "alias");
        if (!possibleSettings.contains(setting)) {
            sender.sendMessage(Component.text("That setting does not exist!", NamedTextColor.RED));
            return;
        }

        switch (setting.toLowerCase()) {
            case "action" -> editAction(sender, args);
            case "permission" -> {}
            case "alias" -> {}
        }

    }


    public void editAction(CommandSender sender, String[] args) {
        // /cm edit <command> action add/remove <(add)actiontype>
        //      0    1         2        3           4
        Configuration config = plugin.getConfig();
        String name = args[1];

        Set<String> possibleCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
        if  (!possibleCommands.contains(name)) {
            sender.sendMessage(Component.text("That command does not exist!", NamedTextColor.RED));
            return;
        }

        if (!args[3].equalsIgnoreCase("add") && !args[2].equalsIgnoreCase("remove")) {
            sender.sendMessage(Component.text("Unknown option. Specify either 'add' or 'remove'", NamedTextColor.RED));
            return;
        }

        if (args[3].equalsIgnoreCase("add")) {

            Set<String> availableActions = Set.of("message", "console", "player");
            if (!availableActions.contains(args[4])) {
                sender.sendMessage(Component.text("Invalid action type! Options: message, console, player", NamedTextColor.RED));
                return;
            }



            String actionPrefix = switch (args[4].toLowerCase()) {
                case "message" -> "MESSAGE:";
                case "console" -> "CONSOLE:";
                case "player" -> "PLAYER:";
                default -> null;
            };

            List<String> actions = config.getStringList("commands." + name + ".actions");

            String action = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
            action = actionPrefix + action;

            actions.add(action);

            config.set("commands." + name + ".actions", actions);

            plugin.saveConfig();
            plugin.reload();

            sender.sendMessage(Component.text("Action '" + action + "' added to command " + name, NamedTextColor.GREEN));

        }


    }

}
