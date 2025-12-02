package mcjty.lostradar.setup;

import mcjty.lostradar.data.BiomeColorIndex;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

public class Config {

    // Server side
    public static ForgeConfigSpec.IntValue SEARCH_RADIUS;
    public static ForgeConfigSpec.IntValue RADAR_MAXENERGY;
    public static ForgeConfigSpec.IntValue RADAR_RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue RADAR_MINENERGY_FOR_MAP;

    // Client side
    public static ForgeConfigSpec.IntValue HILIGHT_R1;
    public static ForgeConfigSpec.IntValue HILIGHT_G1;
    public static ForgeConfigSpec.IntValue HILIGHT_B1;
    public static ForgeConfigSpec.IntValue HILIGHT_R2;
    public static ForgeConfigSpec.IntValue HILIGHT_G2;
    public static ForgeConfigSpec.IntValue HILIGHT_B2;

    // Two arrays (lists) with optional "u,v" coordinates in icons.png for the six biome types
    // Indexing is by BiomeColorIndex.ordinal()
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_ICONS_ON;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_ICONS_OFF;

    // Array (list) with RGB hex colors per biome type (order: OCEAN,MOUNTAIN,DESERT,FOREST,PLAINS,OTHER)
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_COLORS;

    // Cached parsed UV arrays
    private static UV[] CACHED_ON;
    private static UV[] CACHED_OFF;
    private static String CACHE_KEY_ON = null;
    private static String CACHE_KEY_OFF = null;

    // Cached parsed biome colors (RGB)
    private static int[] CACHED_COLORS;
    private static String CACHE_KEY_COLORS = null;

    public static void register() {
        registerServerConfigs();
        registerClientConfigs();
    }

    private static void registerServerConfigs() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        SEARCH_RADIUS = builder
                .comment("The radius of the search area for the radar. This is measured in multiples of 8 chunks")
                .defineInRange("searchRadius", 10, 1, 10000);
        RADAR_MAXENERGY = builder
                .comment("Maximum RF storage that the radar item can hold")
                .defineInRange("radarMaxRF", 20000, 0, Integer.MAX_VALUE);
        RADAR_RECEIVEPERTICK = builder
                .comment("RF per tick that the the radar item can receive")
                .defineInRange("radarRFPerTick", 100, 0, Integer.MAX_VALUE);
        RADAR_MINENERGY_FOR_MAP = builder
                .comment("Minimum RF that the radar item must have to be able to show the map fully")
                .defineInRange("radarMinRFForMap", 10, 0, Integer.MAX_VALUE);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    private static void registerClientConfigs() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        HILIGHT_R1 = builder
                .comment("The red component of the hilight color one")
                .defineInRange("hilightR1", 255, 0, 255);
        HILIGHT_G1 = builder
                .comment("The green component of the hilight color one")
                .defineInRange("hilightG1", 255, 0, 255);
        HILIGHT_B1 = builder
                .comment("The blue component of the hilight color one")
                .defineInRange("hilightB1", 255, 0, 255);

        HILIGHT_R2 = builder
                .comment("The red component of the hilight color two")
                .defineInRange("hilightR2", 128, 0, 255);
        HILIGHT_G2 = builder
                .comment("The green component of the hilight color two")
                .defineInRange("hilightG2", 128, 0, 255);
        HILIGHT_B2 = builder
                .comment("The blue component of the hilight color two")
                .defineInRange("hilightB2", 128, 0, 255);

        // Biome icon coordinates per category when there is energy
        BIOME_ICONS_ON = builder
                .comment("List of 6 entries with '<u,v>' coordinates in 'icons.png' to use for biome types when there is energy (order: OCEAN,MOUNTAIN,DESERT,FOREST,PLAINS,OTHER). Empty string means no texture.")
                .defineList("biomeIconsOn", Arrays.asList("192,32", "", "", "192,64", "192,0", ""), o -> o instanceof String);

        // Biome icon coordinates per category when there is no energy
        BIOME_ICONS_OFF = builder
                .comment("List of 6 entries with '<u,v>' coordinates in 'icons.png' to use for biome types when there is no energy (order: OCEAN,MOUNTAIN,DESERT,FOREST,PLAINS,OTHER). Empty string means no texture.")
                .defineList("biomeIconsOff", Arrays.asList("224,32", "", "", "224,64", "224,0", ""), o -> o instanceof String);

