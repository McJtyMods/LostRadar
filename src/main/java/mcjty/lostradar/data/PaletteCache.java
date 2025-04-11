package mcjty.lostradar.data;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaletteCache {
    private final MapPalette palette;
    private final Map<ResourceLocation, MapPalette.PaletteEntry> entryForBuilding;
    private final Map<ResourceLocation, Integer> indexForBuilding;
    private int defaultEntry = -1;

    private static PaletteCache paletteCache = null;

    public static PaletteCache getOrCreatePaletteCache(MapPalette palette) {
        if (paletteCache == null) {
            paletteCache = new PaletteCache(palette);
        }
        return paletteCache;
    }

    public static void cleanup() {
        paletteCache = null;
    }

    public PaletteCache(MapPalette palette) {
        this.palette = palette;
        this.entryForBuilding = palette.palette().stream()
                .flatMap(entry -> entry.buildings().stream().map(building -> Map.entry(building, entry)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.indexForBuilding = new HashMap<>();
        for (int i = 0; i < palette.palette().size(); i++) {
            MapPalette.PaletteEntry entry = palette.palette().get(i);
            if (entry.buildings().isEmpty()) {
                defaultEntry = i;
            } else {
                for (ResourceLocation building : entry.buildings()) {
                    indexForBuilding.put(building, i);
                }
            }
        }
    }

    public int getDefaultEntry() {
        return defaultEntry;
    }

    public MapPalette getPalette() {
        return palette;
    }

    @Nullable
    public MapPalette.PaletteEntry getEntryForBuilding(ResourceLocation building) {
        return entryForBuilding.get(building);
    }

    public int getIndexForBuilding(ResourceLocation building) {
        return indexForBuilding.getOrDefault(building, -1);
    }
}
