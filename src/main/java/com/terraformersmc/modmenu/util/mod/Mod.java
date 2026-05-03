package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.TextPlaceholderApiCompat;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import eu.pb4.placeholders.api.ParserContext;
import java.awt.Dimension;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Mod {
   Map<String, String> SUMMARY_CACHE = new ConcurrentHashMap();
   Map<String, Component> DESCRIPTION_CACHE = new ConcurrentHashMap();

   @NotNull
   String getId();

   @NotNull
   String getName();

   @NotNull
   default String getTranslatedName() {
      String translationKey = "modmenu.nameTranslation." + this.getId();
      if (!I18n.exists(translationKey)) {
         translationKey = "modmenu.nameTranslation." + this.getId();
      }

      if (!I18n.exists(translationKey)) {
         String var10000 = this.getId();
         translationKey = "modmenu.nameTranslation." + var10000.replace("_", "-");
      }

      return (this.getId().equals("minecraft") || this.getId().equals("java") || (Boolean)ModMenu.getConfig().TRANSLATE_NAMES.get()) && I18n.exists(translationKey) ? I18n.get(translationKey, new Object[0]) : this.getName();
   }

   @NotNull
   Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler var1, int var2, boolean var3);

    /**This was also causing the descriptions to render incorrectly.*/
    @NotNull
   default String getSummary() {
      return (String)SUMMARY_CACHE.computeIfAbsent(this.getId(), (id) -> {
         String string = this.getDescription();
         return TextPlaceholderApiCompat.parseText(string, ParserContext.of()).getString();
      });
   }

   @NotNull
   default String getTranslatedSummary() {
      String translationKey = "modmenu.summaryTranslation." + this.getId();
      if (!I18n.exists(translationKey)) {
         translationKey = "modmenu.summaryTranslation." + this.getId();
      }

      if (!I18n.exists(translationKey)) {
         String var10000 = this.getId();
         translationKey = "modmenu.summaryTranslation." + var10000.replace("_", "-");
      }

      return (this.getId().equals("minecraft") || this.getId().equals("java") || (Boolean)ModMenu.getConfig().TRANSLATE_DESCRIPTIONS.get()) && I18n.exists(translationKey) ? I18n.get(translationKey, new Object[0]) : this.getTranslatedDescription();
   }

   @NotNull
   String getDescription();

   @NotNull
   default String getTranslatedDescription() {
      String translatableDescriptionKey = "modmenu.descriptionTranslation." + this.getId();
      if (!I18n.exists(translatableDescriptionKey)) {
         translatableDescriptionKey = "modmenu.descriptionTranslation." + this.getId();
      }

      if (!I18n.exists(translatableDescriptionKey)) {
          String var10000 = this.getId();
          translatableDescriptionKey = "modmenu.descriptionTranslation." + var10000.replace("_", "-");
      }
       return (this.getId().equals("minecraft") || this.getId().equals("java") || ModMenu.getConfig().TRANSLATE_DESCRIPTIONS.get()) && I18n.exists(translatableDescriptionKey) ? I18n.get(translatableDescriptionKey) : this.getDescription();
   }

   default Component getFormattedDescription() {
       //this is causing the issue
      return (Component)DESCRIPTION_CACHE.computeIfAbsent(this.getId(), (id) -> {
         String string = this.getDescription();
         return TextPlaceholderApiCompat.parseText(string, ParserContext.of());
      });
   }

   default void reCalculateBadge() {
      ModMenuConfig config = ModMenu.getConfig();
      config.mod_badges.putIfAbsent(this.getId(), new LinkedHashSet());
      Set<String> defaultBadges = new LinkedHashSet(this.getBadgeNames());
      if (config.disabled_mod_badges.containsKey(this.getId())) {
         defaultBadges.removeAll((Collection)config.disabled_mod_badges.get(this.getId()));
      }

      this.getBadges().clear();
      if (!(Boolean)ModMenu.getConfig().DISABLE_DEFAULT_BADGES_ALL.get() && !((List)ModMenu.getConfig().DISABLE_DEFAULT_BADGES.get()).contains(this.getId())) {
         this.getBadges().addAll(ModBadge.convert(defaultBadges, this.getId()));
      }

      Set<String> badgelist = (Set)config.mod_badges.get(this.getId());
      this.getBadges().addAll(ModBadge.convert(badgelist, this.getId()));
   }

   @NotNull
   String getVersion();

   @NotNull
   String getPrefixedVersion();

   @NotNull
   List<String> getAuthors();

   @NotNull
   Map<String, Collection<String>> getContributors();

   @NotNull
   SortedMap<String, Set<String>> getCredits();

   @NotNull
   Set<ModBadge> getBadges();

   @NotNull
   Set<String> getBadgeNames();

   @Nullable
   String getWebsite();

   @Nullable
   String getIssueTracker();

   @Nullable
   String getSource();

   @Nullable
   String getParent();

   @NotNull
   Set<String> getLicense();

   @NotNull
   Map<String, String> getLinks();

   boolean isReal();

   void setChildHasUpdate();

   boolean getChildHasUpdate();

   boolean isHidden();

   com.terraformersmc.modmenu.util.mod.Mod.ModMenuData getModMenuData();

   Optional<ModContainer> getContainer();

    public class ModMenuData {
        private final Set<ModBadge> badges = new LinkedHashSet();
        private Optional<String> parent;
        @Nullable
        private final com.terraformersmc.modmenu.util.mod.Mod.ModMenuData.DummyParentData dummyParentData;

        public ModMenuData(Optional<String> parent, com.terraformersmc.modmenu.util.mod.Mod.ModMenuData.DummyParentData dummyParentData, String id) {
            this.parent = parent;
            this.dummyParentData = dummyParentData;
        }

        public Set<ModBadge> getBadges() {
            return this.badges;
        }

        public Optional<String> getParent() {
            return this.parent;
        }
        @Nullable
        public com.terraformersmc.modmenu.util.mod.Mod.ModMenuData.DummyParentData getDummyParentData() {
            return this.dummyParentData;
        }

        public void fillParentIfEmpty(String parent) {
            if (!this.parent.isPresent()) {
                this.parent = Optional.of(parent);
            }

        }
        public static class DummyParentData {
            private final String id;
            private final Optional<String> name;
            private final Optional<String> description;
            private final Optional<String> icon;
            private final Set<String> badges;

            public DummyParentData(String id, Optional<String> name, Optional<String> description, Optional<String> icon, Set<String> badges) {
                this.id = id;
                this.name = name;
                this.description = description;
                this.icon = icon;
                this.badges = badges;
            }

            public String getId() {
                return this.id;
            }

            public Optional<String> getName() {
                return this.name;
            }

            public Optional<String> getDescription() {
                return this.description;
            }

            public Optional<String> getIcon() {
                return this.icon;
            }

            public Set<String> getBadges() {
                return this.badges;
            }
        }
    }
}
