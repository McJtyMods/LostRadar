package mcjty.lostradar.radar;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class BatchQuadGuiRenderer {

    public record Quad(int color, int x1, int y1, int x2, int y2) {
    }

    private final List<Quad> quads = new ArrayList();

    public void quad(int color, int x1, int y1, int x2, int y2) {
        quads.add(new Quad(color, x1, y1, x2, y2));
    }

    public void render(GuiGraphics graphics) {
        Matrix4f matrix4f = graphics.pose().last().pose();
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        for (Quad quad : quads) {
            float f3 = (float) FastColor.ARGB32.alpha(quad.color) / 255.0F;
            float f = (float) FastColor.ARGB32.red(quad.color) / 255.0F;
            float f1 = (float) FastColor.ARGB32.green(quad.color) / 255.0F;
            float f2 = (float) FastColor.ARGB32.blue(quad.color) / 255.0F;
            vertexconsumer.addVertex(matrix4f, (float) quad.x1, (float) quad.y1, (float) 0).setColor(f, f1, f2, f3);
            vertexconsumer.addVertex(matrix4f, (float) quad.x1, (float) quad.y2, (float) 0).setColor(f, f1, f2, f3);
            vertexconsumer.addVertex(matrix4f, (float) quad.x2, (float) quad.y2, (float) 0).setColor(f, f1, f2, f3);
            vertexconsumer.addVertex(matrix4f, (float) quad.x2, (float) quad.y1, (float) 0).setColor(f, f1, f2, f3);
        }
        graphics.flush();   // @todo if unmanaged
        quads.clear();
    }

    public void drawBeveledBox(int x1, int y1, int x2, int y2, int topleftcolor, int botrightcolor, int fillcolor) {
        if (fillcolor != -1) {
            quad(fillcolor, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
        }
        drawHorizontalLine(x1, y1, x2 - 1, topleftcolor);
        drawVerticalLine(x1, y1, y2 - 1, topleftcolor);
        drawVerticalLine(x2 - 1, y1, y2 - 1, botrightcolor);
        drawHorizontalLine(x1, y2 - 1, x2, botrightcolor);
    }

    public void drawHorizontalLine(int x1, int y1, int x2, int color) {
        quad(color, x1, y1, x2, y1 + 1);
    }

    public void drawVerticalLine(int x1, int y1, int y2, int color) {
        quad(color, x1, y1, x1 + 1, y2);
    }
}
