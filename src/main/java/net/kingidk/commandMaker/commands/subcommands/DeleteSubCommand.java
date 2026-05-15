package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class DeleteSubCommand {

    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {
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
        plugin.reload();

        sender.sendMessage(Component.text("Command '" + name + "' has been deleted successfully", NamedTextColor.GREEN));

    }
}
