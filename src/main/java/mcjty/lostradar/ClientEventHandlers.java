package mcjty.lostradar;

import mcjty.lostradar.data.ClientMapData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class ClientEventHandlers {

    @SubscribeEvent
    public void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientMapData.getData().cleanup();
    }
}
