package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ParentEntry extends ModListEntry {
   private static final Identifier PARENT_MOD_TEXTURE = Identifier.parse("modmenu:textures/gui/parent_mod.png");
   protected List<Mod> children;
   protected ModListWidget list;
   protected boolean hoveringIcon = false;

   public ParentEntry(Mod parent, List<Mod> children, ModListWidget list) {
      super(parent, list);
      this.children = children;
      this.list = list;
   }

   public void renderEntry(GuiGraphicsExtractor guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
      super.renderEntry(guiGraphics, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
      Font font = this.client.font;
      Objects.requireNonNull(font);
      int childrenBadgeHeight = 9;
      Objects.requireNonNull(font);
      int childrenBadgeWidth = 9;
      int shownChildren = ModSearch.search(this.list.getParent(), this.list.getParent().getSearchInput(), this.getChildren()).size();
      int allChildren = this.children.stream().filter((child) -> {
         return !child.isHidden() && ((Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() || !child.getBadges().contains(ModBadge.LIBRARY));
      }).toList().size();
      Component str = shownChildren == allChildren ? Component.literal(String.valueOf(shownChildren)) : Component.literal(shownChildren + "/" + allChildren);
      int childrenWidth = font.width(str) - 1;
      if (childrenBadgeWidth < childrenWidth + 4) {
         childrenBadgeWidth = childrenWidth + 4;
      }

      int iconSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 19 : 32;
      int childrenBadgeX = x + iconSize - childrenBadgeWidth;
      int childrenBadgeY = y + iconSize - childrenBadgeHeight;
      int childrenOutlineColor = -15698860;
      int childrenFillColor = -16172759;
      guiGraphics.fill(childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenOutlineColor);
      guiGraphics.fill(childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
      guiGraphics.fill(childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor);
      guiGraphics.fill(childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, childrenFillColor);
      guiGraphics.fill(childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, childrenOutlineColor);
      guiGraphics.text(font, str.getVisualOrderText(), (int)((float)childrenBadgeX + (float)childrenBadgeWidth / 2.0F - (float)childrenWidth / 2.0F), childrenBadgeY + 1, 13290186, false);
      this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
      if (this.isMouseOver((double)mouseX, (double)mouseY)) {
         guiGraphics.fill(x, y, x + iconSize, y + iconSize, -1601138544);
         int xOffset = this.list.getParent().showModChildren.contains(this.getMod().getId()) ? iconSize : 0;
         int yOffset = this.hoveringIcon ? iconSize : 0;
         int textureSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 152 : 256;
         guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PARENT_MOD_TEXTURE, x, y, (float)xOffset, (float)yOffset, iconSize + xOffset, iconSize + yOffset, textureSize, textureSize);
      }

   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      int iconSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 19 : 32;
      boolean quickConfigure = (Boolean)ModMenu.getConfig().QUICK_CONFIGURE.get();
      if (mouseX - (double)this.list.getRowLeft() <= (double)iconSize) {
         this.toggleChildren();
         return true;
      } else if (quickConfigure || !doubleClick && Util.getMillis() - this.sinceLastClick >= 250L) {
         return super.mouseClicked(event, doubleClick);
      } else {
         this.toggleChildren();
         return true;
      }
   }

   private void toggleChildren() {
      String id = this.getMod().getId();
      if (this.list.getParent().showModChildren.contains(id)) {
         this.list.getParent().showModChildren.remove(id);
      } else {
         this.list.getParent().showModChildren.add(id);
      }

      this.list.filter(this.list.getParent().getSearchInput(), false);
   }

   public boolean legacyKeyPressed(int keyCode, int scanCode, int modifiers) {
      String modId = this.getMod().getId();
      if (keyCode != 257 && keyCode != 32) {
         if (keyCode == 263) {
            if (this.list.getParent().showModChildren.contains(modId)) {
               this.list.getParent().showModChildren.remove(modId);
               this.list.filter(this.list.getParent().getSearchInput(), false);
            }

            return true;
         } else if (keyCode == 262) {
            if (!this.list.getParent().showModChildren.contains(modId)) {
               this.list.getParent().showModChildren.add(modId);
               this.list.filter(this.list.getParent().getSearchInput(), false);
               return true;
            } else {
               return this.list.legacyKeyPressed(264, 0, 0);
            }
         } else {
            return super.legacyKeyPressed(keyCode, scanCode, modifiers);
         }
      } else {
         if (this.list.getParent().showModChildren.contains(modId)) {
            this.list.getParent().showModChildren.remove(modId);
         } else {
            this.list.getParent().showModChildren.add(modId);
         }

         this.list.filter(this.list.getParent().getSearchInput(), false);
         return true;
      }
   }

   public void setChildren(List<Mod> children) {
      this.children = children;
   }

   public void addChildren(List<Mod> children) {
      this.children.addAll(children);
   }

   public void addChildren(Mod... children) {
      this.children.addAll(Arrays.asList(children));
   }

   public List<Mod> getChildren() {
      return this.children;
   }

   public boolean isMouseOver(double double_1, double double_2) {
      return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
   }
}
