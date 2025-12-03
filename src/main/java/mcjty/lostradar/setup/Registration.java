package mcjty.lostradar.setup;

import com.mojang.serialization.Codec;
import mcjty.lib.setup.DeferredItems;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.radar.RadarItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static mcjty.lostradar.LostRadar.MODID;
import static mcjty.lostradar.LostRadar.tab;

public class Registration {

    public static final DeferredItems ITEMS = DeferredItems.create(MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<PlayerMapKnowledge>> PLAYER_KNOWLEDGE = ATTACHMENT_TYPES.register("playerknowledge", () -> AttachmentType.builder(() -> PlayerMapKnowledge.DEFAULT)
            .serialize(PlayerMapKnowledge.CODEC)
            .copyOnDeath()
            .build());

    public static Supplier<CreativeModeTab> TAB = TABS.register("lostradar", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(Blocks.STONE_BRICKS))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                LostRadar.setup.populateTab(output);
            })
            .build());

    public static final DeferredItem<RadarItem> RADAR = ITEMS.register("radar", tab(RadarItem::new));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
        ATTACHMENT_TYPES.register(bus);
    }

    @NotNull
    public static Item.Properties createStandardProperties() {
        return LostRadar.setup.defaultProperties();
    }
}
