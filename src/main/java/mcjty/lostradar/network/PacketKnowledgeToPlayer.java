package mcjty.lostradar.network;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.radar.GuiRadar;
import mcjty.lostradar.setup.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record PacketKnowledgeToPlayer(Set<String> knownCategories) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "knowledgetoplayer");
    public static final Type<PacketKnowledgeToPlayer> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketKnowledgeToPlayer> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(HashSet::new)), PacketKnowledgeToPlayer::knownCategories,
            PacketKnowledgeToPlayer::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var data = player.getData(Registration.PLAYER_KNOWLEDGE);
            Set<String> set = new HashSet<>(data.knownCategories());
            set.clear();
            set.addAll(knownCategories);
            player.setData(Registration.PLAYER_KNOWLEDGE, new PlayerMapKnowledge(set));
            GuiRadar.refresh();
        });
    }
}
