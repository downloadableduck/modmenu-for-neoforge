package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;

 public class ChildEntry extends ModListEntry {
   protected final boolean bottomChild;
   protected final ParentEntry parent;
   protected final List<ModListEntry> parents;

   public ChildEntry(Mod mod, ParentEntry parent, List<ModListEntry> parents, ModListWidget list, boolean bottomChild) {
      super(mod, list);
      this.bottomChild = bottomChild;
      this.parent = parent;
      this.parents = parents;
   }

   public void renderEntry(GuiGraphicsExtractor guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
      super.renderEntry(guiGraphics, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
      x -= 9;
      int color = -6250336;

      for(int i = 1; i < this.parents.size(); ++i) {
         Object var14 = this.parents.get(i);
         if (var14 instanceof ChildParentEntry) {
            ChildParentEntry childParent = (ChildParentEntry)var14;
            if (!childParent.bottomChild) {
               guiGraphics.fill(x + childParent.getXOffset(), y - 2, x + 1 + childParent.getXOffset(), y + rowHeight + 2, color);
            }
         }
      }

      x += this.getXOffset();
      guiGraphics.fill(x, y - 2, x + 1, y + (this.bottomChild ? rowHeight / 2 : rowHeight + 2), color);
      guiGraphics.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
   }

   public boolean legacyKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 263) {
         this.list.setSelected(this.parent);
         this.list.ensureVisible(this.parent);
         return true;
      } else {
         return false;
      }
   }

   public int getXOffset() {
      return 13 * this.parents.size();
   }
}
