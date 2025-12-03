package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Messages {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(LostRadar.MODID)
                .versioned("1.0")
                .optional();

        registrar.playToServer(PacketRequestMapChunk.TYPE, PacketRequestMapChunk.CODEC, PacketRequestMapChunk::handle);
        registrar.playToClient(PacketReturnMapChunkToClient.TYPE, PacketReturnMapChunkToClient.CODEC, PacketReturnMapChunkToClient::handle);
        registrar.playToClient(PacketKnowledgeToPlayer.TYPE, PacketKnowledgeToPlayer.CODEC, PacketKnowledgeToPlayer::handle);
        registrar.playToClient(PacketPauseStateToClient.TYPE, PacketPauseStateToClient.CODEC, PacketPauseStateToClient::handle);

        registrar.playToServer(PacketStartSearch.TYPE, PacketStartSearch.CODEC, PacketStartSearch::handle);
        registrar.playToClient(PacketReturnSearchResultsToClient.TYPE, PacketReturnSearchResultsToClient.CODEC, PacketReturnSearchResultsToClient::handle);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
    }

    public static <T extends CustomPacketPayload> void sendToAll(T packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static <T extends CustomPacketPayload> void sendToAllPlayers(ResourceKey<Level> level, T packet) {
        // For now, broadcast to all players. If needed, this can be optimized per-level later.
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }
}