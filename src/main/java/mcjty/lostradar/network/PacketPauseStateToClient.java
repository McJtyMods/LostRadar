package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPauseStateToClient(boolean paused) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "returnpausestate");
    public static final Type<PacketPauseStateToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPauseStateToClient> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketPauseStateToClient::paused,
            PacketPauseStateToClient::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMapData clientMapData = ClientMapData.getData();
            clientMapData.setPauseState(paused);
        });
    }
}
