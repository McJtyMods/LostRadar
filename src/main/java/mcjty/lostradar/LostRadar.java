package mcjty.lostradar;

import mcjty.lostradar.data.CustomRegistries;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.setup.Config;
import mcjty.lostradar.setup.ModSetup;
import mcjty.lostradar.setup.Registration;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(LostRadar.MODID)
public class LostRadar {
    public static final String MODID = "lostradar";

    public static Logger logger = LogManager.getLogger(LostRadar.MODID);
    public static ModSetup setup = new ModSetup();

    public static LostRadar instance;

    public LostRadar(ModContainer mod, IEventBus bus, Dist dist) {
        instance = this;

        Config.register(mod);
        CustomRegistries.init(bus);

        Registration.register(bus);
        bus.addListener(setup::init);
        bus.addListener(CustomRegistries::onDataPackRegistry);
        bus.addListener(Messages::registerMessages);
        if (dist.isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandlers());
        }
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }
}
