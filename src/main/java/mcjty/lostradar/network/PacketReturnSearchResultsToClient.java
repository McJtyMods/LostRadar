package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import mcjty.lostradar.data.EntryPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record PacketReturnSearchResultsToClient(Set<ChunkPos> positions, Set<EntryPos> searchedChunks, int progress) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "returnsearchresults");
    public static final Type<PacketReturnSearchResultsToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnSearchResultsToClient> CODEC = StreamCodec.composite(
            net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs.CHUNK_POS.apply(ByteBufCodecs.collection(HashSet::new)), PacketReturnSearchResultsToClient::positions,
            EntryPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), PacketReturnSearchResultsToClient::searchedChunks,
            ByteBufCodecs.INT, PacketReturnSearchResultsToClient::progress,
            PacketReturnSearchResultsToClient::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMapData clientMapData = ClientMapData.getData();
            clientMapData.addSearchResults(positions, searchedChunks);
            clientMapData.setSearchProgress(progress);
        });
    }
}
