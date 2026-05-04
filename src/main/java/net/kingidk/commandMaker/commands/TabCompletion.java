package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TabCompletion implements TabCompleter {
    private final CommandMaker plugin;

    public TabCompletion(CommandMaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1 && args[0].isEmpty()) {
            return List.of("disable", "argument", "help", "reload", "enable", "edit");
        }

        Configuration config = plugin.getConfig();

        switch (args[0]) {
            case "disable" -> {
                if (args.length == 1) {
                    return config.getStringList("config.enabled-commands");
                }
            }
            case "enable" -> {
                if (args.length == 1) {
                    Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
                    return new ArrayList<>(keys);
                }
            }
            case "argument" -> { return argumentComplete(args, config); }
            case "edit" -> { return editComplete(args, config); }
         }

        return List.of();
    }

    private List<String> argumentComplete(String[] args, Configuration config) {
        // /cm argument <commandName> list

        if (args.length == 1) {
            Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
            return new ArrayList<>(keys);
        }

        if (args.length == 2) {
            return List.of("add", "list", "remove");
        }

        if (args[2].equalsIgnoreCase("add")) {
            if (args.length == 3) {
                return List.of("<argName>");
            } else if (args.length == 4) {
                return List.of("STRING", "PLAYER", "INT", "FLOAT");
            } else {
                return List.of("[options]");
            }
        }

        if (args[2].equalsIgnoreCase("remove")) {
            if (args.length == 3) {
                return new ArrayList<>(Objects.requireNonNull(config.getConfigurationSection("commands." + args[1] + ".args")).getKeys(false));
            }
        }
        return List.of();
    }

    private List<String> editComplete(String[] args, Configuration config) {
        // /cm edit <commandName> action add
        if (args.length == 1) {
            Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
            return new ArrayList<>(keys);
        }

        if (args.length == 2) {
            return List.of("permission", "action", "alias");
        }
        if (args[2].equalsIgnoreCase("permission")) {
            if (args.length == 3) {
                return List.of("<permissionNode>");
            }
        }


        if (args[2].equalsIgnoreCase("action")) {
            if (args.length == 3) {
                return List.of("add", "list", "remove");
            }
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 4) {
                    return List.of("CONSOLE", "PLAYER", "MESSAGE");
                }
                if (args.length == 5) {
                    return List.of("<action>");
                }
            }

            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 4) {
                    return List.of("<actionID>");
                }
            }
        }

        if (args[2].equalsIgnoreCase("alias")) {
            if (args.length == 3) {
                return List.of("add", "list", "remove");
            }

            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 4) {
                    return List.of("<alias>");
                }
            }

            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 4) {
                    return config.getStringList("commands." + args[1] + ".aliases");
                }
            }
        }


        return List.of();
    }

}
