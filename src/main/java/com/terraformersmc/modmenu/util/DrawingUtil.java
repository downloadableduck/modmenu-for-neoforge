package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class DrawingUtil {
   private static final Minecraft CLIENT = Minecraft.getInstance();

   public static void drawRandomVersionBackground(Mod mod, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
      int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
      Random random = new Random((long)seed);
      int color = -16777216 | Mth.hsvToRgb(random.nextFloat(1.0F), random.nextFloat(0.7F, 0.8F), 0.9F);
      if (!(Boolean)ModMenu.getConfig().RANDOM_JAVA_COLORS.get()) {
         color = -2271658;
      }

      guiGraphics.fill(x, y, x + width, y + height, color);
   }

   public static void drawWrappedString(GuiGraphicsExtractor guiGraphics, String string, int x, int y, int wrapWidth, int lines, int color) {
      while(string != null && string.endsWith("\n")) {
         string = string.substring(0, string.length() - 1);
      }

      List<FormattedText> strings = CLIENT.font.getSplitter().splitLines(Component.literal(string), wrapWidth, Style.EMPTY);

      for(int i = 0; i < strings.size() && i < lines; ++i) {
         FormattedText renderable = (FormattedText)strings.get(i);
         if (i == lines - 1 && strings.size() > lines) {
            renderable = FormattedText.composite(new FormattedText[]{(FormattedText)strings.get(i), FormattedText.of("...")});
         }

         FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
         int x1 = x;
         if (CLIENT.font.isBidirectional()) {
            int width = CLIENT.font.width(line);
            x1 = (int)((float)x + (float)(wrapWidth - width));
         }

         Font var10001 = CLIENT.font;
         Objects.requireNonNull(CLIENT.font);
         guiGraphics.text(var10001, line, x1, y + i * 9, color, true);
      }

   }

   public static void drawBadge(GuiGraphicsExtractor guiGraphics, int x, int y, int tagWidth, FormattedCharSequence charSequence, int outlineColor, int fillColor, int textColor) {
      guiGraphics.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
      int var10003 = x + 1;
      Objects.requireNonNull(CLIENT.font);
      guiGraphics.fill(x, y, var10003, y + 9, outlineColor);
      int var10001 = x + 1;
      int var10002 = y + 1;
      Objects.requireNonNull(CLIENT.font);
      var10002 = var10002 + 9 - 1;
      var10003 = x + tagWidth;
      Objects.requireNonNull(CLIENT.font);
      guiGraphics.fill(var10001, var10002, var10003, y + 9 + 1, outlineColor);
      var10001 = x + tagWidth;
      var10003 = x + tagWidth + 1;
      Objects.requireNonNull(CLIENT.font);
      guiGraphics.fill(var10001, y, var10003, y + 9, outlineColor);
      var10001 = x + 1;
      var10003 = x + tagWidth;
      Objects.requireNonNull(CLIENT.font);
      guiGraphics.fill(var10001, y, var10003, y + 9, fillColor);
      guiGraphics.text(CLIENT.font, charSequence, (int)((float)(x + 1) + (float)(tagWidth - CLIENT.font.width(charSequence)) / 2.0F), y + 1, textColor, false);
   }
}
