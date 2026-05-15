package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.*;

import static net.kingidk.commandMaker.Util.FilterUtil.filter;

public class ArgumentSubCommand {
    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {
        // /cm argument <name> add <argName> <argType> [(STRING) option1 option2 option 3]
        //       0          1   2     3           4
        // /cm argument <name> edit <argName> <type/options>
        // /cm argument <name> remove <argName>
        // /cm argument <name> list <argName>

        Configuration config = plugin.getConfig();
        switch (args.length) {
            case 1 -> {
                sender.sendMessage(Component.text("Please specify the name of the command to edit", NamedTextColor.RED));
                return;
            }
            case 2 -> {
                sender.sendMessage(Component.text("You must specify an action. Options: add, list, remove, edit", NamedTextColor.RED));
                return;
            }
            case 3 -> {
                if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove")) {
                    sender.sendMessage(Component.text("You must specify an argument name", NamedTextColor.RED));
                    return;
                }
            }
        }


        Set<String> validOptions = Set.of("add", "remove", "list");
        if (!validOptions.contains(args[2])) {
            sender.sendMessage(Component.text("Unknown option! Select add, remove, or list"));
            return;
        }

        Set<String> availableCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);

        if (!availableCommands.contains(args[1])) {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
            return;
        }

        String name = args[1].toLowerCase();


        // /cm argument <name> add <argName> <argType> [options]
        if (args[2].equalsIgnoreCase("add")) {
            Set<String> availableOptions = Set.of("string", "player", "int", "float");
            if (args.length < 5 || !availableOptions.contains(args[4].toLowerCase())) {
                sender.sendMessage(Component.text("Please specify a valid argument type. Options: string, player, int, float", NamedTextColor.RED));
                return;
            }

            String type = args[4];
            String argName = args[3];


            config.set("commands." + name + ".args." + argName + ".type", type);
            if (type.equalsIgnoreCase("string") && args.length > 5) {
                List<String> options = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 5, args.length)));
                config.set("commands." + name + ".args." + argName + ".options", options);
            }

            if (type.equalsIgnoreCase("player") && args.length > 5) {
                config.set("commands." + name + ".args." + argName + ".placeholder", args[5]);
            }

            plugin.reload();
            sender.sendMessage(Component.text("Argument " + argName + " added to command " + name, NamedTextColor.GREEN));

        }

        // /cm argument <name> list
        if (args[2].equalsIgnoreCase("list")) {
            var argsSection = config.getConfigurationSection("commands." + name + ".args");
            if (argsSection == null) {
                sender.sendMessage(Component.text("There are no arguments to list", NamedTextColor.YELLOW));
                return;
            }
            Set<String> availableArguments = argsSection.getKeys(false);
            List<String> argList = new ArrayList<>(availableArguments);
            if (availableArguments.isEmpty()) {
                sender.sendMessage(Component.text("There are no arguments to list", NamedTextColor.YELLOW));
                return;
            }

            sender.sendMessage(Component.text("Arguments for command " + name + ":", NamedTextColor.YELLOW));
            for (int i = 0; i < argList.size(); i++) {
                sender.sendMessage(Component.text(i + " - " + argList.get(i)));

                if (Objects.requireNonNull(config.getString("commands." + name + ".args." + argList.get(i) + ".type")).equalsIgnoreCase("string")) {
                    List<String> options = config.getStringList("commands." + name + ".args." + argList.get(i) + ".options");
                    if (!options.isEmpty()) {
                        sender.sendMessage(Component.text("  Options:", NamedTextColor.YELLOW));
                        for (String option : options) {
                            sender.sendMessage(Component.text("    - " + option));
                        }

                    }
                }
            }
        }

        if (args[2].equalsIgnoreCase("remove")) {
            // /cm argument <cmdName> remove <argName>
            //        0         1       2       3


            if (!availableCommands.contains(args[1])) {
                sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
                return;
            }

            String argName = args[3].toLowerCase();

            Set<String> availableArgNames = Objects.requireNonNull(config.getConfigurationSection("commands." + name + ".args")).getKeys(false);
            if (!availableArgNames.contains(argName)) {
                sender.sendMessage(Component.text("This argument does not exist. Use /cm agument <name> list to view options", NamedTextColor.RED));
                return;
            }

            config.set("commands." + name + ".args." + argName, null);
            plugin.reload();
            sender.sendMessage(Component.text("Argument '" + argName + "' has been removed from command '" + name + "'"));
        }
    }

    public static List<String> tabComplete(String[] args, CommandMaker plugin) {

        Configuration config = plugin.getConfig();

        if (args.length == 2) {
            Set<String> keys = plugin.getCommandKeys();
            return filter(args[1], keys);
        }

        if (args.length == 3) {
            return filter(args[2], List.of("add", "list", "remove"));
        }

        if (args[2].equalsIgnoreCase("add")) {
            if (args.length == 4) return List.of("<argName>");
            else if (args.length == 5) return filter(args[4], List.of("STRING", "PLAYER", "INT", "FLOAT"));
            else if (args[4].equalsIgnoreCase("string")) return List.of("[options]");
            else if (args[4].equalsIgnoreCase("player")) return List.of("[boolean]");
            else return List.of();
        }

        if (args[2].equalsIgnoreCase("remove")) {
            if (args.length == 4) {
                Set<String> argKeys = Objects.requireNonNull(config.getConfigurationSection("commands." + args[1] + ".args")).getKeys(false);
                return filter(args[3], argKeys);
            }
        }
        return List.of();
    }

}
