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

        // Send help message to player
        if (args.length == 0) {
            TextComponent link = Component.text("Check out https://kingidk.net")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.UNDERLINED, true)
                    .clickEvent(ClickEvent.openUrl("https://kingidk.net"));

            TextComponent message = Component.text("CommandMaker Version 1.3.2", NamedTextColor.YELLOW, TextDecoration.BOLD)
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
            case "reload" -> reloadCommand(sender);
            case "create" -> createCommand(sender, args);
            case "delete" -> deleteCommand(sender, args);
            case "edit" -> editCommand(sender, args);
            case "enable" -> enableCommand(sender, args);
            case "disable" -> disableCommand(sender, args);
            case "argument" -> argumentCommand(sender, args);
            case "help" -> helpCommand(sender);
            case "list" -> listCommand(sender);

            default -> sender.sendMessage(Component.text("Unknown command!", NamedTextColor.RED));
        }


        return true;
    }


    public void reloadCommand(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("CommandMaker has been reloaded!", NamedTextColor.GREEN));
    }

    // List available custom commands
    public void listCommand(CommandSender sender) {
        sender.sendMessage(Component.text("Custom Commands:", NamedTextColor.YELLOW, TextDecoration.BOLD));

        Set<String> commands = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("commands")).getKeys(false);
        int i = 0;
        for (String s : commands) {
            sender.sendMessage(Component.text(" " + i + " - " + s));
            i++;
        }
    }

    // Send full /cm help message
    public void helpCommand(CommandSender sender) {
        sender.sendMessage(Component.text("CommandMaker Help", NamedTextColor.YELLOW, TextDecoration.UNDERLINED, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Commands:", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false));
        sender.sendMessage(Component.text(" /cm reload | Reload plugin config", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm enable <command name> | Enable a command", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm disable <command name> | Disable a command", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm list | List all custom commands", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm delete <command> | Delete a custom command", NamedTextColor.WHITE));
        sender.sendMessage("");
        sender.sendMessage(Component.text("Edit Commands:", NamedTextColor.GREEN, TextDecoration.BOLD));
        sender.sendMessage(Component.text(" /cm edit <commandName> permission <permission node> | Set a permission for a command. Leave blank to disable", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm edit actions <add/list/remove> | Edit command actions", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm edit aliases <add/list/remove> | Edit command aliases", NamedTextColor.WHITE));
        sender.sendMessage("");
        sender.sendMessage(Component.text("Argument Commands:", NamedTextColor.GREEN, TextDecoration.BOLD));
        sender.sendMessage(Component.text(" /cm argument <commandName> add <argName> <argType> [options]", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  The [options] section is only for the string argument type. Every string written after will be an option for the command. Leave blank for no options/any string", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm argument <commandName> list | List arguments for a command", NamedTextColor.WHITE));
        sender.sendMessage(Component.text(" /cm argument <commandName> remove <argName> | Remove an argument from the command", NamedTextColor.WHITE));




    }

    // Disable a command
    public void disableCommand(CommandSender sender, String[] args) {
        // /cm disable <name>
        //      0        1

        Configuration config = plugin.getConfig();
        List<String> enabledCommands = config.getStringList("config.enabled-commands");

        if (args.length < 2) {
            sender.sendMessage(Component.text("Please specify an existing command", NamedTextColor.RED));
            return;
        }

        if (enabledCommands.contains(args[1])) {
            enabledCommands.remove(args[1]);
            config.set("config.enabled-commands", enabledCommands);
            flushConfig();

            sender.sendMessage(Component.text("Command " + args[1] + " has been disabled", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("This command is not enabled or does not exist", NamedTextColor.RED));
        }
    }

    // Enable a command
    public void enableCommand(CommandSender sender, String[] args) {
        // /cm enable <name>
        //      0        1

        Configuration config = plugin.getConfig();
        List<String> enabledCommands = config.getStringList("config.enabled-commands");
        Set<String> existingCommands = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);

        if (args.length < 2) {
            sender.sendMessage(Component.text("Please specify an existing command", NamedTextColor.RED));
            return;
        }

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

    // Create a command
    public void createCommand(CommandSender sender, String[] args) {
        // /cm create <name>
        if (args.length > 2) {
            sender.sendMessage(Component.text("Too many arguments! /cm create <name>", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Please a name for this command", NamedTextColor.RED));
            return;
        }

        String commandSection = "commands." + args[1].toLowerCase();

        plugin.getConfig().createSection(commandSection);

        flushConfig();
        sender.sendMessage(Component.text("Added command \"" + args[1] + "\"", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm edit " + args[1] + "' to edit the command" , NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm enable " + args[1] + "' to enable the command", NamedTextColor.GREEN));
    }

    // Delete a command
    public void deleteCommand(CommandSender sender, String[] args) {
        // /cm delete <cmdName> confirm
        if (args.length == 1) {
            sender.sendMessage(Component.text("Please specify a command to delete", NamedTextColor.RED));
            return;
        }

        var commandsSection = plugin.getConfig().getConfigurationSection("commands");
        if (commandsSection == null) {
            sender.sendMessage(Component.text("There are no commands to delete"));
            return;
        }
        Set<String> availableCommands  = commandsSection.getKeys(false);
        String name = args[1].toLowerCase();
        if (!availableCommands.contains(name)) {
            sender.sendMessage(Component.text("This command does not exist", NamedTextColor.RED));
            return;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            sender.sendMessage(Component.text("Are you sure you want to delete '" + name + "'? This CANNOT be undone.", NamedTextColor.RED, TextDecoration.BOLD));
            sender.sendMessage(Component.text("If you're sure, use /cm delete " + name + " confirm", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            return;
        }

        plugin.getConfig().set("commands." + name, null);
        List<String> enabledCommands = plugin.getConfig().getStringList("config.enabled-commands");
        enabledCommands.remove(name);
        plugin.getConfig().set("config.enabled-commands", enabledCommands);
        flushConfig();

        sender.sendMessage(Component.text("Command '" + name + "' has been deleted successfully", NamedTextColor.GREEN));
        
    }

    // Edit command arguments
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

            flushConfig();
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
            flushConfig();
            sender.sendMessage(Component.text("Argument '" + argName + "' has been removed from command '" + name + "'"));
        }



    }

    // Edit command settings
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

        // Defer edit settings to their own methods
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

            Set<String> availableActions = Set.of("message", "console", "player", "broadcast");
            if (!availableActions.contains(args[4].toLowerCase())) {
                sender.sendMessage(Component.text("Invalid action type! Options: player, console, player, broadcast", NamedTextColor.RED));
                return;
            }



            String actionPrefix = switch (args[4].toLowerCase()) {
                case "message" -> "MESSAGE:";
                case "broadcast" -> "BROADCAST:";
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
                sender.sendMessage(Component.text("Permission " + args[3] + " set for command " + args[1], NamedTextColor.GREEN));
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
