package com.terraformersmc.modmenu.util.mod.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.Mod.ModMenuData.DummyParentData;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaDummyMod implements Mod {
   private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeMod");
   protected final ModMenuData modMenuData;
   private static final String modid = "java";
   protected final Map<String, String> links = new HashMap();
   protected final Set<String> badgeNames = new LinkedHashSet();
   protected boolean defaultIconWarning = true;
   protected boolean allowsUpdateChecks = true;
   protected boolean childHasUpdate = false;

   public JavaDummyMod() {
      this.allowsUpdateChecks = false;
      Optional<String> parentId = Optional.empty();
      this.badgeNames.add("library");
      this.modMenuData = new ModMenuData(parentId, (DummyParentData)null, "java");
   }

   @NotNull
   public String getId() {
      return "java";
   }

   @NotNull
   public String getName() {
      return System.getProperty("java.vm.name");
   }

   @NotNull
   public Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
      String iconSourceId = "modmenu";
      String iconResourceId = iconSourceId + (isSmall ? "_small" : "");
      if (NeoforgeIconHandler.modResourceIconCache.containsKey(iconResourceId)) {
         return (Tuple)NeoforgeIconHandler.modResourceIconCache.get(iconResourceId);
      } else {
         String iconPath = "assets/modmenu/java_icon.png";
         ModContainer iconSource = (ModContainer)ModList.get().getModContainerById(iconSourceId).orElseThrow(() -> {
            return new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + iconSourceId);
         });
         Tuple<DynamicTexture, Dimension> icon = iconHandler.createIcon(iconSource, iconPath);
         if (icon == null) {
            if (this.defaultIconWarning) {
               LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", "java");
               this.defaultIconWarning = false;
            }

            return iconHandler.createIcon((ModContainer)ModList.get().getModContainerById("modmenu").orElseThrow(() -> {
               return new RuntimeException("Cannot get ModContainer for Neoforge mod with id modmenu");
            }), "assets/modmenu/unknown_icon.png");
         } else {
            return icon;
         }
      }
   }

   @NotNull
   public String getDescription() {
      return "java";
   }

   @NotNull
   public String getTranslatedDescription() {
      String description = super.toString();
      description = description + "\n" + I18n.get("modmenu.javaDistributionName", new Object[]{this.getName()});
      return description;
   }

   @NotNull
   public String getVersion() {
      return System.getProperty("java.runtime.version");
   }

   @NotNull
   public String getPrefixedVersion() {
      return VersionUtil.getPrefixedVersion(this.getVersion());
   }

   @NotNull
   public List<String> getAuthors() {
      return Lists.newArrayList(new String[]{System.getProperty("java.vendor")});
   }

   @NotNull
   public Map<String, Collection<String>> getContributors() {
      return Map.of();
   }

   @NotNull
   public SortedMap<String, Set<String>> getCredits() {
      return new TreeMap();
   }

   @NotNull
   public Set<ModBadge> getBadges() {
      return this.modMenuData.getBadges();
   }

   @NotNull
   public Set<String> getBadgeNames() {
      return this.badgeNames;
   }

   @Nullable
   public String getWebsite() {
      return System.getProperty("java.vendor.url");
   }

   @Nullable
   public String getIssueTracker() {
      return null;
   }

   @Nullable
   public String getSource() {
      return null;
   }

   @Nullable
   public String getParent() {
      return null;
   }

   @NotNull
   public Set<String> getLicense() {
      return Sets.newHashSet();
   }

   @NotNull
   public Map<String, String> getLinks() {
      return Map.of();
   }

   public boolean isReal() {
      return true;
   }

   public ModMenuData getModMenuData() {
      return this.modMenuData;
   }

   public boolean getChildHasUpdate() {
      return this.childHasUpdate;
   }

   public void setChildHasUpdate() {
      this.childHasUpdate = true;
   }

   public boolean isHidden() {
      return ((List)ModMenu.getConfig().HIDDEN_MODS.get()).contains(this.getId());
   }

   public Optional<ModContainer> getContainer() {
      return Optional.empty();
   }
}
