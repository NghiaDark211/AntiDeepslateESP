package me.nghiadark.antideepslateesp.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import me.nghiadark.antideepslateesp.config.ConfigManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkPacketAdapter extends PacketAdapter {

    private final AntiDeepslateESP plugin;
    private final ConfigManager config;

    public ChunkPacketAdapter(AntiDeepslateESP plugin) {
        super(plugin, PacketType.Play.Server.MAP_CHUNK);
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isPlayerTemporary()) return;

        Player player = event.getPlayer();
        if (player.getLocation().getY() > config.getThresholdY()) {
            try {
                byte[] original = event.getPacket().getSpecificModifier(byte[].class).read(0);
                if (original == null || original.length == 0) return;

                World world = player.getWorld();
                int minY = world.getMinHeight();
                int maxY = world.getMaxHeight();
                int hideBelow = config.getHideBelowY();

                byte[] modified = SectionModifier.clearSectionsBelow(original, minY, maxY, hideBelow);
                if (modified != original) {
                    event.getPacket().getSpecificModifier(byte[].class).write(0, modified);
                }
            } catch (Exception e) {
                plugin.debug("Failed to modify chunk: " + e.getMessage());
            }
        }
    }
}
