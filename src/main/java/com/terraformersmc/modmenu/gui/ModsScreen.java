package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig.Sorting;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModsScreen extends Screen {
   private static final Identifier FILTERS_BUTTON_LOCATION = Identifier.fromNamespaceAndPath("modmenu", "textures/gui/filters_button.png");
   private static final Identifier CONFIGURE_BUTTON_LOCATION = Identifier.fromNamespaceAndPath("modmenu", "textures/gui/configure_button.png");
   public static final Identifier BADGE_BUTTON_LOCATION = Identifier.fromNamespaceAndPath("modmenu", "textures/gui/badge_button.png");
   private static final Logger LOGGER = LoggerFactory.getLogger("modmenu | ModsScreen");
   private final Screen previousScreen;
   private ModListEntry selected;
   private ModBadgeRenderer modBadgeRenderer;
   private double scrollPercent = 0.0D;
   private boolean keepFilterOptionsShown = false;
   private boolean init = false;
   private boolean filterOptionsShown = false;
   protected static final int RIGHT_PANE_Y = 48;
   private int paneWidth;
   private int rightPaneX;
   private int searchBoxX;
   private int filtersX;
   private int filtersWidth;
   private int searchRowWidth;
   public final Set<String> showModChildren = new HashSet();
   private EditBox searchBox;
   @Nullable
   private AbstractWidget filtersButton;
   private AbstractWidget sortingButton;
   private AbstractWidget librariesButton;
   private ModListWidget modList;
   @Nullable
   private AbstractWidget configureButton;
   @Nullable
   private AbstractWidget badgeButton;
   private AbstractWidget websiteButton;
   private AbstractWidget issuesButton;
   private DescriptionListWidget descriptionListWidget;
   private AbstractWidget modsFolderButton;
   private AbstractWidget doneButton;
   public final Map<ModContainer, Boolean> modHasConfigScreen = new HashMap();
   public final Map<String, Throwable> modScreenErrors = new HashMap();
   private static final Component SEND_FEEDBACK_TEXT = Component.translatable("menu.sendFeedback");
   private static final Component REPORT_BUGS_TEXT = Component.translatable("menu.reportBugs");

   public ModsScreen(Screen previousScreen) {
      super(ModMenuScreenTexts.TITLE);
      this.previousScreen = previousScreen;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.modList.isMouseOver(mouseX, mouseY)) {
         return this.modList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      } else {
         return this.descriptionListWidget.isMouseOver(mouseX, mouseY) ? this.descriptionListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) : false;
      }
   }
   protected void init() {
      boolean hideTop = (Boolean)ModMenu.getConfig().HIDE_SCREEN_TOP.get();
      int paneY;
      byte rightPaneY;
      if (hideTop) {
         paneY = 30;
         rightPaneY = 5;
      } else {
         paneY = (Boolean)ModMenu.getConfig().CONFIG_MODE.get() ? 48 : 67;
         rightPaneY = 48;
      }

      this.paneWidth = this.width / 2 - 8;
      this.rightPaneX = this.width - this.paneWidth;
      this.modList = new ModListWidget(this.minecraft, this.paneWidth, this.height - paneY - 36, paneY, (Boolean)ModMenu.getConfig().COMPACT_LIST.get() ? 23 : 36, this.modList, this);
      this.modList.setX(0);
      int filtersButtonSize = (Boolean)ModMenu.getConfig().CONFIG_MODE.get() ? 0 : 22;
      int searchWidthMax = this.paneWidth - 32 - filtersButtonSize;
      int searchBoxWidth = (Boolean)ModMenu.getConfig().CONFIG_MODE.get() ? Math.min(200, searchWidthMax) : searchWidthMax;
      this.searchBoxX = this.paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
      this.searchBox = new EditBox(this.font, this.searchBoxX, 22, searchBoxWidth, 20, this.searchBox, ModMenuScreenTexts.SEARCH);
      if ((Boolean)ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
         this.searchBox.visible = false;
         this.searchBox.active = false;
      }

      this.searchBox.setResponder((text) -> {
         this.modList.filter(text, false);
      });
      Component sortingText = ModMenuScreenTexts.getSortingComponent();
      Component librariesText = ModMenuScreenTexts.getLibrariesComponent();
      int sortingWidth = this.font.width(sortingText) + 20;
      int librariesWidth = this.font.width(librariesText) + 20;
      this.filtersWidth = librariesWidth + sortingWidth + 2;
      this.searchRowWidth = this.searchBoxX + searchBoxWidth + 22;
      this.updateFiltersX(true);
      if (!(Boolean)ModMenu.getConfig().CONFIG_MODE.get()) {
         this.filtersButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS, (button) -> {
            this.setFilterOptionsShown(!this.filterOptionsShown);
         }).position(this.paneWidth / 2 + searchBoxWidth / 2 - 10 + 2, 22).size(20, 20).uv(0, 0, 20).texture(FILTERS_BUTTON_LOCATION, 32, 64).build();
         this.filtersButton.setTooltip(Tooltip.create(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS));
         if ((Boolean)ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
            this.filtersButton.visible = false;
            this.filtersButton.active = false;
         }
      }

      this.sortingButton = Button.builder(sortingText, (button) -> {
         ((Sorting)ModMenu.getConfig().SORTING.get()).cycleValue();
         ((ModConfigSpec)ModMenu.CONFIG.getRight()).save();
         this.modList.reloadFilters();
         button.setMessage(ModMenuScreenTexts.getSortingComponent());
      }).pos(this.filtersX, 45).size(sortingWidth, 20).build();
      this.librariesButton = Button.builder(librariesText, (button) -> {
         ModMenu.getConfig().SHOW_LIBRARIES.set(!(Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get());
         ((ModConfigSpec)ModMenu.CONFIG.getRight()).save();
         this.modList.reloadFilters();
         button.setMessage(ModMenuScreenTexts.getLibrariesComponent());
      }).pos(this.filtersX + sortingWidth + 2, 45).size(librariesWidth, 20).build();
      if (!(Boolean)ModMenu.getConfig().HIDE_CONFIG_BUTTONS.get()) {
         this.configureButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, (button) -> {
            Mod mod = ((ModListEntry)Objects.requireNonNull(this.selected)).getMod();
            if (this.getModHasConfigScreen(mod.getContainer())) {
               this.safelyOpenConfigScreen((ModContainer)mod.getContainer().get());
            } else {
               button.active = false;
            }

         }).position(this.width - 24, rightPaneY).size(20, 20).uv(0, 0, 20).texture(CONFIGURE_BUTTON_LOCATION, 32, 64).build();
      }

      if (!(Boolean)ModMenu.getConfig().HIDE_BADGE_BUTTONS.get()) {
         this.badgeButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, (button) -> {
            this.minecraft.pushGuiLayer(new BadgeScreen(this.selected.mod, this.paneWidth, searchBoxWidth));
         }).position(this.paneWidth / 2 + searchBoxWidth / 2 - 10 + 26, 22).size(20, 20).uv(0, 0, 20).texture(BADGE_BUTTON_LOCATION, 32, 64).build();
      }

      int urlButtonWidths = this.paneWidth / 2 - 2;
      int cappedButtonWidth = Math.min(urlButtonWidths, 200);
      this.websiteButton = Button.builder(ModMenuScreenTexts.WEBSITE, (button) -> {
         Mod mod = ((ModListEntry)Objects.requireNonNull(this.selected)).getMod();
         boolean isMinecraft = this.selected.getMod().getId().equals("minecraft");
         if (isMinecraft) {
            boolean stable = false;

            try {
               Object current = SharedConstants.getCurrentVersion();
               Method m = current.getClass().getMethod("isStable");
               stable = (Boolean)m.invoke(current);
            } catch (Exception var7) {
            }

            String urlx = stable ? CommonLinks.RELEASE_FEEDBACK.toString() : CommonLinks.SNAPSHOT_FEEDBACK.toString();
            ConfirmLinkScreen.confirmLinkNow(this, urlx, true);
         } else {
            String url = mod.getWebsite();
            ConfirmLinkScreen.confirmLinkNow(this, url, false);
         }

      }).pos(this.rightPaneX + urlButtonWidths / 2 - cappedButtonWidth / 2, rightPaneY + 36).size(Math.min(urlButtonWidths, 200), 20).build();
      this.issuesButton = Button.builder(ModMenuScreenTexts.ISSUES, (button) -> {
         Mod mod = ((ModListEntry)Objects.requireNonNull(this.selected)).getMod();
         boolean isMinecraft = this.selected.getMod().getId().equals("minecraft");
         if (isMinecraft) {
            ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK, true);
         } else {
            String url = mod.getIssueTracker();
            ConfirmLinkScreen.confirmLinkNow(this, url, false);
         }

      }).pos(this.rightPaneX + urlButtonWidths + 4 + urlButtonWidths / 2 - cappedButtonWidth / 2, rightPaneY + 36).size(Math.min(urlButtonWidths, 200), 20).build();
      Minecraft var10003 = this.minecraft;
      int var10004 = this.paneWidth;
      int var10005 = this.height - rightPaneY - 96;
      int var10006 = rightPaneY + 60;
      Objects.requireNonNull(this.font);
      this.descriptionListWidget = new DescriptionListWidget(var10003, var10004, var10005, var10006, 9 + 1, this.descriptionListWidget, this);
      this.descriptionListWidget.setX(this.rightPaneX);
      this.modsFolderButton = Button.builder(ModMenuScreenTexts.MODS_FOLDER, (button) -> {
         Util.getPlatform().openUri(FMLPaths.MODSDIR.get().toUri());
      }).pos(this.width / 2 - 154, this.height - 28).size(150, 20).build();
      this.doneButton = Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.minecraft.setScreen(this.previousScreen);
      }).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build();
      this.modList.finalizeInit();
      this.setFilterOptionsShown(this.keepFilterOptionsShown && this.filterOptionsShown);
      this.addWidget(this.searchBox);
      this.setInitialFocus(this.searchBox);
      if (this.filtersButton != null) {
         this.addRenderableWidget(this.filtersButton);
      }

      this.addRenderableWidget(this.sortingButton);
      this.addRenderableWidget(this.librariesButton);
      this.addWidget(this.modList);
      if (this.configureButton != null) {
         this.addRenderableWidget(this.configureButton);
      }

      if (this.badgeButton != null) {
         this.addRenderableWidget(this.badgeButton);
      }

      this.addRenderableWidget(this.websiteButton);
      this.addRenderableWidget(this.issuesButton);
      this.addWidget(this.descriptionListWidget);
      this.addRenderableWidget(this.modsFolderButton);
      this.addRenderableWidget(this.doneButton);
      this.init = true;
      this.keepFilterOptionsShown = true;
   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
      super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
      boolean hideTop = ModMenu.getConfig().HIDE_SCREEN_TOP.get();
      int rightPaneY = hideTop ? 5 : 48;
      ModListEntry selectedEntry = this.selected;
      if (selectedEntry != null) {
         this.descriptionListWidget.extractRenderState(guiGraphics, mouseX, mouseY, delta);
         this.descriptionListWidget.renderListItems(guiGraphics, mouseX, mouseY, delta);
      }

      this.modList.extractRenderState(guiGraphics, mouseX, mouseY, delta);
      /*this is the reason why the scrolling wasn't working. make sure this is called.*/
      this.modList.renderListItems(guiGraphics, mouseX, mouseY, delta);
      this.searchBox.extractRenderState(guiGraphics, mouseX, mouseY, delta);
      int rightPaneCenterX;
      int x;
      int lineSpacing;
      if (!hideTop) {
         int neoForgeOrange = -29696;
         guiGraphics.text(this.font, this.title, this.modList.getWidth() / 2 - this.font.width(this.title) / 2, 8, neoForgeOrange, true);
         rightPaneCenterX = this.modList.getWidth() / 2;
         x = this.font.width(this.title.getString());
         int left = rightPaneCenterX - x / 2 - 6;
         int right = rightPaneCenterX + x / 2 + 6;
         int underlineTop = 18;
         lineSpacing = underlineTop + 3;
         guiGraphics.fill(left, underlineTop, right, lineSpacing, neoForgeOrange);
      }

      assert this.minecraft != null;

      int grayColor = -5592406;
      if (!(Boolean)ModMenu.getConfig().DISABLE_DRAG_AND_DROP.get() && !hideTop) {
         rightPaneCenterX = this.width - this.modList.getWidth() / 2;
         Font var10001 = this.font;
         Component var10002 = ModMenuScreenTexts.DROP_INFO_LINE_1;
         int var10003 = rightPaneCenterX - this.font.width(ModMenuScreenTexts.DROP_INFO_LINE_1) / 2;
         Objects.requireNonNull(this.minecraft.font);
         guiGraphics.text(var10001, var10002, var10003, 24 - 9 - 1, grayColor, true);
         guiGraphics.text(this.font, ModMenuScreenTexts.DROP_INFO_LINE_2, rightPaneCenterX - this.font.width(ModMenuScreenTexts.DROP_INFO_LINE_2) / 2, 25, grayColor, true);
      }

      if (!(Boolean)ModMenu.getConfig().CONFIG_MODE.get()) {
         Component fullModCount = this.computeModCountText(true, false);
         if (!(Boolean)ModMenu.getConfig().CONFIG_MODE.get() && this.updateFiltersX(false)) {
            byte showingModTextY;
            if (hideTop) {
               showingModTextY = 6;
            } else {
               showingModTextY = 46;
            }

            if ((Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() && this.font.width(fullModCount) > (this.filterOptionsShown ? this.filtersX : this.modList.getWidth()) - 5) {
               guiGraphics.text(this.font, this.computeModCountText(false, false), this.searchBoxX, showingModTextY, -1, true);
               guiGraphics.text(this.font, this.computeLibraryCountText(false), this.searchBoxX, showingModTextY + 11, -1, true);
            } else {
               guiGraphics.text(this.font, fullModCount, this.searchBoxX, showingModTextY + 6, -1, true);
            }
         }
      }

      if (selectedEntry != null) {
         Mod mod = selectedEntry.getMod();
         x = this.rightPaneX;
         if ("java".equals(mod.getId())) {
            DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, rightPaneY, 32, 32);
         }

         Tuple<Identifier, Dimension> iconProperties = selectedEntry.getIconTexture();
         int iconDisplaySize = 32;
         guiGraphics.blit(RenderPipelines.GUI_TEXTURED, (Identifier)iconProperties.getA(), x, rightPaneY, 0.0F, 0.0F, iconDisplaySize, iconDisplaySize, iconDisplaySize, iconDisplaySize);
         int imageOffset = iconDisplaySize + 4;
         Objects.requireNonNull(this.font);
         lineSpacing = 9 + 1;
         Component name = Component.literal(mod.getTranslatedName());
         FormattedText trimmedName = name;
         int maxNameWidth = this.width - (x + imageOffset) - 10;
         if (this.font.width(name) > maxNameWidth) {
            FormattedText ellipsis = FormattedText.of("...");
            trimmedName = FormattedText.composite(new FormattedText[]{this.font.substrByWidth(name, maxNameWidth - this.font.width(ellipsis)), ellipsis});
         }

         int neoForgeOrange = -29696;
         int nameColor = mod.getId().equals("modmenu") ? neoForgeOrange : -1;
         guiGraphics.text(this.font, Language.getInstance().getVisualOrder((FormattedText)trimmedName), x + imageOffset, rightPaneY + 1, nameColor, true);
         if (mouseX > x + imageOffset && mouseY > rightPaneY + 1) {
            int var29 = rightPaneY + 1;
            Objects.requireNonNull(this.font);
            if (mouseY < var29 + 9 && mouseX < x + imageOffset + this.font.width((FormattedText)trimmedName)) {
               this.setTooltipForNextRenderPass(ModMenuScreenTexts.modIdTooltip(mod.getId()));
            }
         }

         if (this.init || this.modBadgeRenderer == null || this.modBadgeRenderer.getMod() != mod) {
            this.modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + this.minecraft.font.width((FormattedText)trimmedName) + 2, rightPaneY, this.width - 28, selectedEntry.mod, this);
            this.init = false;
         }

         if (!(Boolean)ModMenu.getConfig().HIDE_BADGES.get()) {
            this.modBadgeRenderer.draw(guiGraphics);
         }

         if (mod.isReal()) {
            int versionColor = mod.getId().equals("modmenu") ? -19584 : -8355712;
            guiGraphics.text(this.font, mod.getPrefixedVersion(), x + imageOffset, rightPaneY + 2 + lineSpacing, versionColor, true);
         }

         List<String> names = mod.getAuthors();
         if (!names.isEmpty()) {
            String authors;
            if (names.size() > 1) {
               authors = Joiner.on(", ").join(names);
            } else {
               authors = (String)names.getFirst();
            }

            DrawingUtil.drawWrappedString(guiGraphics, I18n.get("modmenu.authorPrefix", new Object[]{authors}), x + imageOffset, rightPaneY + 2 + lineSpacing * 2, this.paneWidth - imageOffset - 4, 1, -8355712);
         }
      }

   }

   private Component computeModCountText(boolean includeLibs, boolean onInit) {
      int[] rootMods = this.formatModCount((Set)ModMenu.ROOT_MODS.values().stream().filter((mod) -> {
         return !mod.isHidden() && !mod.getBadges().contains(ModBadge.LIBRARY);
      }).map(Mod::getId).collect(Collectors.toSet()), onInit);
      if (includeLibs && (Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() && !onInit) {
         int[] rootLibs = this.formatModCount((Set)ModMenu.ROOT_MODS.values().stream().filter((mod) -> {
            return !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY);
         }).map(Mod::getId).collect(Collectors.toSet()), false);
         return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", new int[][]{rootMods, rootLibs});
      } else {
         return TranslationUtil.translateNumeric("modmenu.showingMods", new int[][]{rootMods});
      }
   }

   private Component computeLibraryCountText(boolean onInit) {
      if ((Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() && !onInit) {
         int[] rootLibs = this.formatModCount((Set)ModMenu.ROOT_MODS.values().stream().filter((mod) -> {
            return !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY);
         }).map(Mod::getId).collect(Collectors.toSet()), false);
         return TranslationUtil.translateNumeric("modmenu.showingLibraries", new int[][]{rootLibs});
      } else {
         return Component.empty();
      }
   }

   private int[] formatModCount(Set<String> set, boolean allVisible) {
      int visible = this.modList.getDisplayedCountFor(set);
      int total = set.size();
      return visible != total && !allVisible ? new int[]{visible, total} : new int[]{total};
   }

   public void onClose() {
      this.modList.close();
      this.minecraft.setScreen(this.previousScreen);
   }

   private void setFilterOptionsShown(boolean filterOptionsShown) {
      this.filterOptionsShown = filterOptionsShown;
      this.sortingButton.visible = filterOptionsShown;
      this.librariesButton.visible = filterOptionsShown;
   }

   public ModListEntry getSelectedEntry() {
      return this.selected;
   }

   public void updateSelectedEntry(ModListEntry entry) {
      if (entry != null) {
         this.selected = entry;
         Mod mod = entry.getMod();
         String modId = mod.getId();

         try {
            Minecraft.getInstance().execute(() -> {
               try {
                  try {
                     this.descriptionListWidget.updateSelectedModIfRequired(mod);
                  } catch (Throwable var5) {
                  }

                  if (this.configureButton != null) {
                     try {
                        this.configureButton.active = this.getModHasConfigScreen(mod.getContainer());
                        this.configureButton.visible = this.selected != null && this.getModHasConfigScreen(mod.getContainer()) || this.modScreenErrors.containsKey(modId);
                        if (this.modScreenErrors.containsKey(modId)) {
                           Throwable e = (Throwable)this.modScreenErrors.get(modId);
                           this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.configureError(modId, e)));
                        } else {
                           this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.CONFIGURE));
                        }
                     } catch (Throwable var7) {
                     }
                  }

                  boolean isMinecraft = modId.equals("minecraft");

                  try {
                     this.websiteButton.setMessage(isMinecraft ? SEND_FEEDBACK_TEXT : ModMenuScreenTexts.WEBSITE);
                     this.issuesButton.setMessage(isMinecraft ? REPORT_BUGS_TEXT : ModMenuScreenTexts.ISSUES);
                     this.websiteButton.visible = true;
                     this.websiteButton.active = isMinecraft || mod.getWebsite() != null;
                     this.issuesButton.visible = true;
                     this.issuesButton.active = isMinecraft || mod.getIssueTracker() != null;
                  } catch (Throwable var6) {
                  }
               } catch (Throwable var8) {
                  LOGGER.warn("Deferred updateSelectedEntry task failed", var8);
               }

            });
         } catch (Throwable var7) {
            LOGGER.warn("Failed to defer updateSelectedEntry, falling back to immediate update", var7);

            try {
               this.descriptionListWidget.updateSelectedModIfRequired(mod);
            } catch (Throwable var6) {
            }
         }

      }
   }

   public double getScrollPercent() {
      return this.scrollPercent;
   }

   public void updateScrollPercent(double scrollPercent) {
      this.scrollPercent = scrollPercent;
   }

   public String getSearchInput() {
      return this.searchBox.getValue();
   }

   private boolean updateFiltersX(boolean onInit) {
      if (this.filtersWidth + this.font.width(this.computeModCountText(true, onInit)) + 20 >= this.searchRowWidth && (this.filtersWidth + this.font.width(this.computeModCountText(false, onInit)) + 20 >= this.searchRowWidth || this.filtersWidth + this.font.width(this.computeLibraryCountText(onInit)) + 20 >= this.searchRowWidth)) {
         this.filtersX = this.paneWidth / 2 - this.filtersWidth / 2;
         return !this.filterOptionsShown;
      } else {
         this.filtersX = this.searchRowWidth - this.filtersWidth + 1;
         return true;
      }
   }

   public void onFilesDrop(List<Path> paths) {
      Path modsDirectory = FMLPaths.MODSDIR.get();
      List<Path> mods = (List)paths.stream().filter(ModsScreen::isMod).collect(Collectors.toList());
      if (!mods.isEmpty()) {
         String modList = (String)mods.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
         this.minecraft.setScreen(new ConfirmScreen((value) -> {
            if (value) {
               boolean allSuccessful = true;
               Iterator var5 = mods.iterator();

               while(var5.hasNext()) {
                  Path path = (Path)var5.next();

                  try {
                     Files.copy(path, modsDirectory.resolve(path.getFileName()));
                  } catch (IOException var16) {
                     LOGGER.warn("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName()));
                     SystemToast.onPackCopyFailure(this.minecraft, path.toString());
                     allSuccessful = false;
                     break;
                  }
               }

               if (allSuccessful) {
                  try {
                     Object toasts = null;

                     try {
                        Method gt = Minecraft.class.getMethod("getToasts");
                        toasts = gt.invoke(this.minecraft);
                     } catch (NoSuchMethodException var12) {
                     }

                     Method[] var19 = SystemToast.class.getMethods();
                     int var7 = var19.length;

                     for(int var8 = 0; var8 < var7; ++var8) {
                        Method m = var19[var8];
                        if (m.getName().equals("add")) {
                           try {
                              m.invoke((Object)null, toasts, SystemToastId.PERIODIC_NOTIFICATION, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_1, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_2);
                              break;
                           } catch (IllegalArgumentException var14) {
                              try {
                                 m.invoke((Object)null, toasts, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_1, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_2);
                                 break;
                              } catch (IllegalArgumentException var13) {
                              }
                           }
                        }
                     }
                  } catch (Throwable var15) {
                  }
               }
            }

            this.minecraft.setScreen(this);
         }, ModMenuScreenTexts.DROP_CONFIRM, Component.literal(modList)));
      }
   }

   private static boolean isFabricMod(Path mod) {
      try {
         JarFile jarFile = new JarFile(mod.toFile());

         boolean var2;
         try {
            var2 = jarFile.getEntry("fabric.mod.json") != null;
         } catch (Throwable var5) {
            try {
               jarFile.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         jarFile.close();
         return var2;
      } catch (UnsupportedOperationException | IOException var6) {
         return false;
      }
   }

   private static boolean isMod(Path mod) {
      return isFabricMod(mod) || isNeoforgeMod(mod);
   }

   private static boolean isNeoforgeMod(Path mod) {
      try {
         JarFile jarFile = new JarFile(mod.toFile());

         boolean var2;
         try {
            var2 = jarFile.getEntry("META-INF/neoforge.mods.toml") != null;
         } catch (Throwable var5) {
            try {
               jarFile.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         jarFile.close();
         return var2;
      } catch (UnsupportedOperationException | IOException var6) {
         return false;
      }
   }

   public boolean getModHasConfigScreen(Optional<ModContainer> containerOptional) {
      if (containerOptional.isEmpty()) {
         return false;
      } else {
         ModContainer container = (ModContainer)containerOptional.get();
         return this.modScreenErrors.containsKey(container.getModId()) ? false : (Boolean)this.modHasConfigScreen.computeIfAbsent(container, ModMenu::hasConfigScreen);
      }
   }

   public void safelyOpenConfigScreen(ModContainer modId) {
      try {
         Screen screen = ModMenu.getConfigScreen(modId, this);
         if (screen != null) {
            this.minecraft.setScreen(screen);
         }
      } catch (NoClassDefFoundError var3) {
         Logger var10000 = LOGGER;
         String var10001 = String.valueOf(modId);
         var10000.warn("The '" + var10001 + "' mod config screen is not available because " + var3.getLocalizedMessage() + " is missing.");
         this.modScreenErrors.put(modId.getModId(), var3);
      } catch (Throwable var4) {
         LOGGER.error("Error from mod '" + String.valueOf(modId) + "'", var4);
         this.modScreenErrors.put(modId.getModId(), var4);
      }

   }

   public void setTooltipForNextRenderPass(Component comp) {
      try {
         Method m = Screen.class.getMethod("setTooltipForNextRenderPass", Component.class);
         m.invoke(this, comp);
      } catch (Throwable var3) {
      }

   }

   public void setTooltipForNextRenderPass(List lines) {
      try {
         Method m = Screen.class.getMethod("setTooltipForNextRenderPass", List.class);
         m.invoke(this, lines);
      } catch (Throwable var3) {
      }

   }
}
