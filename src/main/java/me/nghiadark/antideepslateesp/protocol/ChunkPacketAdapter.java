package me.nghiadark.antideepslateesp.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import me.nghiadark.antideepslateesp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChunkPacketAdapter extends PacketAdapter {

    private static final int BATCH_SIZE = 100;
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
                hideDeepslateBlocks(player, chunkX, chunkZ);
            });
        }
    }

    private void hideDeepslateBlocks(Player player, int chunkX, int chunkZ) {
        World world = player.getWorld();
        if (!world.isChunkLoaded(chunkX, chunkZ)) return;

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        int hideBelow = config.getHideBelowY();
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();

        List<Location> blocks = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = worldMinY; y <= hideBelow && y < worldMaxY; y++) {
                    if (chunk.getBlock(x, y, z).getType().isAir()) continue;
                    blocks.add(new Location(world, chunkX * 16 + x, y, chunkZ * 16 + z));
                }
            }
        }

        if (blocks.isEmpty()) return;

        processBatch(player, blocks, 0);
    }

    private void processBatch(Player player, List<Location> blocks, int index) {
        int end = Math.min(index + BATCH_SIZE, blocks.size());
        for (int i = index; i < end; i++) {
            Location loc = blocks.get(i);
            sendBlockChange(player, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Material.AIR);
        }
        if (end < blocks.size()) {
            int nextIndex = end;
            Bukkit.getScheduler().runTask(plugin, () -> processBatch(player, blocks, nextIndex));
        }
    }

    private void sendBlockChange(Player player, int x, int y, int z, Material material) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
        packet.getBlockData().write(0, WrappedBlockData.createData(material));
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            plugin.debug("Failed to send block change: " + e.getMessage());
        }
    }
}
