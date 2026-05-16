package net.kingidk.commandMaker.commandcreation;

import net.kingidk.commandMaker.CommandMaker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExecuteActions {
    private final CommandMaker plugin;

    public ExecuteActions(CommandMaker plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(CommandSender sender, String action, boolean broadcast) {
        Component component = MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(action));
        if (broadcast) {
            for (Player p :  Bukkit.getOnlinePlayers()) {
                p.sendMessage(component);
            }
        } else {
            sender.sendMessage(component);
        }
    }

    public void runCommand(CommandSender sender, String command, boolean isConsole) {
        if (isConsole) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        } else {
            Player p = (Player) sender;
            p.getScheduler().run(plugin, t -> Bukkit.dispatchCommand(p, command), null);
        }
    }

    public void playSound(CommandSender sender, String soundName, boolean global) {
        if (!(sender instanceof Player) && !global) return;

        NamespacedKey key = NamespacedKey.fromString(soundName.toLowerCase());
        if (key == null) {
            plugin.getLogger().warning("Invalid source key: " + soundName);
            return;
        }

        Sound sound = Registry.SOUNDS.get(key);
        if (sound == null) {
            plugin.getLogger().warning("Unknown sound: " + soundName);
            return;
        }

        if (global) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
            }
        } else {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }


    }





    private static String convertLegacyToMiniMessage(String input) {
        return input
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&l", "<bold>").replace("&o", "<italic>")
                .replace("&n", "<underlined>").replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>").replace("&r", "<reset>");
    }


}
