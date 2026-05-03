package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class DescriptionListWidget extends ObjectSelectionList<DescriptionListWidget.DescriptionEntry> {
   private static final Component LINKS_TEXT = Component.translatable("modmenu.links");
   private static final Component SOURCE_TEXT;
   private static final Component LICENSE_TEXT;
   private static final Component CREDITS_TEXT;
   private static final Component AUTHORS_TEXT;
   private final Font textRenderer;
   private Mod selectedMod = null;
   public int itemHeight;

   public DescriptionListWidget(Minecraft client, int width, int height, int y, int itemHeight, DescriptionListWidget list, ModsScreen parent) {
      super(client, width, height, y, itemHeight);
      this.textRenderer = client.font;
      this.itemHeight = itemHeight;
      if (list != null) {
         try {
            this.setScrollAmount(list.getScrollAmount());
         } catch (Throwable var9) {
         }
      }

   }

   public void updateSelectedModIfRequired(Mod mod) {
      if (mod != this.selectedMod) {
         this.selectedMod = mod;
         this.clearEntries();

         try {
            this.setScrollAmount(0.0D);
         } catch (Throwable var3) {
         }

         this.rebuildUI();
      }

   }

   public int getRowWidth() {
      return this.width - 10;
   }

   protected int getScrollbarPosition() {
      return this.getX() + this.width - 6;
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

   public double scrollRate() {
      return 20.0D;
   }

   private void renderScrollbar(GuiGraphicsExtractor guiGraphics) {
      int maxScroll = this.maxScrollAmount();
      if (maxScroll > 0) {
         int scrollbarX = this.getScrollbarPosition();
         int scrollbarWidth = 6;
         int scrollbarHeight = this.height;
         int contentH = this.contentHeight();
         int thumbHeight = Math.max(32, (int)((float)(scrollbarHeight * scrollbarHeight) / (float)contentH));
         thumbHeight = Math.min(thumbHeight, scrollbarHeight);
         int thumbY = (int)(this.scrollAmount() / (double)maxScroll * (double)(scrollbarHeight - thumbHeight));
         thumbY = Math.max(0, Math.min(thumbY, scrollbarHeight - thumbHeight));
         int scrollbarTop = this.getY();
         guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + scrollbarWidth, scrollbarTop + scrollbarHeight, Integer.MIN_VALUE);
         guiGraphics.fill(scrollbarX, scrollbarTop + thumbY, scrollbarX + scrollbarWidth, scrollbarTop + thumbY + thumbHeight, -6250336);
      }

   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int maxScroll = this.maxScrollAmount();
      if (maxScroll <= 0) {
         return false;
      } else {
         double scrollSpeed = this.scrollRate();
         double newScroll = this.scrollAmount() - verticalAmount * scrollSpeed;
         newScroll = Math.max(0.0D, Math.min(newScroll, (double)maxScroll));
         this.setScrollAmount(newScroll);
         return true;
      }
   }

   public int getRowTop(int index) {
      return this.getY() + 4 - (int)this.scrollAmount() + index * this.itemHeight;
   }

   protected void renderListItems(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
      int entryLeft = this.getRowLeft();
      int entryCount = this.getItemCount();
      int listY = this.getY();
      int listBottom = this.getBottom();

      for(int index = 0; index < entryCount; ++index) {
         int entryTop = this.getRowTop(index);
         int entryBottom = entryTop + this.itemHeight;
         if (entryBottom >= listY && entryTop <= listBottom) {
            com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry entry = (com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry)this.children().get(index);
            if (entry != null) {
               entry.render(guiGraphics, entryLeft, entryTop, mouseX, mouseY, delta);
            }
         }
      }

      this.renderScrollbar(guiGraphics);
   }

   public int getRowLeft() {
      return this.getX() + 6;
   }

   private void rebuildUI() {
      this.clearEntries();
      if (this.selectedMod != null) {
         int wrapWidth = Math.max(10, this.getRowWidth() - 5);
         Component description = this.selectedMod.getFormattedDescription();
          //getformatted description is definitely the issue
         List authors;
         Iterator var4;
         FormattedCharSequence line;
         if (description != null && !description.getString().isEmpty()) {
            authors = this.textRenderer.split(description, wrapWidth);
            var4 = authors.iterator();

            while(var4.hasNext()) {
               line = (FormattedCharSequence)var4.next();
               this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 0));
            }
         }

         authors = this.selectedMod.getAuthors();
         Iterator var6;
         String sourceLink;
         if (authors != null && !authors.isEmpty()) {
            this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, FormattedCharSequence.EMPTY, 0));
            var4 = this.textRenderer.split(AUTHORS_TEXT, wrapWidth).iterator();

            while(var4.hasNext()) {
               line = (FormattedCharSequence)var4.next();
               this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 0));
            }

            var4 = authors.iterator();

            while(var4.hasNext()) {
               sourceLink = (String)var4.next();
               var6 = this.textRenderer.split(Component.literal(sourceLink), wrapWidth - 16).iterator();

               while(var6.hasNext()) {
                  line = (FormattedCharSequence)var6.next();
                  this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 8));
               }
            }
         }

         Map<String, String> links = this.selectedMod.getLinks();
         sourceLink = this.selectedMod.getSource();
         Iterator var9;
         if (links != null && !links.isEmpty() || sourceLink != null) {
            this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, FormattedCharSequence.EMPTY, 0));
            var6 = this.textRenderer.split(LINKS_TEXT, wrapWidth).iterator();

            while(var6.hasNext()) {
               line = (FormattedCharSequence)var6.next();
               this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 0));
            }

            if (sourceLink != null) {
               var6 = this.textRenderer.split(SOURCE_TEXT, wrapWidth - 16).iterator();

               while(var6.hasNext()) {
                  line = (FormattedCharSequence)var6.next();
                  this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 8));
               }
            }

            if (links != null) {
               var6 = links.entrySet().iterator();

               while(var6.hasNext()) {
                  Entry entry = (Entry)var6.next();
                  Component linkText = Component.literal(entry.getNarration().getString()).withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.UNDERLINE});
                  var9 = this.textRenderer.split(linkText, wrapWidth - 16).iterator();

                  while(var9.hasNext()) {
                     line = (FormattedCharSequence)var9.next();
                     this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 8));
                  }
               }
            }
         }

         Set<String> licenses = this.selectedMod.getLicense();
         if (licenses != null && !licenses.isEmpty()) {
            this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, FormattedCharSequence.EMPTY, 0));
            Iterator var22 = this.textRenderer.split(LICENSE_TEXT, wrapWidth).iterator();

            while(var22.hasNext()) {
                line = (FormattedCharSequence)var22.next();
               this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 0));
            }

            var22 = licenses.iterator();

            while(var22.hasNext()) {
               String license = (String)var22.next();
               var9 = this.textRenderer.split(Component.literal(license), wrapWidth - 16).iterator();

               while(var9.hasNext()) {
                  line = (FormattedCharSequence)var9.next();
                  this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 8));
               }
            }
         }

         SortedMap<String, Set<String>> credits = this.selectedMod.getCredits();
         if (credits != null && !credits.isEmpty()) {
            this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, FormattedCharSequence.EMPTY, 0));
            Iterator var26 = this.textRenderer.split(CREDITS_TEXT, wrapWidth).iterator();

            while(var26.hasNext()) {
                line = (FormattedCharSequence)var26.next();
               this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 0));
            }

            var26 = credits.entrySet().iterator();

            while(var26.hasNext()) {
               Entry<DescriptionEntry> roleEntry = (Entry)var26.next();
               String roleName = roleEntry.getNarration().getString();
               String translationKey = "modmenu.credits.role." + roleName.replaceAll("[ -]", "_").toLowerCase();
               String fallback = roleName.endsWith("r") ? roleName + "s" : roleName;
               Component roleText = Component.translatableWithFallback(translationKey, fallback).append(Component.literal(":"));
               Iterator var14 = this.textRenderer.split(roleText, wrapWidth - 16).iterator();

               while(var14.hasNext()) {
                   line = (FormattedCharSequence)var14.next();
                  this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 8));
               }

               var14 = ((Set)roleEntry.getNarration()).iterator();

               while(var14.hasNext()) {
                  String contributor = (String)var14.next();
                  Iterator var16 = this.textRenderer.split(Component.literal(contributor), wrapWidth - 24).iterator();

                  while(var16.hasNext()) {
                      line = (FormattedCharSequence)var16.next();
                     this.addEntry(new com.terraformersmc.modmenu.gui.widget.DescriptionListWidget.DescriptionEntry(this, line, 16));
                  }
               }
            }
         }

      }
   }

   static {
      SOURCE_TEXT = Component.translatable("modmenu.source").withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.UNDERLINE});
      LICENSE_TEXT = Component.translatable("modmenu.license");
      CREDITS_TEXT = Component.translatable("modmenu.credits");
      AUTHORS_TEXT = Component.translatable("modmenu.authorPrefix");
   }
    public class DescriptionEntry extends Entry<DescriptionEntry> {
        private final DescriptionListWidget list;
        private final FormattedCharSequence text;
        private final int indent;

        public DescriptionEntry(DescriptionListWidget list, FormattedCharSequence text, int indent) {
            this.list = list;
            this.text = text;
            this.indent = indent;
        }

        public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int mouseX, int mouseY, float delta) {
            guiGraphics.text(this.list.textRenderer, this.text, x + this.indent, y, -5592406, true);
        }

        public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int index = this.list.children().indexOf(this);
            int y = this.list.getRowTop(index);
            int x = this.list.getRowLeft();
            guiGraphics.text(this.list.textRenderer, this.text, x + this.indent, y, -5592406, true);
        }

        public Component getNarration() {
            return Component.empty();
        }
    }
}