        // Biome colors per category (RGB hex). Accepts values like '00ff00' or '#00ff00'
        BIOME_COLORS = builder
                .comment("List of 6 RGB hex colors to use for biome types (order: OCEAN,MOUNTAIN,DESERT,FOREST,PLAINS,OTHER). Values can be 'rrggbb' or '#rrggbb'.")
                .defineList("biomeColors", Arrays.asList("0000ff", "8b4513", "ffff00", "006400", "00ff00", "22ee22"), o -> o instanceof String);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder.build());
    }

    public record UV (int u, int v) {}

    private static String getIconSpec(boolean hasEnergy, int idx) {
        List<? extends String> list = hasEnergy ? BIOME_ICONS_ON.get() : BIOME_ICONS_OFF.get();
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (idx < 0 || idx >= list.size()) {
            return "";
        }
        String s = list.get(idx);
        return s == null ? "" : s.trim();
    }

    private static void ensureCache() {
        List<? extends String> on = BIOME_ICONS_ON == null ? null : BIOME_ICONS_ON.get();
        List<? extends String> off = BIOME_ICONS_OFF == null ? null : BIOME_ICONS_OFF.get();
        List<? extends String> colors = BIOME_COLORS == null ? null : BIOME_COLORS.get();
        String keyOn = on == null ? "" : String.join("|", on);
        String keyOff = off == null ? "" : String.join("|", off);
        String keyColors = colors == null ? "" : String.join("|", colors);

        if (CACHED_ON == null || !keyOn.equals(CACHE_KEY_ON)) {
            CACHED_ON = parseToCache(on);
            CACHE_KEY_ON = keyOn;
        }
        if (CACHED_OFF == null || !keyOff.equals(CACHE_KEY_OFF)) {
            CACHED_OFF = parseToCache(off);
            CACHE_KEY_OFF = keyOff;
        }
        if (CACHED_COLORS == null || !keyColors.equals(CACHE_KEY_COLORS)) {
            CACHED_COLORS = parseColorsToCache(colors);
            CACHE_KEY_COLORS = keyColors;
        }
    }

    private static UV[] parseToCache(List<? extends String> list) {
        int size = BiomeColorIndex.values().length;
        UV[] arr = new UV[size];
        for (int i = 0; i < size; i++) {
            int u = -1;
            int v = -1;
            if (list != null && i < list.size()) {
                String s = list.get(i);
                if (s != null) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        String[] split = s.split(",");
                        if (split.length >= 2) {
                            try {
                                u = Integer.parseInt(split[0].trim());
                                v = Integer.parseInt(split[1].trim());
                            } catch (NumberFormatException ignored) {
                                u = -1;
                                v = -1;
                            }
                        }
                    }
                }
            }
            arr[i] = new UV(u, v);
        }
        return arr;
    }

    private static int[] parseColorsToCache(List<? extends String> list) {
        int size = BiomeColorIndex.values().length;
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            int color = BiomeColorIndex.values()[i].getColor();
            if (list != null && i < list.size()) {
                String s = list.get(i);
                if (s != null) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        // Allow optional '#' or '0x'
                        if (s.startsWith("#")) s = s.substring(1);
                        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
                        try {
                            color = Integer.parseInt(s, 16) & 0xFFFFFF;
                        } catch (NumberFormatException ignored) {
                            // keep default
                        }
                    }
                }
            }
            arr[i] = color;
        }
        return arr;
    }

    // Returns cached UV; values -1,-1 mean no icon configured
    public static UV getBiomeIconUV(boolean hasEnergy, int idx) {
        ensureCache();
        UV[] arr = hasEnergy ? CACHED_ON : CACHED_OFF;
        if (arr == null || idx < 0 || idx >= arr.length) {
            return new UV(-1, -1);
        }
        return arr[idx];
    }

    // Return configured biome color (RGB). Falls back to enum default if invalid/missing
    public static int getBiomeColor(int idx) {
        ensureCache();
        if (CACHED_COLORS == null || idx < 0 || idx >= CACHED_COLORS.length) {
            return BiomeColorIndex.OTHER.getColor();
        }
        return CACHED_COLORS[idx];
    }
}
