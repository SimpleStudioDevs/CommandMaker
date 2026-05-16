package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Set;

public class CreateSubCommand {

    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {
        // /cm create <name>

        if (args.length > 2) {
            sender.sendMessage(Component.text("Too many arguments! /cm create <name>", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Please a name for this command", NamedTextColor.RED));
            return;
        }
        Set<String> existingCommands = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("commands")).getKeys(false);
        if (existingCommands.contains(args[1])) {
            sender.sendMessage(Component.text("This command already exists!", NamedTextColor.RED));
            return;
        }

        String commandSection = "commands." + args[1].toLowerCase();

        plugin.getConfig().createSection(commandSection);

        plugin.reload();
        sender.sendMessage(Component.text("Added command \"" + args[1] + "\"", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm edit " + args[1] + "' to edit the command" , NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Use '/cm enable " + args[1] + "' to enable the command", NamedTextColor.GREEN));
    }

}
