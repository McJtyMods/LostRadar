package mcjty.lostradar.radar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import mcjty.lib.client.GuiTools;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.*;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.*;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketStartSearch;
import mcjty.lostradar.setup.Config;
import mcjty.lostradar.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static mcjty.lib.gui.widgets.Widgets.positional;

public class GuiRadar extends GuiItemScreen implements IKeyReceiver {

    private static final int xSize = 380;
    private static final int ySize = 236;

    private static final int MAPCELL_SIZE = 10;
    private static final int MAP_DIM = 10;

    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(LostRadar.MODID, "textures/gui/icons.png");
    private static final ResourceLocation MAP_ICONS_LOCATION = ResourceLocation.withDefaultNamespace("textures/atlas/map_decorations.png");

    private WidgetList categoryList;
    private Button scanButton;
    private final List<String> categories = new ArrayList<>();

    private final List<Pair<Rect2i, ChunkPos>> borderCoordinates = new ArrayList<>();

    public GuiRadar() {
        super(xSize, ySize, ManualEntry.EMPTY);
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        Panel toplevel = positional().filledRectThickness(2);
        categoryList = Widgets.list(238, 12, 133, ySize - 53);
        scanButton = Widgets.button(238, ySize - 40, 133, 15, ComponentFactory.translatable("button.lostradar.scan").getString())
                .event(() -> {
                    int selected = categoryList.getSelected();
                    if (selected >= 0) {
                        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
                        String category = categories.get(selected);
                        MapPalette.PaletteEntry entry = palette.getEntryByCategory(category);
                        if (entry.usage() > 0) {
                            int extracted = Registration.RADAR.get().extractEnergyNoMax(Minecraft.getInstance().player.getMainHandItem(), entry.usage(), true);
                            if (extracted < entry.usage()) {
                                Minecraft.getInstance().player.sendSystemMessage(ComponentFactory.translatable("lostradar.notenoughenergy", entry.usage()));
                                return;
                            }
                        }
                        if (!category.isEmpty()) {
                            Messages.sendToServer(new PacketStartSearch(category, entry.usage()));
                        }
                        ClientMapData.getData().setSearchString(category);
                    }
                });
        Button clearButton = Widgets.button(238, ySize - 22, 133, 15, ComponentFactory.translatable("button.lostradar.clear").getString()).event(() -> {
            ClientMapData.getData().clearSearchResults();
            ClientMapData.getData().setSearchString("");
            Messages.sendToServer(new PacketStartSearch("", 0));
            categoryList.selected(-1);
        });
        toplevel.children(categoryList, scanButton, clearButton);
        toplevel.bounds(k, l, xSize, ySize);
        populateCategoryList();

        window = new Window(this, toplevel);
    }

    private void renderMap(GuiGraphics graphics) {
        BatchQuadGuiRenderer batch = new BatchQuadGuiRenderer();

        ClientMapData data = ClientMapData.getData();
        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        // For an area of 21x21 chunks around the player we render the color
        int borderLeft = this.guiLeft + 12;
        int borderTop = this.guiTop + 12;
        // Make a copy of searchResults so that we can modify it
        Set<ChunkPos> searchResults = new HashSet<>(data.getSearchResults());
        List<Icon> icons = renderCityGrid(batch, searchResults, p, data, borderLeft, borderTop);

        // Now render the remaining search results but at the border (just beyond the map)
        renderDistantSearchResults(batch, searchResults, p, borderLeft, borderTop);

        batch.render(graphics);

        // Get the angle from the player's rotation
        float angle = Minecraft.getInstance().player.getYRot() + 180;
        // Render the player as a white smaller dot
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int startX = borderLeft + (MAP_DIM) * MAPCELL_SIZE;
        int startZ = borderTop + (MAP_DIM) * MAPCELL_SIZE;
        MapDecoration playerDecoration = new MapDecoration(MapDecorationTypes.PLAYER, (byte)0, (byte)0, (byte)0, Optional.empty());
        TextureAtlasSprite playerSprite = Minecraft.getInstance().getMapDecorationTextures().get(playerDecoration);
        drawRotatedIcon(graphics, startX + 2, startZ + 2, 16, angle, playerSprite, 0, 0, 16, 16);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (Icon icon : icons) {
            graphics.blit(ICONS, icon.x(), icon.y(), icon.w(), icon.h(), icon.u(), icon.v(), icon.pw(), icon.ph(), 256, 256);
        }
    }

