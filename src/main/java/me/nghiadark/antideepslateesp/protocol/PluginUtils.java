package me.nghiadark.antideepslateesp.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import org.bukkit.entity.Player;

import java.util.List;

public class PluginUtils {

    public static void sendSectionBlocksUpdate(Player player, long sectionPos,
                                                List<MultiBlockChangeInfo> changes) {
        if (changes.isEmpty()) return;

        int batchSize = 256;
        for (int i = 0; i < changes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, changes.size());
            List<MultiBlockChangeInfo> batch = changes.subList(i, end);

            try {
                PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
                packet.getLongs().write(0, sectionPos);
                packet.getMultiBlockChangeInfoArrays().write(0, batch.toArray(new MultiBlockChangeInfo[0]));

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
