package mcjty.lostradar.data;

import mcjty.lostradar.LostRadar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CustomRegistries {

    public static final ResourceKey<Registry<MapPalette>> PALETTE_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "radar"));
    public static final DeferredRegister<MapPalette> PALETTE_DEFERRED_REGISTER = DeferredRegister.create(PALETTE_REGISTRY_KEY, LostRadar.MODID);

    public static void init(IEventBus bus) {
        PALETTE_DEFERRED_REGISTER.register(bus);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PALETTE_REGISTRY_KEY, MapPalette.CODEC, MapPalette.CODEC);
    }
}
