package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class UpdateAvailableBadge {
   private static final Identifier UPDATE_ICON = Identifier.parse("minecraft:icon/trial_available");

   public static void renderBadge(GuiGraphicsExtractor guiGraphics, int x, int y) {
      boolean animOffset = false;
      if ((Util.getMillis() / 800L & 1L) == 1L) {
         animOffset = true;
      }

      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, UPDATE_ICON, x, y, 0.0F, 0.0F, 8, 8, 8, 8);
   }
}
