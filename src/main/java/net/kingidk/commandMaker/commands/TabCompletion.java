package net.kingidk.commandMaker.commands;

import net.kingidk.commandMaker.CommandMaker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
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
        if (args.length == 1) {
            return filter(args[0], List.of("disable", "argument", "help", "reload", "enable", "edit", "list", "delete", "create"));
        }

        Configuration config = plugin.getConfig();

        switch (args[0]) {
            case "disable" -> {
                if (args.length == 2) {
                    return filter(args[1], config.getStringList("config.enabled-commands"));
                }
            }
            case "enable", "delete" -> {
                if (args.length == 2) {
                    Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
                    return filter(args[1], keys);
                }
            }
            case "argument" -> { return argumentComplete(args, config); }
            case "edit" -> { return editComplete(args, config); }
        }

        return List.of();
    }


    private List<String> argumentComplete(String[] args, Configuration config) {
        if (args.length <= 2) {
            Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
            return filter(args[args.length - 1], keys);
        }

        if (args.length == 3) {
            return filter(args[2], List.of("add", "list", "remove"));
        }

        if (args[2].equalsIgnoreCase("add")) {
            if (args.length == 4) return List.of("<argName>");
            else if (args.length == 5) return filter(args[4], List.of("STRING", "PLAYER", "INT", "FLOAT"));
            else if (args[4].equalsIgnoreCase("string")) return List.of("[options]");
            else if (args[4].equalsIgnoreCase("player")) return List.of("[boolean]");
            else return List.of();
        }

        if (args[2].equalsIgnoreCase("remove")) {
            if (args.length == 4) {
                Set<String> argKeys = Objects.requireNonNull(config.getConfigurationSection("commands." + args[1] + ".args")).getKeys(false);
                return filter(args[3], argKeys);
            }
        }
        return List.of();
    }

    private List<String> editComplete(String[] args, Configuration config) {
        if (args.length == 2) {
            Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("commands")).getKeys(false);
            return filter(args[1], keys);
        }

        if (args.length == 3) {
            return filter(args[2], List.of("permission", "action", "alias"));
        }

        if (args[2].equalsIgnoreCase("permission")) {
            if (args.length == 4) return List.of("<permissionNode>");
        }

        if (args[2].equalsIgnoreCase("action")) {
            if (args.length == 4) {
                return filter(args[3], List.of("add", "list", "remove"));
            }
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) return filter(args[4], List.of("console", "player", "message"));
                if (args.length == 6) return List.of("<action>");
            }
            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) return List.of("<actionID>");
            }
        }

        if (args[2].equalsIgnoreCase("alias")) {
            if (args.length == 4) {
                return filter(args[3], List.of("add", "list", "remove"));
            }
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) return List.of("<alias>");
            }
            if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) return filter(args[4], config.getStringList("commands." + args[1] + ".aliases"));
            }
        }

        return List.of();
    }

    private List<String> filter(String partial, Collection<String> options) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(partial, options, result);
        return result;
    }

}
