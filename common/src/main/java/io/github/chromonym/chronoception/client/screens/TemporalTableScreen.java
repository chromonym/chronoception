package io.github.chromonym.chronoception.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.screenhandlers.TemporalTableScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TemporalTableScreen extends HandledScreen<TemporalTableScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(Chronoception.MOD_ID, "textures/gui/container/temporal_table.png");
    TemporalTableScreenHandler screenHandler;

    public TemporalTableScreen(TemporalTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = handler;
        this.backgroundHeight = 139;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawIcons(context, x, y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //context.drawText(textRenderer, Integer.toString(screenHandler.getProgress()), 0, 0, 65280, true);
        //context.drawText(textRenderer, Integer.toString(screenHandler.getLocalTime()), 0, 10, 65280, true);
        //context.drawText(textRenderer, Integer.toString(screenHandler.getLunarTime()), 0, 20, 65280, true);
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    protected void drawIcons(DrawContext context, int x, int y) {
        int timeX = 176; // y = 0
        int moonX = 176; // y = 10
        int barX = 176; // y = 20
        long local = (long)screenHandler.getLocalTime();
        int lunar = screenHandler.getLunarTime();
        if (screenHandler.getSlot(0).getStack().getItem() == Chronoception.TEMPORAL_DUST.get()) {
            if (Chronoception.FULL_MOON.test(local, lunar)) {barX += 21;}
            else if (Chronoception.GIBBOUS_MOON.test(local, lunar)) {barX += 28;}
            else if (Chronoception.QUARTER_MOON.test(local, lunar)) {barX += 35;}
            else if (Chronoception.CRESCENT_MOON.test(local, lunar)) {barX += 42;}
            else if (Chronoception.NEW_MOON.test(local, lunar)) {barX += 49;}
        } else { // TODO somehow make the block know if it's doing a lunar recipe or not. atm it just assume not unless the item is temporal dust
            if (Chronoception.CREPUSCULAR.test(local, lunar)) {barX += 7;}
            else if (Chronoception.NOCTURNAL.test(local, lunar)) {barX += 14;}
        }
        if (Chronoception.CREPUSCULAR.test(local, lunar)) {timeX += 10;}
        else if (Chronoception.NOCTURNAL.test(local, lunar)) {timeX += 20;}
        if (Chronoception.GIBBOUS_MOON.test(local, lunar)) {moonX += 10;}
        else if (Chronoception.QUARTER_MOON.test(local, lunar)) {moonX += 20;}
        else if (Chronoception.CRESCENT_MOON.test(local, lunar)) {moonX += 30;}
        else if (Chronoception.NEW_MOON.test(local, lunar)) {moonX += 40;}
        int barHeight;
        if (screenHandler.getSlot(0).getStack().getCount() > 0) {
            barHeight = ((screenHandler.getProgress()*24) / (screenHandler.getSlot(0).getStack().getCount()*20));
        } else {
            barHeight = 0;
        }
        context.drawTexture(TEXTURE, x+62, y+17, timeX, 0, 10, 10);
        context.drawTexture(TEXTURE, x+62, y+31, moonX, 10, 10, 10);
        if (barHeight > 0) {
            context.drawTexture(TEXTURE, x+104, y+17+24-Math.min(barHeight,24), barX, 20 + 24 - Math.min(barHeight,24), 7, barHeight);
        }
    }

    @Override
    protected void init() {
        super.init();
    }
    
}
