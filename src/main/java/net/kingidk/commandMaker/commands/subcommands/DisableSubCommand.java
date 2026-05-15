package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.List;


public class DisableSubCommand {

    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {
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
            plugin.reload();

            sender.sendMessage(Component.text("Command " + args[1] + " has been disabled", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("This command is not enabled or does not exist", NamedTextColor.RED));
        }
    }
}
