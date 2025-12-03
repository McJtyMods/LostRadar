package mcjty.lostradar;

import mcjty.lostradar.commands.ModCommands;
import mcjty.lostradar.data.PaletteCache;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.data.ServerMapData;
import mcjty.lostradar.setup.Registration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class EventHandlers {

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private int tickCounter = 10;
    @SubscribeEvent
    public void onPlayerTickEvent(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            tickCounter--;
            if (tickCounter > 0) {
                return;
            }
            PlayerMapKnowledge data = player.getData(Registration.PLAYER_KNOWLEDGE);
            data.tick(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PaletteCache.cleanup();
        ServerMapData.getData(event.getServer().overworld()).cleanup();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PaletteCache.cleanup();
        ServerMapData.getData(event.getEntity().level()).cleanup();
    }

    @SubscribeEvent
    public void onLevelTick(ServerTickEvent.Pre event) {
        ServerLevel overworld = event.getServer().overworld();
        ServerMapData.getData(overworld).tickSearch(overworld);
    }
}
