package me.nghiadark.antideepslateesp.command;

import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final AntiDeepslateESP plugin;

    public ReloadCommand(AntiDeepslateESP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text("Usage: /adsesp reload")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!sender.hasPermission("antideepslateesp.reload")) {
            sender.sendMessage(Component.text("You don't have permission!")
                    .color(NamedTextColor.RED));
            return true;
        }

        try {
            plugin.getConfigManager().load();
            sender.sendMessage(Component.text("AntiDeepslateESP configuration reloaded!")
                    .color(NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Error reloading config: " + e.getMessage())
                    .color(NamedTextColor.RED));
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
        }

        return true;
    }
}
