package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig.Sorting;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ChildParentEntry;
import com.terraformersmc.modmenu.gui.widget.entries.IndependentEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ModListWidget extends ObjectSelectionList<ModListEntry> implements AutoCloseable {
   public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");
   private final ModsScreen parent;
   private List<Mod> mods = null;
   private final Set<Mod> addedMods = new HashSet();
   private String selectedModId = null;
   private final NeoforgeIconHandler iconHandler = new NeoforgeIconHandler();
   private Double restoreScrollY = null;
   public int itemHeight;
   public int headerHeight = 0;

   public ModListWidget(Minecraft client, int width, int height, int y, int itemHeight, ModListWidget list, ModsScreen parent) {
      super(client, width, height, y, itemHeight);
      this.itemHeight = itemHeight;
      this.parent = parent;
      if (list != null) {
         this.mods = list.mods;
         this.restoreScrollY = list.getScrollAmount();
      }

   }

   public int getRowTop(int index) {
      return this.getY() + 4 - (int)this.scrollAmount() + index * this.itemHeight;
   }

   public int getItemCount() {
      return super.getItemCount();
   }

   public ModListEntry getSelected() {
      return (ModListEntry)super.getSelected();
   }

   public void setScrollAmount(double amount) {
      super.setScrollAmount(amount);
      int denominator = Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4));
      if (denominator <= 0) {
         this.parent.updateScrollPercent(0.0D);
      } else {
         double percent = this.scrollAmount() / (double)Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4));
         this.parent.updateScrollPercent(percent);
      }

   }

   public double getScrollAmount() {
      return this.scrollAmount();
   }

   protected int contentHeight() {
      return this.getItemCount() * this.itemHeight + 4;
   }

   public int maxScrollAmount() {
      return Math.max(0, this.contentHeight() - this.height);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int maxScroll = this.maxScrollAmount();
      if (maxScroll <= 0) {
         return false;
      } else {
         double scrollRate = this.scrollRate();
         double newScroll = this.scrollAmount() - verticalAmount * scrollRate;
         newScroll = Math.max(0.0D, Math.min(newScroll, (double)maxScroll));
         this.setScrollAmount(newScroll);
         return true;
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      if (!this.isMouseOver(mouseX, mouseY)) {
         return false;
      } else {
         ModListEntry entry = this.getEntryAtPos(mouseX, mouseY);
         return entry != null ? entry.mouseClicked(event, doubleClick) : false;
      }
   }

   public boolean isFocused() {
      return this.parent.getFocused() == this;
   }

   public void select(ModListEntry entry) {
      this.setSelected(entry);
      if (entry != null) {
         Mod mod = entry.mod;

         try {
            Object narrator = Minecraft.getInstance().getNarrator();
            Method m = narrator.getClass().getMethod("sayNow", String.class);
            m.invoke(narrator, Component.translatable("narrator.select", new Object[]{mod.getTranslatedName()}).getString());
         } catch (Throwable var5) {
         }
      }

   }

   public void setSelected(ModListEntry entry) {
      double savedScroll = this.scrollAmount();

      try {
         Field selectedField = null;
         Class clazz = this.getClass().getSuperclass();

         while(clazz != null && selectedField == null) {
            try {
               selectedField = clazz.getDeclaredField("selected");
            } catch (NoSuchFieldException var7) {
               clazz = clazz.getSuperclass();
            }
         }

         if (selectedField != null) {
            selectedField.setAccessible(true);
            selectedField.set(this, entry);
         } else {
            super.setSelected(entry);
         }
      } catch (Throwable var8) {
         super.setSelected(entry);
      }

      this.setScrollAmount(savedScroll);
      this.selectedModId = entry != null && entry.mod != null ? entry.mod.getId() : null;
      this.parent.updateSelectedEntry(entry);
   }

   private void deferSetSelected(ModListEntry entry, String ctx) {
      Minecraft.getInstance().execute(() -> {
         double savedScroll = this.scrollAmount();

         try {
            Field selectedField = null;
            Class clazz = this.getClass().getSuperclass();

            while(clazz != null && selectedField == null) {
               try {
                  selectedField = clazz.getDeclaredField("selected");
               } catch (NoSuchFieldException var7) {
                  clazz = clazz.getSuperclass();
               }
            }

            if (selectedField != null) {
               selectedField.setAccessible(true);
               selectedField.set(this, entry);
            } else {
               super.setSelected(entry);
            }
         } catch (Throwable var8) {
            super.setSelected(entry);
         }

         this.setScrollAmount(savedScroll);
         this.selectedModId = entry != null && entry.mod != null ? entry.mod.getId() : null;
         this.parent.updateSelectedEntry(entry);
      });
   }

   protected boolean isSelectedItem(int index) {
      ModListEntry selected = this.getSelected();
      ModListEntry e = this.getEntry(index);
      return selected != null && e != null && selected.mod != null && e.mod != null && selected.mod.getId().equals(e.mod.getId());
   }

   public int addEntry(ModListEntry entry) {
      if (this.addedMods.contains(entry.mod)) {
         return 0;
      } else {
         this.addedMods.add(entry.mod);
         int idx = super.addEntry(entry);
         if (entry.mod != null && entry.mod.getId().equals(this.selectedModId)) {
            this.deferSetSelected(entry, "addEntry-match");
         }

         return idx;
      }
   }

   protected void removeEntry(ModListEntry entry) {
      this.addedMods.remove(entry.mod);
      super.removeEntry(entry);
   }

   protected ModListEntry remove(int index) {
      ModListEntry e = (ModListEntry)this.children().remove(index);
      if (e != null) {
         this.addedMods.remove(e.mod);
      }

      return e;
   }

   public void finalizeInit() {
      this.reloadFilters();
      if (this.restoreScrollY != null) {
         this.setScrollAmount(this.restoreScrollY);
         this.restoreScrollY = null;
      }

   }

   public void reloadFilters() {
      this.filter(this.parent.getSearchInput(), true, false);
   }

   public void filter(String searchTerm, boolean refresh) {
      this.filter(searchTerm, refresh, true);
   }

   private boolean hasVisibleChildMods(Mod parent) {
      List<Mod> children = ModMenu.PARENT_MAP.get(parent);
      boolean hideLibraries = !(Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get();
      return !children.stream().allMatch((child) -> {
         return child.isHidden() || hideLibraries && (child.getBadgeNames().contains("library") || child.getBadges().contains(ModBadge.LIBRARY));
      });
   }

   private void filter(String searchTerm, boolean refresh, boolean search) {
      this.clearEntries();
      this.addedMods.clear();
      ModMenu.LOGGER.debug("ModListWidget.filter() start: search='{}' refresh={} searchFlag={}", new Object[]{searchTerm, refresh, search});
      Collection<Mod> mods = ModMenu.MODS.values().stream().filter((modx) -> {
         if (ModMenu.getConfig().CONFIG_MODE.get()) {
            return !this.parent.getModHasConfigScreen(modx.getContainer());
         } else {
            return !modx.isHidden();
         }
      }).collect(Collectors.toSet());
      if (DEBUG) {
         mods = new ArrayList((Collection)mods);
      }

      if (this.mods == null || refresh) {
         this.mods = new ArrayList();
         this.mods.addAll(mods);
         ModMenu.LOGGER.debug("ModListWidget.filter(): collected {} mods before sorting", this.mods.size());
         this.mods.sort(((Sorting)ModMenu.getConfig().SORTING.get()).getComparator());
         ModMenu.LOGGER.debug("ModListWidget.filter(): sorting complete");
      }

      List<Mod> matched = ModSearch.search(this.parent, searchTerm, this.mods);
      ModMenu.LOGGER.debug("ModListWidget.filter(): matched {} mods after search", matched.size());
      int __ml_idx = 0;
      List<ModListEntry> entriesToAdd = new ArrayList();
      Iterator var8 = matched.iterator();

      while(true) {
         String modId;
         long __entryStart;
         long beforeIndependent;
         while(true) {
            if (!var8.hasNext()) {
               ModMenu.LOGGER.debug("ModListWidget.filter(): adding {} entries in batch", entriesToAdd.size());

               try {
                  Field childrenField = null;
                  Class currentClass = this.getClass();

                  while(currentClass != null && childrenField == null) {
                     try {
                        childrenField = currentClass.getDeclaredField("children");
                     } catch (NoSuchFieldException var31) {
                        currentClass = currentClass.getSuperclass();
                     }
                  }

                  if (childrenField != null) {
                     childrenField.setAccessible(true);
                     List<ModListEntry> internalList = (List)childrenField.get(this);
                     String internalClassName = internalList.getClass().getName();
                     boolean modifiable = !internalClassName.contains("Unmodifiable") && !internalClassName.contains("UnmodifiableCollection");
                     if (modifiable) {
                        try {
                           beforeIndependent = System.nanoTime();
                           internalList.addAll(entriesToAdd);
                           long __internalAddEnd = System.nanoTime();
                           ModMenu.LOGGER.debug("ModListWidget.filter(): internalList.addAll took {} ms", (__internalAddEnd - beforeIndependent) / 1000000L);
                           Iterator var56 = entriesToAdd.iterator();

                           while(var56.hasNext()) {
                              ModListEntry entry = (ModListEntry)var56.next();
                              this.addedMods.add(entry.mod);
                              if (entry.mod != null && entry.mod.getId().equals(this.selectedModId)) {
                                 this.deferSetSelected(entry, "bulk-match");
                              }
                           }
                        } catch (UnsupportedOperationException var33) {
                           ModMenu.LOGGER.warn("ModListWidget.filter(): internalList.addAll threw UnsupportedOperationException (class={}), falling back to per-entry addEntry()", internalList.getClass().getName());
                           Iterator var50 = entriesToAdd.iterator();

                           while(var50.hasNext()) {
                              ModListEntry entry = (ModListEntry)var50.next();
                              this.addEntry(entry);
                           }
                        }
                     } else {
                        ModMenu.LOGGER.warn("ModListWidget.filter(): internal 'children' list appears unmodifiable (class={}), falling back to per-entry addEntry()", internalClassName);
                        Iterator var51 = entriesToAdd.iterator();

                        while(var51.hasNext()) {
                           ModListEntry entry = (ModListEntry)var51.next();
                           this.addEntry(entry);
                        }
                     }
                  } else {
                     ModMenu.LOGGER.warn("ModListWidget.filter(): could not find internal 'children' field, falling back to addEntry()");
                     Iterator var43 = entriesToAdd.iterator();

                     while(var43.hasNext()) {
                        ModListEntry entry = (ModListEntry)var43.next();
                        this.addEntry(entry);
                     }
                  }
               } catch (Throwable var34) {
                  ModMenu.LOGGER.error("ModListWidget.filter(): error adding entries via reflection, falling back to addEntry()", var34);
                  Iterator var38 = entriesToAdd.iterator();

                  while(var38.hasNext()) {
                     ModListEntry entry = (ModListEntry)var38.next();
                     this.addEntry(entry);
                  }
               }

               try {
                  ModListEntry parentSelected = this.parent.getSelectedEntry();
                  ModListEntry currentSelected = this.getSelected();
                  if (parentSelected != null && !this.children().isEmpty()) {
                     boolean found = false;
                     Iterator var47 = this.children().iterator();

                     while(var47.hasNext()) {
                        ModListEntry entry = (ModListEntry)var47.next();
                        if (entry.mod != null && parentSelected.mod != null && entry.mod.equals(parentSelected.mod)) {
                           this.deferSetSelected(entry, "parent-match");
                           found = true;
                           break;
                        }
                     }

                     if (!found && !this.children().isEmpty() && this.getEntry(0) != null) {
                        this.deferSetSelected(this.getEntry(0), "select-first");
                     }
                  } else if (currentSelected == null && !this.children().isEmpty() && this.getEntry(0) != null) {
                     this.deferSetSelected(this.getEntry(0), "select-first");
                  }
               } catch (Exception var32) {
                  ModMenu.LOGGER.error("ModListWidget.filter(): Error in selection logic", var32);
               }

               if (this.getScrollAmount() > (double)Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4))) {
                  this.setScrollAmount((double)Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4)));
               }

               return;
            }

            Mod mod = (Mod)var8.next();
            if ((__ml_idx++ & 31) == 0) {
               ModMenu.LOGGER.trace("ModListWidget.filter(): processing matched index {}", __ml_idx);
            }

            modId = mod.getId();
            __entryStart = System.nanoTime();
            ModMenu.LOGGER.debug("ModListWidget.filter(): processing mod '{}' ({}/{}) start", new Object[]{modId, __ml_idx, matched.size()});
            boolean var30 = false;

            try {
               var30 = true;
               if (!(Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get()) {
                  if (mod.getBadgeNames().contains("library")) {
                     var30 = false;
                     break;
                  }

                  if (mod.getBadges().contains(ModBadge.LIBRARY)) {
                     var30 = false;
                     break;
                  }
               }

               if (!ModMenu.PARENT_MAP.values().contains(mod)) {
                  if (ModMenu.PARENT_MAP.keySet().contains(mod) && this.hasVisibleChildMods(mod)) {
                     List<Mod> children = ModMenu.PARENT_MAP.get(mod);
                     children.sort(((Sorting)ModMenu.getConfig().SORTING.get()).getComparator());
                     ModMenu.LOGGER.debug("ModListWidget.filter(): creating ParentEntry for mod {} ({} children)", modId, children.size());
                     long beforeParent = System.nanoTime();
                     ParentEntry parent = new ParentEntry(mod, children, this);
                     long afterParentCreate = System.nanoTime();
                     ModMenu.LOGGER.debug("ModListWidget.filter(): ParentEntry created in {} ms", (afterParentCreate - beforeParent) / 1000000L);
                     ModMenu.LOGGER.debug("ModListWidget.filter(): queuing ParentEntry for mod {}", modId);
                     entriesToAdd.add(parent);
                     if (this.parent.showModChildren.contains(modId)) {
                        List<Mod> validChildren = ModSearch.search(this.parent, searchTerm, children);
                        Iterator var20 = validChildren.iterator();

                        while(var20.hasNext()) {
                           Mod child = (Mod)var20.next();
                           this.collectChildEntries(child, validChildren, parent, List.of(parent), searchTerm, 1, entriesToAdd);
                        }

                        var30 = false;
                     } else {
                        var30 = false;
                     }
                  } else {
                     ModMenu.LOGGER.debug("ModListWidget.filter(): creating IndependentEntry for mod {}", modId);
                     beforeIndependent = System.nanoTime();
                     IndependentEntry entry = new IndependentEntry(mod, this);
                     long afterIndependentCreate = System.nanoTime();
                     ModMenu.LOGGER.debug("ModListWidget.filter(): IndependentEntry created in {} ms", (afterIndependentCreate - beforeIndependent) / 1000000L);
                     ModMenu.LOGGER.debug("ModListWidget.filter(): queuing IndependentEntry for mod {}", modId);
                     entriesToAdd.add(entry);
                     var30 = false;
                  }
               } else {
                  var30 = false;
               }
            } finally {
               if (var30) {
                  long __entryEnd = System.nanoTime();
                  ModMenu.LOGGER.debug("ModListWidget.filter(): processing mod '{}' finished, took {} ms", modId, (__entryEnd - __entryStart) / 1000000L);
               }
            }

            beforeIndependent = System.nanoTime();
            ModMenu.LOGGER.debug("ModListWidget.filter(): processing mod '{}' finished, took {} ms", modId, (beforeIndependent - __entryStart) / 1000000L);
         }

         beforeIndependent = System.nanoTime();
         ModMenu.LOGGER.debug("ModListWidget.filter(): processing mod '{}' finished, took {} ms", modId, (beforeIndependent - __entryStart) / 1000000L);
      }
   }

   public void addChildMod(Mod child, List<Mod> validChildren, ParentEntry parent, List<ModListEntry> parents, String searchTerm, int parentCount) {
      if (ModMenu.PARENT_MAP.keySet().contains(child) && this.hasVisibleChildMods(child)) {
         List<Mod> childChildren = ModMenu.PARENT_MAP.get(child);
         childChildren.sort(((Sorting)ModMenu.getConfig().SORTING.get()).getComparator());
         ChildParentEntry childParentEntry = new ChildParentEntry(child, parent, parents, childChildren, this, validChildren.indexOf(child) == validChildren.size() - 1);
         this.addEntry((ModListEntry)childParentEntry);
         if (this.parent.showModChildren.contains(child.getId())) {
            List<Mod> validChildChildren = ModSearch.search(this.parent, searchTerm, childChildren);
            Iterator var10 = validChildChildren.iterator();

            while(var10.hasNext()) {
               Mod childChild = (Mod)var10.next();
               List<ModListEntry> childParents = new ArrayList(parents);
               childParents.add(childParentEntry);
               this.addChildMod(childChild, validChildChildren, parent, childParents, searchTerm, parentCount + 1);
            }
         }
      } else {
         this.addEntry((ModListEntry)(new ChildEntry(child, parent, parents, this, validChildren.indexOf(child) == validChildren.size() - 1)));
      }

   }

   private void collectChildEntries(Mod child, List<Mod> validChildren, ParentEntry parent, List<ModListEntry> parents, String searchTerm, int parentCount, List<ModListEntry> collector) {
      if (ModMenu.PARENT_MAP.keySet().contains(child) && this.hasVisibleChildMods(child)) {
         List<Mod> childChildren = ModMenu.PARENT_MAP.get(child);
         childChildren.sort(((Sorting)ModMenu.getConfig().SORTING.get()).getComparator());
         ChildParentEntry childParentEntry = new ChildParentEntry(child, parent, parents, childChildren, this, validChildren.indexOf(child) == validChildren.size() - 1);
         collector.add(childParentEntry);
         if (this.parent.showModChildren.contains(child.getId())) {
            List<Mod> validChildChildren = ModSearch.search(this.parent, searchTerm, childChildren);
            Iterator var11 = validChildChildren.iterator();

            while(var11.hasNext()) {
               Mod childChild = (Mod)var11.next();
               List<ModListEntry> childParents = new ArrayList(parents);
               childParents.add(childParentEntry);
               this.collectChildEntries(childChild, validChildChildren, parent, childParents, searchTerm, parentCount + 1, collector);
            }
         }
      } else {
         collector.add(new ChildEntry(child, parent, parents, this, validChildren.indexOf(child) == validChildren.size() - 1));
      }

   }

   public void renderListItems(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
      int entryLeft = this.getRowLeft();
      int entryWidth = this.getRowWidth();
      int entryHeight = this.itemHeight - 4;
      int entryCount = this.getItemCount();
      int listY = this.getY();
      int listBottom = this.getBottom();

      for(int index = 0; index < entryCount; ++index) {
         int entryTop = this.getRowTop(index);
         int entryBottom = entryTop + this.itemHeight;
         if (entryBottom >= listY && entryTop <= listBottom) {
            ModListEntry entry = this.getEntry(index);
            if (entry != null) {
               if (this.isSelectedItem(index)) {
                  int entryContentLeft = entryLeft + entry.getXOffset() - 2;
                  int selectionRight = entryLeft + entryWidth + 2;
                  int selectionTop = entryTop - 1;
                  int alpha = this.isFocused() ? 160 : 64;
                  int outlineColor = alpha << 24 | 16747520;
                  guiGraphics.fill(entryContentLeft, selectionTop, selectionRight, selectionTop + 2, outlineColor);
                  guiGraphics.fill(entryContentLeft, selectionTop + entryHeight + 1, selectionRight, selectionTop + entryHeight + 3, outlineColor);
                  guiGraphics.fill(entryContentLeft, selectionTop + 1, entryContentLeft + 2, selectionTop + entryHeight + 2, outlineColor);
                  guiGraphics.fill(selectionRight - 2, selectionTop + 1, selectionRight, selectionTop + entryHeight + 2, outlineColor);
                  int innerAlpha = this.isFocused() ? 48 : 18;
                  int innerColor = innerAlpha << 24 | 16747520;
                  guiGraphics.fill(entryContentLeft + 2, selectionTop + 2, selectionRight - 2, selectionTop + entryHeight + 1, innerColor);
               }

               boolean hovered = this.isMouseOver((double)mouseX, (double)mouseY) && mouseY >= entryTop && mouseY < entryBottom;
               entry.renderEntry(guiGraphics, index, entryTop, entryLeft, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
            }
         }
      }

   }

   public void ensureVisible(ModListEntry entry) {
      if (entry != null) {
         int index = this.children().indexOf(entry);
         if (index >= 0) {
            int top = this.getRowTop(index);
            int bottom = top + this.itemHeight;
            if (top < this.getY()) {
               this.setScrollAmount(this.getScrollAmount() - (double)(this.getY() - top));
            } else if (bottom > this.getBottom()) {
               this.setScrollAmount(this.getScrollAmount() + (double)(bottom - this.getBottom()));
            }

         }
      }
   }

   public boolean legacyKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 265 && keyCode != 264) {
         return this.getSelected() != null ? this.getSelected().legacyKeyPressed(keyCode, scanCode, modifiers) : false;
      } else {
         return false;
      }
   }

   public boolean keyPressed(Object event) {
      try {
         Integer key = extractInt(event, "getKeyCode", "getKey", "key");
         Integer scan = extractInt(event, "getScanCode", "getScan");
         Integer mods = extractInt(event, "getModifiers", "getModifiersEx", "mods");
         return this.legacyKeyPressed(key == null ? 0 : key, scan == null ? 0 : scan, mods == null ? 0 : mods);
      } catch (Throwable var5) {
         return false;
      }
   }

   private static Integer extractInt(Object ev, String... names) {
      if (ev == null) {
         return null;
      } else {
         String[] var2 = names;
         int var3 = names.length;

         int var4;
         String n;
         Method m;
         Object val;
         for(var4 = 0; var4 < var3; ++var4) {
            n = var2[var4];

            try {
               m = ev.getClass().getMethod(n);
               m.setAccessible(true);
               val = m.invoke(ev);
               if (val instanceof Integer) {
                  return (Integer)val;
               }

               if (val instanceof Short) {
                  return ((Short)val).intValue();
               }

               if (val instanceof Byte) {
                  return ((Byte)val).intValue();
               }

               if (val instanceof Long) {
                  return ((Long)val).intValue();
               }

               if (val instanceof Number) {
                  return ((Number)val).intValue();
               }
            } catch (NoSuchMethodException var9) {
            } catch (Throwable var10) {
            }
         }

         var2 = names;
         var3 = names.length;

         for(var4 = 0; var4 < var3; ++var4) {
            n = var2[var4];

            try {
               m = ev.getClass().getMethod(n);
               m.setAccessible(true);
               val = m.invoke(ev);
               if (val instanceof Boolean) {
                  return (Boolean)val ? 1 : 0;
               }
            } catch (Throwable var8) {
            }
         }

         return null;
      }
   }

   public final ModListEntry getEntryAtPos(double x, double y) {
      int int_5 = Mth.floor(y - (double)this.getY()) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int index = int_5 / this.itemHeight;
      return x < (double)this.getScrollbarPosition() && x >= (double)this.getRowLeft() && x <= (double)(this.getRowLeft() + this.getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? (ModListEntry)this.children().get(index) : null;
   }

   protected int getScrollbarPosition() {
      return this.width - 6;
   }

   public int getRowWidth() {
      return this.width - (Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
   }

   public int getRowLeft() {
      return this.getX() + 6;
   }

   public int getWidth() {
      return this.width;
   }

   public int getTop() {
      return this.getY();
   }

   public ModsScreen getParent() {
      return this.parent;
   }

   protected int getMaxPosition() {
      return this.getItemCount() * this.itemHeight + 4;
   }

   public int getDisplayedCountFor(Set<String> set) {
      int count = 0;
      Iterator var3 = this.children().iterator();

      while(var3.hasNext()) {
         ModListEntry c = (ModListEntry)var3.next();
         if (c.mod != null && set.contains(c.mod.getId())) {
            ++count;
         }
      }

      return count;
   }

   public void close() {
      this.iconHandler.close();
   }

   public NeoforgeIconHandler getNeoforgeIconHandler() {
      return this.iconHandler;
   }

   public int getRowBottom(int index) {
      return super.getRowBottom(index);
   }

   public ModListEntry getEntry(int index) {
      return index >= 0 && index < this.children().size() ? this.children().get(index) : null;
   }
}
