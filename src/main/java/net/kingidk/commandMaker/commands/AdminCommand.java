package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {
    private final CommandMaker plugin;

    public AdminCommand(CommandMaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("commandmaker.admin")) {
            commandSender.sendMessage(Component.text("You do not have permission!", NamedTextColor.RED));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            commandSender.sendMessage(Component.text("CommandMaker has been reloaded!", NamedTextColor.GREEN));
            return true;
        }

        return false;
    }
}
