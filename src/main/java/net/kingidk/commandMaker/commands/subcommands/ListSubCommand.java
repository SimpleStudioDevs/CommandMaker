package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Set;


public class ListSubCommand {

    public static void runCommand(CommandSender sender, CommandMaker plugin) {
        sender.sendMessage(Component.text("Custom Commands:", NamedTextColor.YELLOW, TextDecoration.BOLD));

        Set<String> commands = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("commands")).getKeys(false);
        int i = 0;
        for (String s : commands) {
            sender.sendMessage(Component.text(" " + i + " - " + s));
            i++;
        }
    }

}
