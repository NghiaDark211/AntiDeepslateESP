package me.nghiadark.antideepslateesp;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.nghiadark.antideepslateesp.command.ReloadCommand;
import me.nghiadark.antideepslateesp.config.ConfigManager;
import me.nghiadark.antideepslateesp.listener.PlayerListener;
import me.nghiadark.antideepslateesp.protocol.ChunkPacketAdapter;
import me.nghiadark.antideepslateesp.update.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiDeepslateESP extends JavaPlugin {

    private static AntiDeepslateESP instance;
    private ConfigManager configManager;
    private ProtocolManager protocolManager;
    private boolean debug = false;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        configManager.load();

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("antideepslateesp").setExecutor(new ReloadCommand(this));

        ChunkPacketAdapter chunkAdapter = new ChunkPacketAdapter(this);
        protocolManager.addPacketListener(chunkAdapter);

        if (configManager.isUpdateChecker()) {
            new UpdateChecker(this).check();
        }

        getLogger().info("AntiDeepslateESP v" + getPluginMeta().getVersion() + " enabled!");
        getLogger().info("Author: nghiadark");
    }

    @Override
    public void onDisable() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
        getLogger().info("AntiDeepslateESP disabled!");
    }

    public static AntiDeepslateESP getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public void debug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
