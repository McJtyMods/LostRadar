package mcjty.lostradar.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class CommandList implements Command<CommandSourceStack> {

    private static final CommandList CMD = new CommandList();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermission(1))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos position = player.blockPosition();
//        LostChunkData soulData = LostSoulData.getSoulData(player.level(), position.getX() >> 4, position.getZ() >> 4, null);
//        System.out.println("  isHaunted() = " + soulData.isHaunted());
//        System.out.println("  getMaxMobs() = " + soulData.getTotalMobs());
//        System.out.println("  getNumberKilled() = " + soulData.getNumberKilled());
//        MobSettings settings = soulData.getSettings();
//        if (settings != null) {
//            System.out.println("  settings.getHauntedChance() = " + settings.getHauntedChance());
//            System.out.println("  settings.getMobAmounts() = " + settings.getMobAmounts());
//        }
        return 0;
    }
}
