package net.kingidk.commandMaker.commandcreation;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kingidk.commandMaker.CommandMaker;
import net.kingidk.commandMaker.arguments.ArgVerification;
import net.kingidk.commandMaker.arguments.ArgsDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class CustomCommand extends Command {
    private final List<String> actions;
    private final CommandMaker plugin;
    private final String permission;
    private final List<ArgsDefinition> argDefs;
    private final ExecuteActions executeCommands;

    public CustomCommand(String name, List<String> aliases, List<String> actions, CommandMaker plugin, String permission, List<ArgsDefinition> argDefs) {
        super(name);
        this.plugin = plugin;
        setAliases(aliases);
        this.actions = actions;
        this.permission = permission;
        this.argDefs = argDefs;
        this.executeCommands = new ExecuteActions(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NonNull [] args) {
        if (!(permission == null)) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(Component.text("You do not have permission to run this command!", NamedTextColor.RED));
                return true;
            }
        }
        long requiredCount = argDefs.size();
        if (args.length < requiredCount) {
            sender.sendMessage(Component.text("Not enough arguments!", NamedTextColor.RED));
            return true;
        }

        for (String string : actions) {
            if (!string.contains(":")) {
                plugin.getLogger().warning("Incorrectly formatted action! Failed to parse: " + string);
                return true;
            }
            int colonIndex = string.indexOf(":");
            String prefix = string.substring(0, colonIndex + 1);
            String action = string.substring(colonIndex + 1).trim();

            Player placeholderTarget;
            if (sender instanceof Player p) {
                placeholderTarget = p;
            } else placeholderTarget = null;

            // Verify args
            for (int i = 0; i < argDefs.size() && i < args.length; i ++) {
                ArgsDefinition def = argDefs.get(i);
                String arg = args[i];
                boolean valid = switch (def.type().toUpperCase()) {
                    case "INT" -> ArgVerification.isInteger(arg);
                    case "FLOAT" -> ArgVerification.isFloat(arg);
                    case "STRING" -> ArgVerification.validString(arg, def.options());
                    case "PLAYER" -> ArgVerification.validPlayer(arg);
                    default -> true;
                };

                // Send messages if invalid args
                if (!valid) {
                    switch (def.type().toUpperCase()) {
                        case "INT" -> sender.sendMessage(Component.text(
                                "Argument '" + def.name() + "' must be an integer" , NamedTextColor.RED
                        ));
                        case "FLOAT" -> sender.sendMessage(Component.text(
                                "Argument '" + def.name() + "' must be a number", NamedTextColor.RED
                        ));
                        case "STRING" -> sender.sendMessage(Component.text("Invalid string argument! Options: " + def.options(), NamedTextColor.RED));
                        case "PLAYER" -> sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                    }
                    return true;
                }

                // Set papi target to player argument if set in config
                if (def.type().equalsIgnoreCase("PLAYER") && def.papi()) {
                    placeholderTarget = Bukkit.getPlayer(args[i]);
                }
            }


            // Parse Placeholders
            // Args
            for (int i = 0; i < argDefs.size() && i < args.length; i++) {
                action = action.replace("{" + argDefs.get(i).name() + "}", args[i]);
            }

            // Server
            String onlinePlayers = String.valueOf(plugin.getServer().getOnlinePlayers().size());
            String maxPlayers = String.valueOf(plugin.getServer().getMaxPlayers());

            action = action.replace("{onlineplayers}", onlinePlayers);
            action = action.replace("{maxplayers}", maxPlayers);

            if (sender instanceof Player p) {
                action = action.replace("{player}", p.getName());
                action = action.replace("{sender}", p.getName());
            } else {
                action = action.replace("{sender}", "Console");
                action = action.replace("{sender}", "Console");
            }

            // Placeholders for player argument
            if (placeholderTarget != null) {
                action = action.replace("{target}", placeholderTarget.getName());
                action = action.replace("{x}", String.format("%.2f", placeholderTarget.getX()));
                action = action.replace("{y}", String.format("%.2f", placeholderTarget.getY()));
                action = action.replace("{z}", String.format("%.2f", placeholderTarget.getZ()));
                action = action.replace("{world}", placeholderTarget.getWorld().getName());
                action = action.replace("{displayname}", placeholderTarget.displayName().toString());
            }
            if (plugin.papi) {
                action = PlaceholderAPI.setPlaceholders(placeholderTarget, action);
            }

            // Send action out to methods
            switch (prefix) {
                case "MESSAGE:" -> executeCommands.sendMessage(sender, action, false);
                case "BROADCAST:" -> executeCommands.sendMessage(sender, action, true);
                case "CONSOLE:" -> executeCommands.runCommand(sender, action, true);
                case "PLAYER:" -> executeCommands.runCommand(sender, action, false);
                default -> plugin.getLogger().warning("Incorrectly formatted action! Failed to parse: " + string);
            }
        }


        return true;
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NonNull [] args) {
        int index = args.length - 1;

        if (index < argDefs.size()) {
            switch (argDefs.get(index).type().toUpperCase()) {
                case "PLAYER" -> {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[index].toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "INT" -> { return List.of("<whole number>");}
                case "FLOAT" -> { return List.of("<number>");}
                case "STRING" -> {
                    if (argDefs.get(index).options().isEmpty()) {
                        return List.of("<string>");
                    } else return argDefs.get(index).options();}
            }
        }

        return List.of();
    }



}
