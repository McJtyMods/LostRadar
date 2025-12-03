package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Enum representing the six biome color categories used on the radar map.
 * The values are indices sent over the network and stored in MapChunk.biomeColors.
 */
public enum BiomeColorIndex implements StringRepresentable {
    OCEAN(0x0000ff),
    MOUNTAIN(0x8b4513),
    DESERT(0xffff00),
    FOREST(0x006400),
    PLAINS(0x00ff00),
    OTHER(0x22ee22); // Default to green if unknown

    public static final Codec<BiomeColorIndex> CODEC = StringRepresentable.fromEnum(BiomeColorIndex::values);

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeColorIndex[]> ARRAY_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BiomeColorIndex[] decode(RegistryFriendlyByteBuf buffer) {
            int cnt = buffer.readVarInt();
            if (cnt <= 0) {
                return new BiomeColorIndex[0];
            } else {
                BiomeColorIndex[] value = new BiomeColorIndex[cnt];
                for (int i = 0; i < cnt; i++) {
                    value[i] = fromOrdinal(buffer.readVarInt());
                }
                return value;
            }
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, BiomeColorIndex[] value) {
            buffer.writeVarInt(value.length);
            for (BiomeColorIndex v : value) {
                buffer.writeVarInt(v.ordinal());
            }
        }
    };

    private final int color;

    BiomeColorIndex(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }


    public static BiomeColorIndex fromOrdinal(int idx) {
        if (idx < 0 || idx >= values().length) {
            return OTHER;
        }
        return values()[idx];
    }

    @Override
    public String getSerializedName() {
        return name();
    }
}
