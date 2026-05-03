package com.terraformersmc.modmenu.util.mod.neoforge;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.Mod.ModMenuData.DummyParentData;
import java.awt.Dimension;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFile.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoforgeMod implements Mod {
   private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeMod");
   protected final ModContainer container;
   protected final IModInfo modInfo;
   protected final ModMenuData modMenuData;
   protected final Set<ModBadge> badges;
   protected final LinkedHashSet<String> badgeNames = new LinkedHashSet();
   protected final Map<String, String> links = new HashMap();
   protected final List<String> contributors = new ArrayList();
   protected final List<String> authors = new ArrayList();
   protected boolean defaultIconWarning = true;
   protected boolean childHasUpdate = false;
   protected String sources;
   protected String issueTrackerUrl;
   protected String website;

   public NeoforgeMod(ModContainer modContainer) {
      this.container = modContainer;
      this.modInfo = modContainer.getModInfo();
      String id = this.modInfo.getModId();
      ModFileInfo modFileInfo = (ModFileInfo)this.modInfo.getOwningFile();
      this.issueTrackerUrl = (String)this.modInfo.getConfig().getConfigElement(new String[]{"issueTrackerURL"}).orElse((Object)null);
      if (this.issueTrackerUrl == null) {
         this.issueTrackerUrl = (String)modFileInfo.getConfig().getConfigElement(new String[]{"issueTrackerURL"}).orElse((Object)null);
      }

      this.website = (String)this.modInfo.getConfig().getConfigElement(new String[]{"displayURL"}).orElse((Object)null);
      Optional<String> parentId = Optional.empty();
      DummyParentData parentData = null;
      Optional<Map<String, Object>> modMenuValue = modFileInfo.getConfigElement(new String[]{"modproperties", "modmenu"});
      if (modMenuValue.isPresent()) {
         Map<String, Object> modMenuMap = (Map)modMenuValue.get();
         Optional<Map<String, Object>> parentValues = modFileInfo.getConfigElement(new String[]{"modproperties", "modmenu_parent"});
         if (parentValues.isPresent() && !((Map)parentValues.get()).isEmpty()) {
            Set<String> parentBadges = new LinkedHashSet();
            Object var11 = ((Map)parentValues.get()).get("badges");
            if (var11 instanceof ArrayList) {
               ArrayList<?> list = (ArrayList)var11;
               parentBadges.addAll((Collection) list);
            }

            try {
               parentId = Optional.of((String)((Map)parentValues.get()).get("id"));
               String var10002 = (String)parentId.orElseThrow(() -> {
                  return new RuntimeException("Parent object lacks an id");
               });
               Optional var10003 = Optional.of((String)((Map)parentValues.get()).get("name"));
               String var10004 = String.valueOf(((Map)parentValues.get()).get("description"));
               parentData = new DummyParentData(var10002, var10003, Optional.of(var10004 + "\n" + String.valueOf(this.modInfo.getConfig().getConfigElement(new String[]{"credits"}).orElse(""))), Optional.of((String)((Map)parentValues.get()).get("icon")), parentBadges);
               if (((String)parentId.orElse("")).equals(id)) {
                  parentId = Optional.empty();
                  parentData = null;
                  throw new RuntimeException("Mod declared itself as its own parent");
               }
            } catch (Throwable var12) {
               LOGGER.error("Error loading parent data from mod: " + id, var12);
            }
         }

         Object var19 = modMenuMap.get("badges");
         ArrayList list;
         if (var19 instanceof ArrayList) {
            list = (ArrayList)var19;
            this.badgeNames.addAll(list);
         }

         var19 = modMenuMap.get("links");
         if (var19 instanceof ArrayList) {
            list = (ArrayList)var19;
            list.forEach((stringx) -> {
               String[] strings = stringx.toString().split("=");
               this.links.put(strings[0], strings[1]);
            });
         }

         var19 = modMenuMap.get("contributors");
         if (var19 instanceof ArrayList) {
            list = (ArrayList)var19;
            this.contributors.addAll(list);
         }

         this.sources = (String)modMenuMap.getOrDefault("sources", "");
      }

      String[] var13 = this.modInfo.getConfig().getConfigElement(new String[]{"authors"}).orElse("").toString().split(", ");
      int var15 = var13.length;

      for(int var18 = 0; var18 < var15; ++var18) {
         String string = var13[var18];
         if (!string.isEmpty()) {
            if (string.contains(",")) {
               this.authors.addAll(Arrays.stream(string.split(",")).toList());
            }

            this.authors.add(string);
         }
      }

      this.modMenuData = new ModMenuData(parentId, parentData, id);
      if (id.startsWith("fabric")) {
         this.modMenuData.fillParentIfEmpty("fabric-api");
         this.badgeNames.add("library");
      }

      if (id.startsWith("connectorextras")) {
         this.modMenuData.fillParentIfEmpty("connector");
         this.badgeNames.add("library");
      }

      this.badges = this.modMenuData.getBadges();
      IModFile parent = modFileInfo.getFile().getDiscoveryAttributes().parent();
      if (parent != null && parent.getType() != Type.LIBRARY) {
         this.badgeNames.add("library");
      }

      boolean isClientSide = false;

      for(Iterator var20 = modFileInfo.getFile().getScanResult().getAnnotatedBy(net.neoforged.fml.common.Mod.class, ElementType.TYPE).toList().iterator(); var20.hasNext(); isClientSide = true) {
         AnnotationData data = (AnnotationData)var20.next();
         EnumSet<Dist> dist = AutomaticEventSubscriber.getSides(data.annotationData().get("dist"));
         if (dist.contains(Dist.DEDICATED_SERVER)) {
            isClientSide = false;
            break;
         }
      }

      if ("minecraft".equals(id)) {
         this.badgeNames.add("minecraft");
      } else if (isClientSide) {
         this.badgeNames.add("client");
      }

   }

   public Optional<ModContainer> getContainer() {
      return Optional.ofNullable(this.container);
   }

   @NotNull
   public String getId() {
      return this.modInfo.getModId();
   }

   @NotNull
   public String getName() {
      return this.modInfo.getDisplayName();
   }

   @NotNull
   public Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
      String iconSourceId = this.getId();
      String iconResourceId = iconSourceId + (isSmall ? "_small" : "");
      if (NeoforgeIconHandler.modResourceIconCache.containsKey(iconResourceId)) {
         return (Tuple)NeoforgeIconHandler.modResourceIconCache.get(iconResourceId);
      } else {
         String iconPath = (String)this.modInfo.getLogoFile().orElse("assets/" + this.getId() + "/icon.png");
         if (isSmall) {
            String catalogueIcon;
            if ((Boolean)ModMenu.getConfig().USE_CATALOGUE_ICON.get() && (catalogueIcon = (String)((ModInfo)this.modInfo).getConfigElement(new String[]{"catalogueImageIcon"}).orElse((Object)null)) != null) {
               iconPath = catalogueIcon;
            } else {
               iconPath = iconPath.replace(".png", "_small.png");
            }
         }

         if ("minecraft".equals(this.getId())) {
            iconSourceId = "modmenu";
            iconPath = "assets/modmenu/minecraft_icon.png";
         } else if ("neoforge".equals(this.getId()) && isSmall) {
            iconSourceId = "modmenu";
            iconPath = "assets/modmenu/neoforge.png";
         }

          String finalIconSourceId = iconSourceId;
          ModContainer iconSource = (ModContainer)ModList.get().getModContainerById(iconSourceId).orElseThrow(() -> {
            return new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + finalIconSourceId);
         });
         Tuple<DynamicTexture, Dimension> icon = iconHandler.createIcon(iconSource, iconPath);
         if (icon == null && !isSmall) {
            if (this.defaultIconWarning) {
               LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", this.modInfo.getModId());
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
      return this.modInfo.getDescription();
   }

   @NotNull
   public String getTranslatedDescription() {
      return super.toString();
   }

   @NotNull
   public String getVersion() {
      return this.modInfo.getVersion().toString();
   }

   @NotNull
   public String getPrefixedVersion() {
      return VersionUtil.getPrefixedVersion(this.getVersion());
   }

   @NotNull
   public List<String> getAuthors() {
      return (List)(this.authors.isEmpty() && "minecraft".equals(this.getId()) ? Lists.newArrayList(new String[]{"Mojang Studios"}) : this.authors);
   }

   @NotNull
   public Map<String, Collection<String>> getContributors() {
      Map<String, Collection<String>> contributors = new LinkedHashMap();
      Iterator var2 = this.contributors.iterator();

      while(var2.hasNext()) {
         String contributor = (String)var2.next();
         contributors.put(contributor, List.of("Contributor"));
      }

      return contributors;
   }

   @NotNull
   public SortedMap<String, Set<String>> getCredits() {
      SortedMap<String, Set<String>> credits = new TreeMap();
      List<String> authors = this.getAuthors();
      Map<String, Collection<String>> contributors = this.getContributors();
      Iterator var4 = authors.iterator();

      while(var4.hasNext()) {
         String author = (String)var4.next();
         contributors.put(author, List.of("Author"));
      }

      var4 = contributors.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, Collection<String>> contributor = (Entry)var4.next();
         Iterator var6 = ((Collection)contributor.getValue()).iterator();

         while(var6.hasNext()) {
            String role = (String)var6.next();
            credits.computeIfAbsent(role, (key) -> {
               return new LinkedHashSet();
            });
            ((Set)credits.get(role)).add((String)contributor.getKey());
         }
      }

      return credits;
   }

   @NotNull
   public Set<ModBadge> getBadges() {
      return this.badges;
   }

   @NotNull
   public Set<String> getBadgeNames() {
      return this.badgeNames;
   }

   @Nullable
   public String getWebsite() {
      return "minecraft".equals(this.getId()) ? "https://www.minecraft.net/" : this.website;
   }

   @Nullable
   public String getIssueTracker() {
      return "minecraft".equals(this.getId()) ? "https://aka.ms/snapshotbugs?ref=game" : this.issueTrackerUrl;
   }

   @Nullable
   public String getSource() {
      return this.sources;
   }

   @Nullable
   public String getParent() {
      return (String)this.modMenuData.getParent().orElse(null);
   }

   @NotNull
   public Set<String> getLicense() {
      return "minecraft".equals(this.getId()) ? Sets.newHashSet(new String[]{"Minecraft EULA"}) : Sets.newHashSet(new String[]{this.modInfo.getOwningFile().getLicense()});
   }

   @NotNull
   public Map<String, String> getLinks() {
      return this.links;
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
}