    private static void drawRotatedIcon(GuiGraphics graphics, int centerX, int centerY, int size, double angle, TextureAtlasSprite texture, int u, int v, int w, int h) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) angle));
        poseStack.translate(-centerX, -centerY, 0);
        RenderSystem.setShaderTexture(0, MAP_ICONS_LOCATION);
        drawTexturedModalRect(poseStack, texture, centerX - size / 2, centerY - size / 2, u, v, w, h);
        poseStack.popPose();
    }

    private static void drawTexturedModalRect(PoseStack poseStack, TextureAtlasSprite texture, int x, int y, int u, int v, int width, int height) {
        Matrix4f matrix = poseStack.last().pose();
        float zLevel = 0.01f;
        float f = (1 / 256.0f);
        float f1 = (1 / 256.0f);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, (x + 0), (y + height), zLevel).setUv(((u + 0) * f), ((v + height) * f1));
        buffer.addVertex(matrix, (x + width), (y + height), zLevel).setUv(((u + width) * f), ((v + height) * f1));
        buffer.addVertex(matrix, (x + width), (y + 0), zLevel).setUv(((u + width) * f), ((v + 0) * f1));
        buffer.addVertex(matrix, (x + 0), (y + 0), zLevel).setUv(((u + 0) * f), ((v + 0) * f1));
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private record Icon(int x, int y, int w, int h, int u, int v, int pw, int ph) {
    }

    private List<Icon> renderCityGrid(BatchQuadGuiRenderer batch, Set<ChunkPos> searchResults, ChunkPos p, ClientMapData data, int borderLeft, int borderTop) {
        Set<EntryPos> searchedChunks = data.getSearchedChunks();
        // Based on the current time calculate an rgb color between Config.HIGHLIGHT_x1 and Config.HIGHLIGHT_x2
        // Fluctuate back and forth between the two colors in a 2 second cycle
        float time = System.currentTimeMillis() % 4000 / 2000f;
        if (time > 1) {
            time = 2 - time;
        }
        int r = (int) (Config.HILIGHT_R1.get() + (Config.HILIGHT_R2.get() - Config.HILIGHT_R1.get()) * time);
        int g = (int) (Config.HILIGHT_G1.get() + (Config.HILIGHT_G2.get() - Config.HILIGHT_G1.get()) * time);
        int b = (int) (Config.HILIGHT_B1.get() + (Config.HILIGHT_B2.get() - Config.HILIGHT_B1.get()) * time);
        int highlightColor = 0xff000000 | (r << 16) | (g << 8) | b;

        int energyStored = Registration.RADAR.get().getEnergyStored(Minecraft.getInstance().player.getMainHandItem());
        boolean hasEnergy = energyStored >= Config.RADAR_MINENERGY_FOR_MAP.get();

        List<Icon> icons = new ArrayList<>();
        for (int x = -MAP_DIM; x <= MAP_DIM; x++) {
            for (int z = -MAP_DIM; z <= MAP_DIM; z++) {
                ChunkPos pos = new ChunkPos(p.x + x, p.z + z);
                int biomeIdx = data.getBiomeColor(Minecraft.getInstance().level, pos);
                if (biomeIdx != -1) {
                    // Translate biome index to actual color (configurable)
                    int biomeColor = Config.getBiomeColor(biomeIdx);
                    if (!hasEnergy) {
                        biomeColor = 0x777777; // gray when no energy
                    }
                    batch.drawBeveledBox(borderLeft + (x+ MAP_DIM) * MAPCELL_SIZE, borderTop + (z+ MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, 0xff000000 + biomeColor, 0xff000000 + biomeColor, 0xff000000 + biomeColor);
                }
                int startX = borderLeft + (x + MAP_DIM) * MAPCELL_SIZE;
                int startZ = borderTop + (z + MAP_DIM) * MAPCELL_SIZE;

                MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
                if (entry != null) {
                    // Render the color
                    int color = entry.color();
                    if (!hasEnergy) {
                        color = 0x444444;
                    }
                    int borderColor = 0xff333333;
                    if (searchResults.contains(pos)) {
                        // This is a search result
                        borderColor = highlightColor;
                        searchResults.remove(pos);
                    }

                    batch.drawBeveledBox(borderLeft + (x + MAP_DIM) * MAPCELL_SIZE, borderTop + (z + MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, borderColor, borderColor, 0xff000000 + color);
                    if (entry.iconU() >= 0) {
                        // We have an icon
                        icons.add(new Icon(startX, startZ, MAPCELL_SIZE, MAPCELL_SIZE, entry.iconU(), entry.iconV(), 32, 32));
                    }
                } else {
                    // Optional biome icon overlay from config
                    if (biomeIdx != -1) {
                        Config.UV uv = Config.getBiomeIconUV(hasEnergy, biomeIdx);
                        if (uv.u() >= 0 && uv.v() >= 0) {
                            icons.add(new Icon(startX, startZ, MAPCELL_SIZE, MAPCELL_SIZE, uv.u(), uv.v(), 32, 32));
                        }
                    }
                }

                // If we want to show searched areas we render a darker overlay on top of the map parts that we didn't search
                if (!searchedChunks.isEmpty() && !searchedChunks.contains(EntryPos.fromChunkPos(Minecraft.getInstance().level.dimension(), pos))) {
                    // Render a darker overlay
                    batch.drawBeveledBox(borderLeft + (x + MAP_DIM) * MAPCELL_SIZE, borderTop + (z + MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, 0x80000000, 0x80000000, 0x80000000);
                }
            }
        }
        return icons;
    }

    private void renderDistantSearchResults(BatchQuadGuiRenderer batch, Set<ChunkPos> searchResults, ChunkPos p, int borderLeft, int borderTop) {
        borderCoordinates.clear();
        for (ChunkPos pos : searchResults) {
            int dx = pos.x - p.x;
            int dz = pos.z - p.z;

            if (dx == 0 && dz == 0) continue; // Skip center

            int adx = Math.abs(dx);
            int adz = Math.abs(dz);
            int bx, bz;

            if (adx * MAP_DIM >= adz * MAP_DIM) {
                bx = Integer.signum(dx) * (MAP_DIM + 1);
                bz = (int) Math.round((double) dz / adx * (MAP_DIM + 1));
            } else {
                bz = Integer.signum(dz) * (MAP_DIM + 1);
                bx = (int) Math.round((double) dx / adz * (MAP_DIM + 1));
            }

            // Color based on distance to center:
            // - White when the search result is right at the center of the map
            // - Black when the search result is 80 chunks away
            // - Gray when the search result is in between
            int distance = Math.max(Math.abs(dx), Math.abs(dz));
            int minDistance = MAP_DIM + 1;
            int maxDistance = 80;
            int clamped = Math.max(0, Math.min(255, (int)(255 * (1 - (double)(distance - minDistance) / (maxDistance - minDistance)))));
            int color = 0xff000000 | (clamped << 16) | (clamped << 8) | clamped;
            int x1 = borderLeft + (bx + MAP_DIM) * MAPCELL_SIZE + 3;
            int y1 = borderTop + (bz + MAP_DIM) * MAPCELL_SIZE + 3;
            batch.drawBeveledBox(x1, y1, borderLeft + (bx + MAP_DIM + 1) * MAPCELL_SIZE - 3, borderTop + (bz + MAP_DIM + 1) * MAPCELL_SIZE - 3, color, color, color);
            // Store the coordinates for later use
            borderCoordinates.add(Pair.of(new Rect2i(x1, y1, MAPCELL_SIZE - 6, MAPCELL_SIZE - 6), pos));
        }
    }

    public static void refresh() {
        if (Minecraft.getInstance().screen instanceof GuiRadar radar) {
            radar.populateCategoryList();
        }
    }

    private void populateCategoryList() {
        ClientMapData data = ClientMapData.getData();
        String searchString = data.getSearchString();
        AtomicInteger selected = new AtomicInteger(categoryList.getSelected());
        categoryList.removeChildren();
        categories.clear();
        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
        PlayerMapKnowledge knowledge = Minecraft.getInstance().player.getData(Registration.PLAYER_KNOWLEDGE);
        for (MapPalette.PaletteEntry category : palette.getPalette().palette()) {
            if (knowledge.knownCategories().contains(category.name())) {
                categoryList.children(makeLine(category));
                categories.add(category.name());
                if (!searchString.isEmpty() && category.name().equals(searchString)) {
                    selected.set(categoryList.getChildren().size() - 1);
                }
            }
        }
        categoryList.selected(selected.get());
    }

    private Widget<Label> makeLine(MapPalette.PaletteEntry category) {
        return Widgets.label(ComponentFactory.translatable(category.translatableKey()).getString());
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ClientMapData data = ClientMapData.getData();
        boolean scanEnabled = categoryList.getSelected() >= 0;
        scanButton.enabled(scanEnabled);
        int progress = data.getSearchProgress();
        if (progress >= 100) {
            scanButton.text(ComponentFactory.translatable("button.lostradar.scan").getString());
        } else if (data.isPaused()) {
            scanButton.text(ComponentFactory.translatable("lostradar.paused_scan", progress).getString());
        } else {
            scanButton.text(progress + "%");
        }
        drawWindow(graphics, mouseX, mouseY, partialTicks);
        renderMap(graphics);
        renderTooltip(graphics, mouseX, mouseY);

        int energyStored = Registration.RADAR.get().getEnergyStored(Minecraft.getInstance().player.getMainHandItem());
        boolean hasEnergy = energyStored >= Config.RADAR_MINENERGY_FOR_MAP.get();
        if (!hasEnergy) {
            graphics.drawString(Minecraft.getInstance().font, ComponentFactory.translatable("lostradar.energylow"), this.guiLeft + 12, this.guiTop + 12, 0xffff0000);
        }
    }

    private void renderTooltip(@Nonnull GuiGraphics graphics, int xxmouseX, int yymouseY) {
        int mouseX = GuiTools.getRelativeX(this);
        int mouseY = GuiTools.getRelativeY(this);

        int borderLeft = this.guiLeft + 12;
        int borderTop = this.guiTop + 12;

        ClientMapData data = ClientMapData.getData();

        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        int tooltipX = mouseX - 20;
        int tooltipY = mouseY - 3;
        if (tooltipY < 14) {
            tooltipY = mouseY + 20;
        }

        // Check that the mouse position is on the map
        if (mouseX < borderLeft || mouseX > borderLeft + MAPCELL_SIZE * (MAP_DIM * 2 + 1) || mouseY < borderTop || mouseY > borderTop + MAPCELL_SIZE * (MAP_DIM * 2 + 1)) {
            // Mouse is outside the map area. Check if it is on the border
            for (Pair<Rect2i, ChunkPos> pair : borderCoordinates) {
                Rect2i rect = pair.getKey();
                ChunkPos pos = pair.getValue();
                // Check if mouseX and mouseY are within the rectangle
                if (rect.contains(mouseX, mouseY)) {
                    String posString = String.format("%d, %d", pos.getMiddleBlockX(), pos.getMiddleBlockZ());
                    String distanceString = String.format("%d", Math.max(Math.abs(pos.x - p.x), Math.abs(pos.z - p.z)) * 16);
                    List<Component> components = List.of(ComponentFactory.translatable("lostradar.chunk.pos", posString),
                            ComponentFactory.translatable("lostradar.chunk.dist", distanceString));
                    graphics.renderTooltip(Minecraft.getInstance().font, components, Optional.empty(),
                            tooltipX, tooltipY);
                    break;
                }
            }
        } else {
            // Find the palette entry at the mouse position (x, y)
            ChunkPos pos = new ChunkPos(
                    p.x + (mouseX - borderLeft) / MAPCELL_SIZE - MAP_DIM,
                    p.z + (mouseY - borderTop) / MAPCELL_SIZE - MAP_DIM);
            MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
            if (entry != null) {
                graphics.renderTooltip(Minecraft.getInstance().font, ComponentFactory.translatable(entry.translatableKey()), tooltipX, tooltipY);
            }
        }
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (window != null) {
            if (window.keyTyped(keyCode, scanCode)) {
                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public void charTypedFromEvent(char codePoint) {
        if (window != null) {
            if (window.charTyped(codePoint)) {
                super.charTyped(codePoint, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public boolean mouseClickedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleasedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolledFromEvent(double x, double y, double dx, double dy) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseScrolled(x, y, dx, dy);
        return true;
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }
}
