package me.nghiadark.antideepslateesp.config;

import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final AntiDeepslateESP plugin;
    private int thresholdY;
    private int hideBelowY;
    private List<String> surfaceCommands;
    private List<String> miningCommands;
    private boolean updateChecker;
    private String githubRepo;

    public ConfigManager(AntiDeepslateESP plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        thresholdY = config.getInt("threshold-y", 6);
        hideBelowY = config.getInt("hide-below-y", 0);
        surfaceCommands = config.getStringList("auto-commands.surface");
        miningCommands = config.getStringList("auto-commands.mining");
        updateChecker = config.getBoolean("update-checker", true);
        githubRepo = config.getString("github-repo", "nghiadark/AntiDeepslateESP");

        plugin.getLogger().info("Configuration loaded: threshold-y=" + thresholdY
                + ", hide-below-y=" + hideBelowY);
    }

    public int getThresholdY() {
        return thresholdY;
    }

    public int getHideBelowY() {
        return hideBelowY;
    }

    public List<String> getSurfaceCommands() {
        return surfaceCommands;
    }

    public List<String> getMiningCommands() {
        return miningCommands;
    }

    public boolean isUpdateChecker() {
        return updateChecker;
    }

    public String getGithubRepo() {
        return githubRepo;
    }
}
