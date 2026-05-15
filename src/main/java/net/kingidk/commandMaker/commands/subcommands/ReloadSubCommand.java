package net.kingidk.commandMaker.commands.subcommands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;


public class ReloadSubCommand {

    public static void runCommand(CommandSender sender, CommandMaker plugin) {
        plugin.reload();
        sender.sendMessage(Component.text("CommandMaker has been reloaded!", NamedTextColor.GREEN));
    }

}
