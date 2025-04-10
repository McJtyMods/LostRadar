package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostradar.compat.LostCitiesCompat;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketReturnMapChunkToClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ServerMapData extends SavedData {

    public static final String NAME = "LostRadarData";

    private final Map<EntryPos, MapChunk> mapChunks = new HashMap<>();

    private static final Codec<Pair<EntryPos, MapChunk>> PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntryPos.CODEC.fieldOf("entryPos").forGetter(Pair::getLeft),
            MapChunk.CODEC.fieldOf("mapChunk").forGetter(Pair::getRight)
    ).apply(instance, Pair::of));

    private static final Codec<Map<EntryPos, MapChunk>> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PAIR_CODEC.listOf().fieldOf("mapChunks").forGetter(m -> m.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .toList())
    ).apply(instance, (chunks) -> {
        Map<EntryPos, MapChunk> map = new HashMap<>();
        for (Pair<EntryPos, MapChunk> entry : chunks) {
            map.put(entry.getLeft(), entry.getRight());
        }
        return map;
    }));

    @Nonnull
    public static ServerMapData getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel)world).getDataStorage();
        return storage.computeIfAbsent(ServerMapData::new, ServerMapData::new, NAME);
    }

    private ServerMapData() {
    }

    private ServerMapData(CompoundTag tag) {
        load(tag);
    }

    public void requestMapChunk(Level level, ChunkPos pos) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        EntryPos entryPos = EntryPos.fromChunkPos(level.dimension(), pos);
        MapChunk mapChunk = mapChunks.get(entryPos);
        if (mapChunk == null) {
            mapChunk = calculateMapChunk(level, pos);
        }
        if (mapChunk != null) {
            // Send the map chunk to all clients
            PacketReturnMapChunkToClient packet = new PacketReturnMapChunkToClient(level.dimension(), mapChunk);
            Messages.sendToAllPlayers(level.dimension(), packet);
        }
    }

    private MapChunk calculateMapChunk(Level level, ChunkPos pos) {
        ILostCityInformation info = LostCitiesCompat.lostCities.getLostInfo(level);
        if (info != null) {
            PaletteCache cache = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(level));
            int defaultEntry = cache.getDefaultEntry();
            short[] data = new short[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int dataAt = -1;
                    ILostChunkInfo chunk = info.getChunkInfo(pos.x + x, pos.z + z);
                    if (chunk != null) {
                        ResourceLocation buildingId = chunk.getBuildingId();
                        if (buildingId != null) {
                            int index = cache.getIndexForBuilding(buildingId);
                            if (index != -1) {
                                dataAt = (short) index;
                            } else {
                                dataAt = (short) defaultEntry;
                            }
                        }
                    }
                    data[x + z * 16] = (short) dataAt;
                }
            }
            MapChunk mapChunk = new MapChunk(pos.x, pos.z, data);
            mapChunks.put(EntryPos.fromChunkPos(level.dimension(), pos), mapChunk);
            setDirty();
            return mapChunk;
        }
        return null;
    }

    private void load(CompoundTag nbt) {
        MAP_CODEC.decode(NbtOps.INSTANCE, nbt.get("chunks"))
                .resultOrPartial(error -> {
                    throw new IllegalStateException("Failed to decode map chunks: " + error);
                })
                .ifPresent(chunks -> this.mapChunks.putAll(chunks.getFirst()));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        Tag mapTag = MAP_CODEC.encodeStart(NbtOps.INSTANCE, mapChunks).result().orElseThrow();
        nbt.put("chunks", mapTag);
        return nbt;
    }
}
