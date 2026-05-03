package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class LegacyTexturedButtonWidget extends ImageButton {
   protected final int u;
   protected final int v;
   protected final int hoveredVOffset;
   protected final Identifier texture;
   protected final int textureWidth;
   protected final int textureHeight;

   public LegacyTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, OnPress pressAction, Component message) {
      super(x, y, width, height, (WidgetSprites)null, pressAction, message);
      this.u = u;
      this.v = v;
      this.hoveredVOffset = hoveredVOffset;
      this.texture = texture;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
   }

   public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
      int v = this.v;
      if (!this.isActive()) {
         v += this.hoveredVOffset * 2;
      } else if (this.isHoveredOrFocused()) {
         v += this.hoveredVOffset;
      }

      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), (float)this.u, (float)v, this.width, this.height, this.textureWidth, this.textureHeight);
   }

   public static com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget.Builder legacyTexturedBuilder(Component message, OnPress onPress) {
      return new Builder(message, onPress);
   }
    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width;
        private int height;
        private int u;
        private int v;
        private int hoveredVOffset;
        private Identifier texture;
        private int textureWidth;
        private int textureHeight;

        public Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder uv(int u, int v, int hoveredVOffset) {
            this.u = u;
            this.v = v;
            this.hoveredVOffset = hoveredVOffset;
            return this;
        }

        public Builder texture(Identifier texture, int textureWidth, int textureHeight) {
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            return this;
        }

        public LegacyTexturedButtonWidget build() {
            return new LegacyTexturedButtonWidget(this.x, this.y, this.width, this.height, this.u, this.v, this.hoveredVOffset, this.texture, this.textureWidth, this.textureHeight, this.onPress, this.message);
        }
    }

}
