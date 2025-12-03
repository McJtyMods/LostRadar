package mcjty.lostradar.network;

import mcjty.lib.varia.ComponentFactory;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ServerMapData;
import mcjty.lostradar.setup.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketStartSearch(String category, int usage) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "startsearch");
    public static final Type<PacketStartSearch> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketStartSearch> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketStartSearch::category,
            ByteBufCodecs.INT, PacketStartSearch::usage,
            PacketStartSearch::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            ServerMapData mapData = ServerMapData.getData(player.level());
            if (category.isEmpty()) {
                mapData.stopSearch(player);
            } else if (mapData.isSearching(player)) {
                if (mapData.isPaused(player)) {
                    mapData.unpauseSearch(player);
                } else {
                    mapData.pauseSearch(player);
                }
            } else {
                if (usage > 0) {
                    int extracted = Registration.RADAR.get().extractEnergyNoMax(player.getMainHandItem(), usage, false);
                    if (extracted < usage) {
                        player.sendSystemMessage(ComponentFactory.translatable("lostradar.notenoughenergy", usage));
                        return;
                    }
                }
                mapData.startSearch(player, category);
            }
        });
    }
}
