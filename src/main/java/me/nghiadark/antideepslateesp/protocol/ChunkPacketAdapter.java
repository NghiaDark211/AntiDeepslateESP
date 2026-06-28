package me.nghiadark.antideepslateesp.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import me.nghiadark.antideepslateesp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            final int chunkX = event.getPacket().getIntegers().read(0);
            final int chunkZ = event.getPacket().getIntegers().read(1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                hideChunkDeepslate(player, chunkX, chunkZ);
            });
        }
    }

    private void hideChunkDeepslate(Player player, int chunkX, int chunkZ) {
        World world = player.getWorld();
        if (!world.isChunkLoaded(chunkX, chunkZ)) return;

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        int hideBelow = config.getHideBelowY();
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();

        // Group blocks by chunk section (16-block vertical slices)
        Map<Integer, List<MultiBlockChangeInfo>> sectionChanges = new HashMap<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = worldMinY; y <= hideBelow && y < worldMaxY; y++) {
                    if (chunk.getBlock(x, y, z).getType().isAir()) continue;

                    int sectionY = y >> 4;
                    int sectionIndex = sectionY - (worldMinY >> 4);

                    short relPos = (short)((x << 8) | (z << 4) | (y & 0xF));

                    sectionChanges
                        .computeIfAbsent(sectionIndex, k -> new ArrayList<>())
                        .add(new MultiBlockChangeInfo(
                            relPos,
                            WrappedBlockData.createData(Material.AIR),
                            new ChunkCoordIntPair(chunkX, chunkZ)
                        ));
                }
            }
        }

        if (sectionChanges.isEmpty()) return;

        for (Map.Entry<Integer, List<MultiBlockChangeInfo>> entry : sectionChanges.entrySet()) {
            int sectionIndex = entry.getKey();
            List<MultiBlockChangeInfo> changes = entry.getValue();

            int sectionY = sectionIndex + (worldMinY >> 4);

            long sectionPos = encodeSectionPosition(chunkX, sectionY, chunkZ);

            PluginUtils.sendSectionBlocksUpdate(player, sectionPos, changes);
        }
    }

    private long encodeSectionPosition(int x, int y, int z) {
        return ((long) x & 0x3FFFFFL) << 42
             | ((long) y & 0xFFFFFL)
             | ((long) z & 0x3FFFFFL) << 20;
    }
}
