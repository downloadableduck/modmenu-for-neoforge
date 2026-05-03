package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class ModBadgeRenderer {
   protected int startX;
   protected int startY;
   protected int badgeX;
   protected int badgeY;
   protected int badgeMax;
   protected Mod mod;
   protected Minecraft client;
   protected final ModsScreen screen;

   public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
      this.startX = startX;
      this.startY = startY;
      this.badgeMax = endX;
      this.mod = mod;
      this.screen = screen;
      this.client = Minecraft.getInstance();
   }

   public void draw(GuiGraphicsExtractor guiGraphics) {
      this.badgeX = this.startX;
      this.badgeY = this.startY;
      Set<ModBadge> badges = this.mod.getBadges();
      badges.forEach((badge) -> {
         if (!((List)ModMenu.getConfig().HIDE_BADGE.get()).contains(badge.getId())) {
            this.drawBadge(guiGraphics, badge);
         }

      });
   }

   public void drawBadge(GuiGraphicsExtractor guiGraphics, ModBadge badge) {
      this.drawBadge(guiGraphics, badge.getComponent().getVisualOrderText(), badge.getOutlineColor(), badge.getFillColor(), badge.getTextColor());
   }

   public void drawBadge(GuiGraphicsExtractor guiGraphics, FormattedCharSequence charSequence, int outlineColor, int fillColor, int textColor) {
      int width = this.client.font.width(charSequence) + 6;
      if (this.badgeX + width < this.badgeMax) {
         DrawingUtil.drawBadge(guiGraphics, this.badgeX, this.badgeY, width, charSequence, outlineColor, fillColor, textColor);
         this.badgeX += width + 3;
      }

   }

   public Mod getMod() {
      return this.mod;
   }
}
