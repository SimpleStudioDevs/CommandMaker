package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.Set;


public class EnableSubCommand {

    public static void runCommand(CommandSender sender, String[] args, CommandMaker plugin) {
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

            plugin.reload();

            sender.sendMessage(Component.text("Command " + args[1] + " has been enabled", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("This command does not exist!", NamedTextColor.RED));
        }
    }


}
