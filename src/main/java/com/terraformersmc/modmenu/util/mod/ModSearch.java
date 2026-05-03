package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Tuple;

public class ModSearch {
   public static boolean validSearchQuery(String query) {
      return query != null && !query.isEmpty();
   }

   public static List<Mod> search(ModsScreen screen, String query, List<Mod> candidates) {
      return !validSearchQuery(query) ? candidates.stream().filter((child) -> {
         return !child.isHidden() && ((Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() || !child.getBadges().contains(ModBadge.LIBRARY));
      }).toList() : (List)candidates.stream().map((modContainer) -> {
         return new Tuple(modContainer, passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT)));
      }).filter((pair) -> {
         return (Integer)pair.getB() > 0;
      }).sorted((a, b) -> {
         return (Integer)b.getB() - (Integer)a.getB();
      }).map(Tuple::getA).collect(Collectors.toList());
   }

   private static int passesFilters(ModsScreen screen, Mod mod, String query) {
      String modId = mod.getId();
      String modName = mod.getName();
      String modTranslatedName = mod.getTranslatedName();
      String modDescription = mod.getDescription();
      ModMenu.LOGGER.warn(modDescription);
      String modSummary = mod.getSummary();
      boolean hasCustomBadge = false;
      Iterator var9 = ModBadge.CUSTOM_BADGES.entrySet().iterator();

      String modpack;
      while(var9.hasNext()) {
         Entry<String, ModBadge> badgeEntry = (Entry)var9.next();
         modpack = ((ModBadge)badgeEntry.getValue()).getComponent().getString();
         if (I18n.exists("modmenu.searchTerms." + (String)badgeEntry.getKey())) {
            modpack = I18n.get("modmenu.searchTerms." + (String)badgeEntry.getKey(), new Object[0]);
         }

         if (modpack.contains(query) && mod.getBadges().contains(badgeEntry.getValue())) {
            hasCustomBadge = true;
            break;
         }
      }

      String library = I18n.get("modmenu.searchTerms.library", new Object[0]);
      String sinytra = I18n.get("modmenu.searchTerms.sinytra", new Object[0]);
      modpack = I18n.get("modmenu.searchTerms.modpack", new Object[0]);
      String deprecated = I18n.get("modmenu.searchTerms.deprecated", new Object[0]);
      String clientside = I18n.get("modmenu.searchTerms.clientside", new Object[0]);
      String neoforge = I18n.get("modmenu.searchTerms.neoforge", new Object[0]);
      String configurable = I18n.get("modmenu.searchTerms.configurable", new Object[0]);
      String hasUpdate = I18n.get("modmenu.searchTerms.hasUpdate", new Object[0]);
      if (!mod.isHidden() && ((Boolean)ModMenu.getConfig().SHOW_LIBRARIES.get() || !mod.getBadges().contains(ModBadge.LIBRARY))) {
         if (!modName.toLowerCase(Locale.ROOT).contains(query) && !modTranslatedName.toLowerCase(Locale.ROOT).contains(query) && !modId.toLowerCase(Locale.ROOT).contains(query)) {
            if (!modDescription.toLowerCase(Locale.ROOT).contains(query) && !modSummary.toLowerCase(Locale.ROOT).contains(query) && !authorMatches(mod, query) && (!library.contains(query) || !mod.getBadges().contains(ModBadge.LIBRARY)) && (!sinytra.contains(query) || !mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("sinytra_fabric"))) && (!modpack.contains(query) || !mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("modpack"))) && (!deprecated.contains(query) || !mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("deprecated"))) && (!clientside.contains(query) || !mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("client"))) && (!neoforge.contains(query) || !mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("sinytra_neoforge"))) && !hasCustomBadge && (!configurable.contains(query) || !screen.getModHasConfigScreen(mod.getContainer()))) {
               if (ModMenu.PARENT_MAP.keySet().contains(mod)) {
                  Iterator var17 = ModMenu.PARENT_MAP.get(mod).iterator();

                  while(var17.hasNext()) {
                     Mod child = (Mod)var17.next();
                     int result = passesFilters(screen, child, query);
                     if (result > 0) {
                        return result;
                     }
                  }
               }

               return 0;
            } else {
               return 1;
            }
         } else {
            return query.length() >= 3 ? 2 : 1;
         }
      } else {
         return 0;
      }
   }

   private static boolean authorMatches(Mod mod, String query) {
      return mod.getAuthors().stream().map((s) -> {
         return s.toLowerCase(Locale.ROOT);
      }).anyMatch((s) -> {
         return s.contains(query.toLowerCase(Locale.ROOT));
      });
   }
}
