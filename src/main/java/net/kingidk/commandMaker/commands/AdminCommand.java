package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

            commandSender.sendMessage(message);
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
