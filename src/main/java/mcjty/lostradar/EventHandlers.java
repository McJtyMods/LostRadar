package mcjty.lostradar;

import mcjty.lostradar.commands.ModCommands;
import mcjty.lostradar.data.PlayerMapData;
import mcjty.lostradar.data.PlayerMapDataDispatcher;
import mcjty.lostradar.setup.ModSetup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlers {

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        PlayerMapData.register(event);
    }

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.getCommandSenderWorld().isClientSide) {
            PlayerMapDataDispatcher.getPlayerMapData(event.player).ifPresent(handler -> handler.tick((ServerPlayer) event.player));
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player) {
            if (!event.getCapabilities().containsKey(ModSetup.PLAYER_MAP_DATA) && !event.getObject().getCapability(ModSetup.PLAYER_MAP_DATA).isPresent()) {
                event.addCapability(ModSetup.PLAYER_MAP_DATA_KEY, new PlayerMapDataDispatcher());
            } else {
                throw new IllegalStateException(event.getObject().toString());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // We need to copyFrom the capabilities
            event.getOriginal().getCapability(ModSetup.PLAYER_MAP_DATA).ifPresent(oldStore -> {
                event.getEntity().getCapability(ModSetup.PLAYER_MAP_DATA).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }
}
