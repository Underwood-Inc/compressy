package com.compressy.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Simple config screen when Cloth Config is not available.
 * Just displays info about editing the config file manually.
 */
public class SimpleConfigScreen extends Screen {
    
    private final Screen parent;
    
    public SimpleConfigScreen(Screen parent) {
        super(Text.literal("Compressy Config"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        // Done button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
        
        // Open config folder button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Config Folder"), button -> {
            try {
                var configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile();
                java.awt.Desktop.getDesktop().open(configDir);
            } catch (Exception e) {
                // Ignore
            }
        }).dimensions(this.width / 2 - 100, this.height / 2 + 30, 200, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // Info text
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Edit config/compressy.json to configure"), 
            this.width / 2, this.height / 2 - 30, 0xAAAAAA);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Install Cloth Config for in-game settings"), 
            this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Options: showRomanNumerals, excludedBlocks"), 
            this.width / 2, this.height / 2 + 10, 0x55FF55);
    }
}

