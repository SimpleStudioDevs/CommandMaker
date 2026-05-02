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

import java.util.*;

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
            case "argument" -> argumentCommand(sender, args);
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
            flushConfig();

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

            flushConfig();

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

        flushConfig();
        sender.sendMessage(Component.text("Added command \"" + args[1] + "\"", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm edit " + args[1] + "' to edit the command" , NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm enable " + args[1] + "' to enable the command", NamedTextColor.GREEN));
    }

    public void argumentCommand(CommandSender sender, String[] args) {
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


        Set<String> availableCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);

        if (!availableCommands.contains(args[1])) {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
            return;
        }

        String name = args[1].toLowerCase();

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

            flushConfig();
            sender.sendMessage(Component.text("Argument " + argName + " added to command " + name, NamedTextColor.GREEN));

        }

        if (args[2].equalsIgnoreCase("list")) {
            Set<String> availableArguments = Objects.requireNonNull(config.getConfigurationSection("commands." + name + ".args")).getKeys(false);
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
                sender.sendMessage(Component.text("This argument does not exist. Use /cm agument <name> list to view options"));
                return;
            }

            config.set("commands." + name + ".args." + argName, null);
            flushConfig();
            sender.sendMessage(Component.text("Argument '" + argName + "' has been removed from command '" + name + "'"));
        }



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
            case "alias" -> editAliases(sender, args);
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

            flushConfig();

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

                flushConfig();
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

            flushConfig();


        } else {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
        }
    }

    public void editAliases(CommandSender sender, String[] args) {
        // /cm edit <name> alias add/remove/list
        //        0     1      2         3

        Configuration config = plugin.getConfig();
        String name = args[1];
        List<String> aliases = config.getStringList("commands." + name + ".aliases");

        if (args[3].equalsIgnoreCase("add")) {

            if (args.length < 5) {
                sender.sendMessage(Component.text("Please specify a command alias", NamedTextColor.GREEN));
                return;
            }

            aliases.add(args[4].toLowerCase());

            config.set("commands." + name + ".aliases", aliases);
            flushConfig();
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
            if (!ArgVerification.isInteger(args[4])) {
                sender.sendMessage(Component.text("You must write the id to the alias you wish to remove. Use /cm edit <name> alias list to view IDs", NamedTextColor.RED));
            } else if (Integer.parseInt(args[4]) > aliases.size()) {
                sender.sendMessage(Component.text("This alias ID does not exist. Check /cm edit <name> alias list", NamedTextColor.RED));
            } else {
                aliases.remove(Integer.parseInt(args[4]));
                config.set("commands." + name + ".aliases", aliases);

                flushConfig();
                sender.sendMessage(Component.text("Successfully removed alias id " + args[4], NamedTextColor.GREEN));

            }
        }
    }


    public void flushConfig() {
        plugin.saveConfig();
        plugin.reload();
    }


}
