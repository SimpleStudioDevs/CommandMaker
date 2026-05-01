package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import net.kingidk.commandMaker.arguments.ArgVerification;
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
            case "enable" -> enableCommand(sender, args);
            case "disable" -> disableCommand(sender, args);
        }


        return true;
    }


    public void reloadCommand(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("CommandMaker has been reloaded!", NamedTextColor.GREEN));
    }

    public void disableCommand(CommandSender sender, String[] args) {
        // /cm disable <name>
        //      0        1

        Configuration config = plugin.getConfig();
        List<String> enabledCommands = config.getStringList("config.enabled-commands");

        if (enabledCommands.contains(args[1])) {
            enabledCommands.remove(args[1]);
            config.set("config.enabled-commands", enabledCommands);
            plugin.reload();

            sender.sendMessage(Component.text("Command " + args[1] + " has been disabled", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("This command is not enabled or does not exist", NamedTextColor.RED));
        }
    }

    public void enableCommand(CommandSender sender, String[] args) {
        // /cm enable <name>
        //      0        1

        Configuration config = plugin.getConfig();
        List<String> enabledCommands = config.getStringList("config.enabled-commands");
        Set<String> existingCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);

        if (enabledCommands.contains(args[1])) {
            sender.sendMessage(Component.text("This command is already enabled!", NamedTextColor.YELLOW));
            return;
        }

        if (existingCommands.contains(args[1])) {
            enabledCommands.add(args[1]);
            config.set("config.enabled-commands", enabledCommands);

            plugin.reload();

            sender.sendMessage(Component.text("Command " + args[1] + " has been enabled", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
        }


    }

    public void createCommand(CommandSender sender, String[] args) {
        // /cm create <name>
        if (args.length > 2) {
            sender.sendMessage(Component.text("Too many arguments! /cm create <name>", NamedTextColor.RED));
            return;
        }

        String commandSection = "commands." + args[1].toLowerCase();

        plugin.getConfig().createSection(commandSection);

        plugin.reload();
        sender.sendMessage(Component.text("Added command \"" + args[1] + "\"", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm edit " + args[1] + "' to edit the command" , NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm enable " + args[1] + "' to enable the command", NamedTextColor.GREEN));
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
            case "permission" -> editPermission(sender, args);
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

        List<String> validOptions = List.of("add", "remove", "list");

        if (!validOptions.contains(args[3])) {
            sender.sendMessage(Component.text("Unknown option. Specify either 'add' or 'remove'", NamedTextColor.RED));
            return;
        }

        List<String> actions = config.getStringList("commands." + name + ".actions");

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



            String action = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
            action = actionPrefix + action;

            actions.add(action);

            config.set("commands." + name + ".actions", actions);

            plugin.reload();

            sender.sendMessage(Component.text("Action '" + action + "' added to command " + name, NamedTextColor.GREEN));

        }

        if (args[3].equalsIgnoreCase("list")) {
            if (actions.isEmpty()) {
                sender.sendMessage(Component.text("There are no actions to list", NamedTextColor.YELLOW));
                return;
            }

            sender.sendMessage(Component.text("Actions for command " + name + ":", NamedTextColor.YELLOW));
            for (int i = 0; i < actions.size(); i++) {
                sender.sendMessage(Component.text(i + " - " + actions.get(i)));
            }
        }

        if (args[3].equalsIgnoreCase("remove")) {
            if (!ArgVerification.isInteger(args[4])) {
                sender.sendMessage(Component.text("You must write the id to the action you wish to remove. Use /cm edit <name> action list to view IDs", NamedTextColor.RED));
            } else if (Integer.parseInt(args[4]) > actions.size()) {
                sender.sendMessage(Component.text("This action ID does not exist. Check /cm edit <name> action list", NamedTextColor.RED));
            } else {
                actions.remove(Integer.parseInt(args[4]));
                config.set("commands." + name + ".actions", actions);

                plugin.reload();
                sender.sendMessage(Component.text("Successfully removed action id " + args[4], NamedTextColor.GREEN));
            }
        }


    }

    public void editPermission(CommandSender sender, String[] args) {
        // /cm edit <name> <permission>
        //       0    1          2

        Configuration config = plugin.getConfig();
        Set<String> availableCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);

        if (availableCommands.contains(args[1])) {

            if (args.length < 4) {
                config.set("commands." + args[1] + ".permission", null);
                sender.sendMessage(Component.text("Permission has been removed from this command. Any player will be able to run it", NamedTextColor.GREEN));
            } else {
                config.set("commands." + args[1] + ".permission", args[3]);
                sender.sendMessage(Component.text("Permission " + args[3] + " set for command " + args[1]));
            }

            plugin.reload();


        } else {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
        }
    }

    public void editAliases(CommandSender sender, String[] args) {
        // /cm edit <name> alias add/remove/list
        //        0     1      2         3

        Configuration config = plugin.getConfig();
        String name = args[1];

        if (args[3].equalsIgnoreCase("add")) {

            if (args.length < 5) {
                sender.sendMessage(Component.text());
            }

            config.set("commands." + name + ".actions", args[4]);

            plugin.reload();

            sender.sendMessage(Component.text("Alias '" + args[4] + "' added to command " + name, NamedTextColor.GREEN));

        }

    }

}
