package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.BadgeToogleButton;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

public class BadgeScreen extends Screen {
   @Nullable
   private AbstractWidget badgeButton;
   private final Mod mod;
   private final int posX;

   protected BadgeScreen(Mod mod, int paneWidth, int searchBoxWidth) {
      super(CommonComponents.EMPTY);
      this.mod = mod;
      this.posX = paneWidth / 2 + searchBoxWidth / 2 - 10 + 26;
   }

   public void onClose() {
      ModMenu.getConfig().save();
      super.onClose();
   }

   protected void init() {
      this.badgeButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, (button) -> {
         this.onClose();
      }).position(this.posX, 22).size(20, 20).uv(0, 0, 20).texture(ModsScreen.BADGE_BUTTON_LOCATION, 32, 64).build();
      this.addRenderableWidget(this.badgeButton);
      int i = 0;
      int buttonX = this.posX - 11;
      Iterator var3 = ModBadge.BADGES.iterator();

      while(var3.hasNext()) {
         Map<String, ModBadge> badgeMap = (Map)var3.next();

         for(Iterator var5 = badgeMap.entrySet().iterator(); var5.hasNext(); ++i) {
            Entry<String, ModBadge> badgeEntry = (Entry)var5.next();
            ModBadge badge = (ModBadge)badgeEntry.getValue();
            this.addRenderableWidget(BadgeToogleButton.badgeButtonBuilder(CommonComponents.EMPTY, (button) -> {
               ModMenuConfig config = ModMenu.getConfig();
               if (this.mod.getBadges().contains(badge)) {
                  this.mod.getBadges().remove(badge);
                  ((Set)config.mod_badges.get(this.mod.getId())).remove(badgeEntry.getKey());
                  if (this.mod.getBadgeNames().contains(badgeEntry.getKey())) {
                     ((Set)config.disabled_mod_badges.computeIfAbsent(this.mod.getId(), (v) -> {
                        return new LinkedHashSet();
                     })).add((String)badgeEntry.getKey());
                  }
               } else {
                  this.mod.getBadges().add(badge);
                  Set<String> disabled_badges = (Set)config.disabled_mod_badges.get(this.mod.getId());
                  if (disabled_badges != null && disabled_badges.contains(badgeEntry.getKey())) {
                     disabled_badges.remove(badgeEntry.getKey());
                  } else {
                     ((Set)config.mod_badges.get(this.mod.getId())).add((String)badgeEntry.getKey());
                  }
               }

               ((BadgeToogleButton)button).toggle();
            }, this.mod.getBadges().contains(badge)).position(buttonX, 43 + 11 * i).size(11, 11).uv(0, 0, 11).build());
         }
      }

   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
      int i = 0;
      Iterator var7 = ModBadge.BADGES.iterator();

      while(var7.hasNext()) {
         Map<String, ModBadge> badges = (Map)var7.next();

         for(Iterator var9 = badges.entrySet().iterator(); var9.hasNext(); ++i) {
            Entry<String, ModBadge> mapEntry = (Entry)var9.next();
            ModBadge badge = (ModBadge)mapEntry.getValue();
            int badgeWidth = this.minecraft.font.width(badge.getComponent().getVisualOrderText()) + 6;
            DrawingUtil.drawBadge(guiGraphics, this.posX, 43 + 11 * i, badgeWidth, badge.getComponent().getVisualOrderText(), badge.getOutlineColor(), badge.getFillColor(), badge.getTextColor());
         }
      }

   }
}
