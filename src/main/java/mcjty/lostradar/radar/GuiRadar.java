package mcjty.lostradar.radar;

import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lostradar.data.*;
import mcjty.lostradar.network.PacketRequestMapChunk;
import mcjty.lostradar.network.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_VERTICAL_MARGIN;
import static mcjty.lib.gui.widgets.Widgets.vertical;

public class GuiRadar extends GuiItemScreen {

    private static final int xSize = 340;
    private static final int ySize = 220;

    public GuiRadar() {
        super(xSize, ySize, ManualEntry.EMPTY);
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        Panel toplevel = vertical(DEFAULT_VERTICAL_MARGIN, 0).filledRectThickness(2);

        // setup
        toplevel.bounds(k, l, xSize, ySize);

        window = new Window(this, toplevel);
    }

    private void renderMap(GuiGraphics graphics) {
//        PlayerMapKnowledgeDispatcher.getPlayerMapData(Minecraft.getInstance().player).ifPresent(data -> {
//        });
        ClientMapData data = ClientMapData.getData();
        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        // For an area of 21x21 chunks around the player we render the color
        int size = 10;
        int dim = 10;
        for (int x = -dim; x <= dim; x++) {
            for (int z = -dim; z <= dim; z++) {
                ChunkPos pos = new ChunkPos(p.x + x, p.z + z);
                int biomeColor = data.getBiomeColor(Minecraft.getInstance().level, pos);
                if (biomeColor != -1) {
                    // Render the biome color
                    RenderHelper.drawBeveledBox(graphics, this.guiLeft + (x+dim) * size, this.guiTop + (z+dim) * size, this.guiLeft + (x + dim + 1) * size, this.guiTop + (z + dim + 1) * size, 0xff000000 + biomeColor, 0xff000000 + biomeColor, 0xff000000 + biomeColor);
                }
                MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
                if (entry != null) {
                    // Render the color
                    int color = entry.color();
                    if (entry == MapPalette.CITY) {
                        RenderHelper.drawBeveledBox(graphics, this.guiLeft + (x+dim) * size, this.guiTop + (z+dim) * size, this.guiLeft + (x + dim + 1) * size, this.guiTop + (z + dim + 1) * size, 0xff000000 + color, 0xff000000 + color, 0xff000000 + color);
                    } else {
                        RenderHelper.drawBeveledBox(graphics, this.guiLeft + (x + dim) * size, this.guiTop + (z + dim) * size, this.guiLeft + (x + dim + 1) * size, this.guiTop + (z + dim + 1) * size, 0xff333333, 0xff333333, 0xff000000 + color);
                    }
                }
            }
        }
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int xSize_lo, int ySize_lo, float par3) {
        drawWindow(graphics);
        renderMap(graphics);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }

}
