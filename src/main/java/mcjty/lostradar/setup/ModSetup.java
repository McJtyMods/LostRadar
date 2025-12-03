package mcjty.lostradar.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.lostradar.EventHandlers;
import mcjty.lostradar.compat.LostCitiesCompat;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import static mcjty.lostradar.LostRadar.MODID;

public class ModSetup extends DefaultModSetup {

    public static final ResourceLocation PLAYER_KNOWLEDGE_KEY = ResourceLocation.fromNamespaceAndPath(MODID, "playermapknowledge");


    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        NeoForge.EVENT_BUS.register(new EventHandlers());
    }

    @Override
    protected void setupModCompat() {
        LostCitiesCompat.setupLostCities();
    }
}
