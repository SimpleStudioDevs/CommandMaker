package net.kingidk.commandMaker;

import net.kingidk.commandMaker.arguments.ArgsDefinition;
import net.kingidk.commandMaker.commandcreation.CustomCommand;
import net.kingidk.commandMaker.commands.AdminCommand;
import net.kingidk.commandMaker.conditions.ConditionsDefinition;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class CommandMaker extends JavaPlugin {
    private final List<CustomCommand> registeredCommands = new ArrayList<>();
    public boolean papi;
    public boolean vault;
    public static Economy econ = null;


    @Override
    public void onLoad() {
        saveDefaultConfig();
        registerCommands();
    }

    @Override
    public void onEnable() {
        // BStats
        final int PLUGINID = 31020;
        new Metrics(this, PLUGINID);

        // Update Notifier
        UpdateNotifier updateNotifier = new UpdateNotifier(this);
        updateNotifier.checkUpdate();
        getServer().getPluginManager().registerEvents(updateNotifier, this);

        // Update config | Move to its own check later if more config options are added
        if (!getConfig().isSet("version")) {
            getConfig().set("version", "1");
            getConfig().set("config.update-notification", true);
            saveConfig();
        }


        var placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if (placeholderAPI == null) {
            getLogger().info("PlaceholderAPI not detected, PAPI-based placeholders will be in plain-text!");
            papi = false;
        } else papi = true;

        var vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");

        if (vaultPlugin == null) {
            getLogger().info("Vault not detected, {balance} placeholder will be unavailable");
            vault = false;
        } else vault = true;

        Objects.requireNonNull(getCommand("commandmaker")).setExecutor(new AdminCommand(this));
        Objects.requireNonNull(getCommand("commandmaker")).setTabCompleter(new AdminCommand(this) {});
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        unregisterCommands();
    }
    public void reload() {
        unregisterCommands();
        saveConfig();
        reloadConfig();
        registerCommands();
    }

    private void registerCommands() {
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        for (String cmdName : getConfig().getStringList("config.enabled-commands")) {
            // Define command information and details

            List<String> aliases = getConfig().getStringList("commands." + cmdName + ".aliases");
            List<String> actions = getConfig().getStringList("commands." + cmdName + ".actions");
            String permission = getConfig().getString("commands." + cmdName + ".permission");

            ConfigurationSection argsSection = getConfig().getConfigurationSection("commands." + cmdName + ".args");
            List<ArgsDefinition> argDefs = new ArrayList<>();
            if (argsSection != null) {
                for (String argName : argsSection.getKeys(false)) {
                    // Define argument information, add each to argDefs array
                    String type = argsSection.getString(argName + ".type", "STRING");
                    boolean papi = argsSection.getBoolean(argName + ".placeholder", false);
                    List<String> options = argsSection.getStringList(argName + ".options");
                    argDefs.add(new ArgsDefinition(argName, type, papi, options));
                }
            }

            ConfigurationSection conditionsSection = getConfig().getConfigurationSection("commands." + cmdName + ".conditions");
            List<ConditionsDefinition> conditionDefs = new ArrayList<>();
            if (conditionsSection != null) {
                for (String conditionName : conditionsSection.getKeys(false)) {
                    String variable =  conditionsSection.getString(conditionName + ".variable");
                    double min = conditionsSection.getDouble(conditionName + ".min");
                    double max = conditionsSection.getDouble(conditionName + ".max");
                    String message = conditionsSection.getString(conditionName + ".message");
                    conditionDefs.add(new ConditionsDefinition(variable, min, max, message));
                }
            }


            // Register built command to the server
            CustomCommand cmd = new CustomCommand(cmdName, aliases, actions, this, permission, argDefs, conditionDefs);
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
        for (CustomCommand cmd : registeredCommands) {
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

    public Set<String> getCommandKeys() {
        return Objects.requireNonNull(getConfig().getConfigurationSection("commands")).getKeys(false);
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();

        return true;




    }



}
