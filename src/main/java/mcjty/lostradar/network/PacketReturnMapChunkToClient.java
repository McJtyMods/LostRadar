package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import mcjty.lostradar.data.MapChunk;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnMapChunkToClient(ResourceKey<Level> level, MapChunk chunk) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "returnmapchunk");
    public static final Type<PacketReturnMapChunkToClient> TYPE = new Type<>(ID);

    private static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Level>> LEVEL_KEY_CODEC = StreamCodec.of(
            (buf, key) -> buf.writeResourceLocation(key.location()),
            buf -> ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnMapChunkToClient> CODEC = StreamCodec.composite(
            LEVEL_KEY_CODEC, PacketReturnMapChunkToClient::level,
            MapChunk.STREAM_CODEC, PacketReturnMapChunkToClient::chunk,
            PacketReturnMapChunkToClient::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMapData clientMapData = ClientMapData.getData();
            clientMapData.addChunk(level, chunk);
        });
    }
}
