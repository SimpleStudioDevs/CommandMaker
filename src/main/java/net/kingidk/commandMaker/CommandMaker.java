package net.kingidk.commandMaker;

import net.kingidk.commandMaker.arguments.ArgsDefinition;
import net.kingidk.commandMaker.commandcreation.ParseCommands;
import net.kingidk.commandMaker.commands.AdminCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommandMaker extends JavaPlugin {
    private final List<ParseCommands> registeredCommands = new ArrayList<>();
    public boolean papi;


    @Override
    public void onLoad() {
        saveDefaultConfig();
        registerCommands();
    }

    @Override
    public void onEnable() {
        var placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if (placeholderAPI == null) {
            getLogger().info("PlaceholderAPI not detected, PAPI-based placeholders will be in plain-text!");
            papi = false;
        } else papi = true;

        Objects.requireNonNull(getCommand("commandmaker")).setExecutor(new AdminCommand(this));
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        unregisterCommands();
    }
    public void reload() {
        unregisterCommands();
        reloadConfig();
        registerCommands();
    }

    private void registerCommands() {
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        for (String cmdName : getConfig().getStringList("config.enabled-commands")) {
            List<String> aliases = getConfig().getStringList("commands." + cmdName + ".aliases");
            List<String> actions = getConfig().getStringList("commands." + cmdName + ".actions");
            String permission = getConfig().getString("commands." + cmdName + ".permission");

            ConfigurationSection argsSection = getConfig().getConfigurationSection("commands." + cmdName + ".args");
            List<ArgsDefinition> argDefs = new ArrayList<>();
            if (argsSection != null) {
                for (String argName : argsSection.getKeys(false)) {
                    String type = argsSection.getString(argName + ".type", "STRING");
                    boolean papi = argsSection.getBoolean(argName + ".placeholder", false);
                    List<String> options = argsSection.getStringList(argName + ".options");
                    argDefs.add(new ArgsDefinition(argName, type, papi, options));
                }
            }

            ParseCommands cmd = new ParseCommands(cmdName, aliases, actions, this, permission, argDefs);
            commandMap.register(getName(), cmd);
            // Force custom commands to take highest priority — overwrite any conflicting registration under the bare name
            knownCommands.put(cmdName.toLowerCase(), cmd);
            for (String alias : aliases) {
                knownCommands.put(alias.toLowerCase(), cmd);
            }
            registeredCommands.add(cmd);
        }
        getLogger().info("Successfully registered " + registeredCommands.size() + " commands to the server");
    }


    private void unregisterCommands() {
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        for (ParseCommands cmd : registeredCommands) {
            cmd.unregister(commandMap);
            knownCommands.remove(cmd.getName());
            knownCommands.remove(getName() + ":" + cmd.getName());
            for (String alias : cmd.getAliases()) {
                knownCommands.remove(alias);
                knownCommands.remove(getName() + ":" + alias);
            }
        }

        registeredCommands.clear();
    }


}
