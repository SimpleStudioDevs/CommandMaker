package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kingidk.commandMaker.arguments.ArgVerification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.kingidk.commandMaker.Util.FilterUtil.filter;

public class EditSubCommand {

    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {

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

        // Defer edit settings to their own methods
        switch (setting.toLowerCase()) {
            case "action" -> editAction(sender, args, plugin);
            case "permission" -> editPermission(sender, args, plugin);
            case "alias" -> editAliases(sender, args, plugin);
        }
    }


    public static void editAction(CommandSender sender, String[] args, CommandMaker plugin) {
        // /cm edit <command> action add/remove <(add)actiontype>
        //      0    1         2        3           4
        Configuration config = plugin.getConfig();
        String name = args[1];

        Set<String> possibleCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
        if  (!possibleCommands.contains(name)) {
            sender.sendMessage(Component.text("That command does not exist!", NamedTextColor.RED));
            return;
        }

        if (args.length == 3) {
            sender.sendMessage(Component.text("You must specify an action to perform on this setting", NamedTextColor.RED));
            return;
        }

        List<String> validOptions = List.of("add", "remove", "list");

        if (!validOptions.contains(args[3])) {
            sender.sendMessage(Component.text("Unknown option. Specify either 'add' or 'remove'", NamedTextColor.RED));
            return;
        }

        List<String> actions = config.getStringList("commands." + name + ".actions");

        if (args[3].equalsIgnoreCase("add")) {

            Set<String> availableActions = Set.of("message", "console", "player", "broadcast", "sound", "soundall");
            if (!availableActions.contains(args[4].toLowerCase())) {
                sender.sendMessage(Component.text("Invalid action type! Options: player, console, player, broadcast, sound, soundall", NamedTextColor.RED));
                return;
            }



            String actionPrefix = switch (args[4].toLowerCase()) {
                case "message" -> "MESSAGE:";
                case "broadcast" -> "BROADCAST:";
                case "console" -> "CONSOLE:";
                case "player" -> "PLAYER:";
                case "sound" -> "SOUND:";
                case "soundall" -> "SOUNDALL:";
                default -> null;
            };



            String action = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
            if (action.isBlank()) {
                if (actionPrefix.equals("CONSOLE:") || actionPrefix.equals("PLAYER:")) {
                    sender.sendMessage(Component.text("Command actions MUST have text. Please include the command to run", NamedTextColor.RED));
                    return;
                }
            }
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

    public static void editPermission(CommandSender sender, String[] args, CommandMaker plugin) {

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
                sender.sendMessage(Component.text("Permission " + args[3] + " set for command " + args[1], NamedTextColor.GREEN));
            }

            plugin.reload();


        } else {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
        }
    }

    public static void editAliases(CommandSender sender, String[] args, CommandMaker plugin) {
        // /cm edit <name> alias add/remove/list
        //        0     1      2         3

        Configuration config = plugin.getConfig();
        String name = args[1];
        List<String> aliases = config.getStringList("commands." + name + ".aliases");

        if (args.length == 3) {
            sender.sendMessage(Component.text("You must specify an action to perform on this setting", NamedTextColor.RED));
            return;
        }

        Set<String> validOptions = Set.of("add", "remove", "list");

        if (!validOptions.contains(args[3])) {
            sender.sendMessage(Component.text("Unknown option! Available options: add, remove, list"));
            return;
        }

        if (args[3].equalsIgnoreCase("add")) {

            if (args.length < 5) {
                sender.sendMessage(Component.text("Please specify a command alias", NamedTextColor.GREEN));
                return;
            }

            aliases.add(args[4].toLowerCase());

            config.set("commands." + name + ".aliases", aliases);
            plugin.reload();
            sender.sendMessage(Component.text("Alias '" + args[4] + "' added to command " + name, NamedTextColor.GREEN));

        }
        if (args[3].equalsIgnoreCase("list")) {

            if (aliases.isEmpty()) {
                sender.sendMessage(Component.text("There are no actions to list", NamedTextColor.YELLOW));
                return;
            }

            sender.sendMessage(Component.text("Aliases for command " + name + ":", NamedTextColor.YELLOW));
            for (int i = 0; i < aliases.size(); i++) {
                sender.sendMessage(Component.text(i + " - " + aliases.get(i)));
            }
        }

        if (args[3].equalsIgnoreCase("remove")) {

                if (args.length < 5) {
                    sender.sendMessage(Component.text("Please specify a command alias", NamedTextColor.RED));
                    return;
                }

                String alias = args[4].toLowerCase();

                if (!aliases.contains(alias)) {
                    sender.sendMessage(Component.text("This command alias does not exist!", NamedTextColor.RED));
                    return;
                }

                aliases.remove(alias);
                config.set("commands." + name + ".aliases", aliases);

                plugin.reload();
                sender.sendMessage(Component.text("Successfully removed alias " + alias, NamedTextColor.GREEN));


        }
    }

    public static List<String> tabComplete(String[] args, CommandMaker plugin) {

        if (args.length == 2) {
            Set<String> keys = plugin.getCommandKeys();
            return filter(args[1], keys);
        }

        if (args.length == 3) {
            return filter(args[2], List.of("permission", "action", "alias"));
        }

        if (args[2].equalsIgnoreCase("permission")) {
            if (args.length == 4) return List.of("<permissionNode>");
        }

        if (args[2].equalsIgnoreCase("action")) {
            if (args.length == 4) {
                return filter(args[3], List.of("add", "list", "remove"));
            }
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) return filter(args[4], List.of("console", "player", "message", "broadcast", "sound", "soundall"));
                if (args.length == 6) return List.of("<action>");
            }
            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) return List.of("<actionID>");
            }
        }

        if (args[2].equalsIgnoreCase("alias")) {
            if (args.length == 4) {
                return filter(args[3], List.of("add", "list", "remove"));
            }
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) return List.of("<alias>");
            }
            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) return filter(args[4], plugin.getConfig().getStringList("commands." + args[1] + ".aliases"));
            }
        }

        return List.of();
    }


}
