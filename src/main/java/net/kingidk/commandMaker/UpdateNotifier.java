package net.kingidk.commandMaker;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateNotifier implements Listener {

    private static final String MODRINTH_URL = "https://api.modrinth.com/v2/project/commandmaker/version";
    private static final String MODRINTH_PAGE = "https://modrinth.com/plugin/commandmaker/versions";

    private final CommandMaker plugin;
    private String latestVersion = null;

    public UpdateNotifier(CommandMaker plugin) {
        this.plugin = plugin;
    }

    public void checkUpdate() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                var conn = (HttpURLConnection) URI.create(MODRINTH_URL).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "CommandMaker/" + plugin.getDescription().getVersion());
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    var versions = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();
                    if (!versions.isEmpty()) {
                        latestVersion = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                        if (isUpdateAvailable()) {
                            plugin.getLogger().info("Update available: " + latestVersion + " (running " + plugin.getDescription().getVersion() + ")");
                            plugin.getLogger().info("Download at " + MODRINTH_PAGE);
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        });
    }

    private boolean isUpdateAvailable() {
        return latestVersion != null && !latestVersion.equals(plugin.getDescription().getVersion());
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("config.update-notification")) return;

        if (!isUpdateAvailable()) return;
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task ->
            updateMessage(e.getPlayer()), 20L
        );
    }


    private void updateMessage(Player p) {
        TextComponent link = Component.text("here", NamedTextColor.LIGHT_PURPLE).clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/commandmaker/versions"));
        if (p.hasPermission("commandmaker.admin")) {
            p.sendMessage("");
            p.sendMessage(
                    Component.text("A new version of", NamedTextColor.YELLOW, TextDecoration.BOLD)
                            .append(Component.text(" CommandMaker ", NamedTextColor.GREEN))
                            .append(Component.text("is available! (" + plugin.getDescription().getVersion() + " -> " + latestVersion + ") Click ", NamedTextColor.YELLOW))
                            .append(link)
                            .append(Component.text(" to view the latest version!", NamedTextColor.YELLOW))

            );
            p.sendMessage("");
        }
    }
}