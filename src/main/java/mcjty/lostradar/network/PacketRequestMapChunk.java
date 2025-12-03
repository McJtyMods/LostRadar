package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.EntryPos;
import mcjty.lostradar.data.ServerMapData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRequestMapChunk(EntryPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "requestmapchunk");
    public static final Type<PacketRequestMapChunk> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestMapChunk> CODEC = StreamCodec.composite(
            EntryPos.STREAM_CODEC, PacketRequestMapChunk::pos,
            PacketRequestMapChunk::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var level = player.getCommandSenderWorld();
            ServerMapData mapData = ServerMapData.getData(level);
            mapData.requestMapChunk(level, pos);
        });
    }
}
