package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;

public class ModListEntry extends Entry<ModListEntry> {
   public static final Identifier UNKNOWN_ICON = Identifier.parse("minecraft:textures/misc/unknown_pack.png");
   private static final Identifier MOD_CONFIGURATION_ICON = Identifier.fromNamespaceAndPath("modmenu", "textures/gui/mod_configuration.png");
   private static final Identifier ERROR_ICON = Identifier.parse("minecraft:world_list/error");
   private static final Identifier ERROR_HIGHLIGHTED_ICON = Identifier.parse("minecraft:world_list/error_highlighted");
   protected final Minecraft client;
   public final Mod mod;
   protected final ModListWidget list;
   protected Tuple<Identifier, Dimension> iconLocation;
   protected Tuple<Identifier, Dimension> smallIconLocation;
   public static final int FULL_ICON_SIZE = 32;
   public static final int COMPACT_ICON_SIZE = 19;
   protected long sinceLastClick;

   public ModListEntry(Mod mod, ModListWidget list) {
      this.mod = mod;
      this.list = list;
      this.client = Minecraft.getInstance();
   }

   public Component getNarration() {
      return Component.literal(this.mod.getTranslatedName());
   }

   public void render(GuiGraphicsExtractor guiGraphics, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float delta) {
      this.renderEntry(guiGraphics, index, y, x, itemWidth, itemHeight, mouseX, mouseY, hovered, delta);
   }

