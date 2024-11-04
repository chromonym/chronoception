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
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(textRenderer, Integer.toString(screenHandler.getProgress()), 0, 0, 65280, true);
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
    }
    
}
