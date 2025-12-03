package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostradar.compat.LostCitiesCompat;
import mcjty.lostradar.setup.Registration;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record PlayerMapKnowledge(Set<String> knownCategories) {

    public static final Codec<PlayerMapKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("categories").forGetter(s -> new ArrayList<>(s.knownCategories))
    ).apply(instance, list -> new PlayerMapKnowledge(new HashSet<>(list))));

    public static final PlayerMapKnowledge DEFAULT = new PlayerMapKnowledge(new HashSet<>());

    private static final Component DEFAULT_NAME = Component.literal("@");
    private static final CommandSource EMPTY = new CommandSource() {
        @Override
        public void sendSystemMessage(Component component) {
        }

        @Override
        public boolean acceptsSuccess() {
            return false;
        }

        @Override
        public boolean acceptsFailure() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    };

    public PlayerMapKnowledge addCategory(String category) {
        Set<String> categories = new HashSet<>(knownCategories);
        categories.add(category);
        return new PlayerMapKnowledge(categories);
    }

    public void tick(ServerPlayer player) {
        ILostCityInformation lostInfo = LostCitiesCompat.lostCities.getLostInfo(player.level());
        if (lostInfo != null) {
            ChunkPos pos = player.chunkPosition();
            ILostChunkInfo chunkInfo = lostInfo.getChunkInfo(pos.x, pos.z);
            if (chunkInfo != null) {
                PaletteCache cache = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(player.level()));
                ResourceLocation buildingId = chunkInfo.getBuildingId();
                if (buildingId != null) {
                    MapPalette.PaletteEntry entry = cache.getEntryForBuilding(buildingId);
                    if (entry != null) {
                        if (knownCategories.add(entry.name())) {
                            player.setData(Registration.PLAYER_KNOWLEDGE, new PlayerMapKnowledge(knownCategories));
                            if (!entry.commands().isEmpty()) {
                                MinecraftServer server = player.server;
                                CommandSourceStack stack = new CommandSourceStack(EMPTY, Vec3.atCenterOf(player.blockPosition()), Vec2.ZERO, (ServerLevel) player.level(), 2,
                                        DEFAULT_NAME.getString(), DEFAULT_NAME, server, player);
                                for (String command : entry.commands()) {
                                    server.getCommands().performPrefixedCommand(stack, command);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public void clearKnowledge() {
        knownCategories.clear();
    }
}