   public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
      int index = this.list.children().indexOf(this);
      int y = this.list.getRowTop(index);
      int x = this.list.getRowLeft();
      int rowWidth = this.list.getRowWidth();
      int rowHeight = this.list.itemHeight;
      this.renderEntry(guiGraphics, index, y, x, rowWidth, rowHeight, mouseX, mouseY, hovered, delta);
   }

   public void renderEntry(GuiGraphicsExtractor guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float delta) {
      x += this.getXOffset();
      rowWidth -= this.getXOffset();
      int iconSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 19 : 32;
      String modId = this.mod.getId();
      if ("java".equals(modId)) {
         DrawingUtil.drawRandomVersionBackground(this.mod, guiGraphics, x, y, iconSize, iconSize);
      }

      Tuple<Identifier, Dimension> iconData = this.getIconTexture();
      Identifier iconTexture = (Identifier)iconData.getA();
      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, iconTexture, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
      Component name = Component.literal(this.mod.getTranslatedName());
      FormattedText trimmedName = name;
      int maxNameWidth = rowWidth - iconSize - 3;
      Font font = this.client.font;
      if (font.width(name) > maxNameWidth) {
         FormattedText ellipsis = FormattedText.of("...");
         trimmedName = FormattedText.composite(new FormattedText[]{font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis});
      }

      guiGraphics.text(font, Language.getInstance().getVisualOrder((FormattedText)trimmedName), x + iconSize + 3, y + 1, -1, true);
      int updateBadgeXOffset = 0;
      if (!(Boolean)ModMenu.getConfig().HIDE_BADGES.get()) {
         (new ModBadgeRenderer(x + iconSize + 3 + font.width(name) + 2 + updateBadgeXOffset, y, x + rowWidth, this.mod, this.list.getParent())).draw(guiGraphics);
      }

      int var10002;
      if (!(Boolean)ModMenu.getConfig().COMPACT_LIST.get()) {
         String summary = this.mod.getSummary();
         var10002 = x + iconSize + 3 + 4;
         Objects.requireNonNull(this.client.font);
         DrawingUtil.drawWrappedString(guiGraphics, summary, var10002, y + 9 + 2, rowWidth - iconSize - 7, 2, -8355712);
      } else {
         String var10001 = this.mod.getPrefixedVersion();
         var10002 = x + iconSize + 3;
         Objects.requireNonNull(this.client.font);
         DrawingUtil.drawWrappedString(guiGraphics, var10001, var10002, y + 9 + 2, rowWidth - iconSize - 7, 2, -8355712);
      }

      if (!(this instanceof ParentEntry) && !(this instanceof ChildParentEntry) && (Boolean)ModMenu.getConfig().QUICK_CONFIGURE.get() && (this.list.getParent().getModHasConfigScreen(this.mod.getContainer()) || this.list.getParent().modScreenErrors.containsKey(modId))) {
         int textureSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 152 : 256;
         if ((Boolean)this.client.options.touchscreen().get() || hovered) {
            guiGraphics.fill(x, y, x + iconSize, y + iconSize, -1601138544);
            boolean hoveringIcon = mouseX - x < iconSize;
            if (this.list.getParent().modScreenErrors.containsKey(modId)) {
               guiGraphics.blit(RenderPipelines.GUI_TEXTURED, hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
               if (hoveringIcon) {
                  Throwable e = (Throwable)this.list.getParent().modScreenErrors.get(modId);
                  this.list.getParent().setTooltipForNextRenderPass(this.client.font.split(ModMenuScreenTexts.configureError(modId, e), 175));
               }
            } else {
               int v = hoveringIcon ? iconSize : 0;
               guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MOD_CONFIGURATION_ICON, x, y, 0.0F, (float)v, iconSize, iconSize, textureSize, textureSize);
            }
         }
      }

   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      this.list.select(this);
      if ((Boolean)ModMenu.getConfig().QUICK_CONFIGURE.get() && this.list.getParent().getModHasConfigScreen(this.mod.getContainer())) {
         int iconSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 19 : 32;
         if (mouseX - (double)this.list.getRowLeft() <= (double)(iconSize + this.getXOffset())) {
            this.openConfig();
         } else if (doubleClick || Util.getMillis() - this.sinceLastClick < 250L) {
            this.openConfig();
         }
      }

      this.sinceLastClick = Util.getMillis();
      return true;
   }

   public boolean legacyKeyPressed(int keyCode, int scanCode, int modifiers) {
      return false;
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

   private static double extractDouble(Object ev, String... names) {
      if (ev == null) {
         return Double.NaN;
      } else {
         String[] var2 = names;
         int var3 = names.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String n = var2[var4];

            try {
               Method m = ev.getClass().getMethod(n);
               m.setAccessible(true);
               Object val = m.invoke(ev);
               if (val instanceof Double) {
                  return (Double)val;
               }

               if (val instanceof Float) {
                  return ((Float)val).doubleValue();
               }

               if (val instanceof Integer) {
                  return ((Integer)val).doubleValue();
               }

               if (val instanceof Number) {
                  return ((Number)val).doubleValue();
               }
            } catch (NoSuchMethodException var8) {
            } catch (Throwable var9) {
            }
         }

         return Double.NaN;
      }
   }

   public void openConfig() {
      this.mod.getContainer().ifPresent((container) -> {
         this.list.getParent().safelyOpenConfigScreen(container);
      });
   }

   public Mod getMod() {
      return this.mod;
   }

   public Tuple<Identifier, Dimension> getIconTexture() {
      try {
         String caller = Thread.currentThread().getStackTrace()[2].getMethodName();
         //ModMenu.LOGGER.debug("getIconTexture() called for mod {} from {}", this.mod.getId(), caller);
      } catch (Throwable var3) {
      }

      if (ModMenu.shouldResetCache) {
         this.smallIconLocation = null;
         this.iconLocation = null;
         ModMenu.shouldResetCache = false;
      }

      if (this.iconLocation == null) {
         String var10004 = this.mod.getId();
         this.iconLocation = new Tuple(Identifier.fromNamespaceAndPath("modmenu", var10004 + "_icon"), new Dimension());
         ModMenu.LOGGER.debug("ModListEntry.getIconTexture(): requesting icon for mod {}", this.mod.getId());
         Tuple<DynamicTexture, Dimension> icon = this.mod.getIcon(this.list.getNeoforgeIconHandler(), 64 * (Integer)this.client.options.guiScale().get(), false);
         ModMenu.LOGGER.debug("ModListEntry.getIconTexture(): received icon={} for mod {}", icon == null ? "<null>" : "<texture>", this.mod.getId());
         if (icon != null) {
            float multiplier = 32.0F / (float)((Dimension)icon.getB()).height;
            this.iconLocation.setB(new Dimension((int)((float)((Dimension)icon.getB()).width * multiplier), (int)((float)((Dimension)icon.getB()).height * multiplier)));
            this.client.getTextureManager().register((Identifier)this.iconLocation.getA(), (AbstractTexture)icon.getA());
            ((DynamicTexture)icon.getA()).upload();
         } else {
            this.iconLocation.setA(UNKNOWN_ICON);
            this.iconLocation.setB(new Dimension(32, 32));
         }
      }

      return this.iconLocation;
   }

   public Tuple<Identifier, Dimension> getSquaredIconTexture() {
      Tuple<Identifier, Dimension> icon = new Tuple((Identifier)this.getIconTexture().getA(), ((Dimension)this.iconLocation.getB()).getSize());
      float iconSize = (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 19.0F : 32.0F;
      float biggerValue = (float)Math.max(((Dimension)icon.getB()).width, ((Dimension)icon.getB()).height);
      ((Dimension)icon.getB()).setSize((double)((float)((Dimension)icon.getB()).width / biggerValue * iconSize), (double)((float)((Dimension)icon.getB()).height / biggerValue * iconSize));
      return icon;
   }

   public Tuple<Identifier, Dimension> getSquareIconTexture() {
      try {
         String caller = Thread.currentThread().getStackTrace()[2].getMethodName();
         ModMenu.LOGGER.debug("getSquareIconTexture() called for mod {} from {}", this.mod.getId(), caller);
      } catch (Throwable var2) {
      }

      if (this.smallIconLocation == null) {
         String var10004 = this.mod.getId();
         this.smallIconLocation = new Tuple(Identifier.fromNamespaceAndPath("modmenu", var10004 + "_icon_small"), new Dimension());
         ModMenu.LOGGER.debug("ModListEntry.getSquareIconTexture(): requesting small icon for mod {}", this.mod.getId());
         Tuple<DynamicTexture, Dimension> icon = this.mod.getIcon(this.list.getNeoforgeIconHandler(), 64 * (Integer)this.client.options.guiScale().get(), true);
         ModMenu.LOGGER.debug("ModListEntry.getSquareIconTexture(): received small icon={} for mod {}", icon == null ? "<null>" : "<texture>", this.mod.getId());
         if (icon != null) {
            this.smallIconLocation.setB(new Dimension(((Dimension)icon.getB()).width, ((Dimension)icon.getB()).height));
            this.client.getTextureManager().register((Identifier)this.smallIconLocation.getA(), (AbstractTexture)icon.getA());
         } else {
            this.smallIconLocation = this.getSquaredIconTexture();
         }
      }

      return this.smallIconLocation;
   }

   public int getXOffset() {
      return 0;
   }

   public String toString() {
      return "ModListEntry{mod_id=\"" + this.getMod().getId() + "\"}";
   }
}
