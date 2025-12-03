package mcjty.lostradar.radar;

import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.IEnergyItem;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketKnowledgeToPlayer;
import mcjty.lostradar.setup.Config;
import mcjty.lostradar.setup.Registration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class RadarItem extends Item implements IEnergyItem {

    public RadarItem() {
        super(LostRadar.setup.defaultProperties().stacksTo(1).durability(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (world.isClientSide) {
            GuiRadar.open();
        } else {
            // Send knowledge data to the client
            PlayerMapKnowledge data = player.getData(Registration.PLAYER_KNOWLEDGE);
            Messages.sendToPlayer(new PacketKnowledgeToPlayer(data.knownCategories()), player);
        }
        return super.use(world, player, hand);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = getMaxEnergyStored(stack);
        return Math.round((float)getEnergyStored(stack) * 13.0F / (float)max);
    }

    // @todo 1.21
//    @Override
//    public boolean canBeDepleted() {
//        return true;
//    }

    @Override
    public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
        ItemEnergy data = container.get(mcjty.lib.setup.Registration.ITEM_ENERGY);
        if (data == null) {
            data = new ItemEnergy(0);
        }
        long energy = data.energy();
        long energyReceived = Math.min(Config.RADAR_MAXENERGY.get() - energy, Math.min(Config.RADAR_RECEIVEPERTICK.get(), EnergyTools.unsignedClampToInt(maxReceive)));

        if (!simulate) {
            energy += energyReceived;
            container.set(mcjty.lib.setup.Registration.ITEM_ENERGY, new ItemEnergy(energy));
        }
        return energyReceived;
    }

    @Override
    public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
        ItemEnergy data = container.get(mcjty.lib.setup.Registration.ITEM_ENERGY);
        if (data == null) {
            data = new ItemEnergy(0);
        }
        long energy = data.energy();
        int energyExtracted = 0;

        if (!simulate) {
            energy -= energyExtracted;
            container.set(mcjty.lib.setup.Registration.ITEM_ENERGY, new ItemEnergy(energy));
        }
        return energyExtracted;
    }

    @Override
    public long getEnergyStoredL(ItemStack container) {
        ItemEnergy data = container.get(mcjty.lib.setup.Registration.ITEM_ENERGY);
        if  (data == null) {
            return 0;
        }
        return data.energy();
    }

    @Override
    public long getMaxEnergyStoredL(ItemStack container) {
        return Config.RADAR_MAXENERGY.get();
    }

    public int extractEnergyNoMax(ItemStack container, int maxExtract, boolean simulate) {
        ItemEnergy data = container.get(mcjty.lib.setup.Registration.ITEM_ENERGY);
        if (data == null) {
            data = new ItemEnergy(0);
        }
        long energy = data.energy();
        long energyExtracted = Math.min(energy, maxExtract);

        if (!simulate) {
            energy -= energyExtracted;
            container.set(mcjty.lib.setup.Registration.ITEM_ENERGY, new ItemEnergy(energy));
        }
        return (int) energyExtracted;
    }

}
