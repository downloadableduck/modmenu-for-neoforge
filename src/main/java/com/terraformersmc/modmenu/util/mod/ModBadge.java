package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

public class ModBadge {
   private final String id;
   private final Component component;
   private final int fillColor;
   private final int outlineColor;
   private final int textColor;
   public static ModBadge LIBRARY = new ModBadge("library", "modmenu.badge.library", -15698860, -16172759);
   public static Map<String, ModBadge> DEFAULT_BADGES;
   public static Map<String, ModBadge> CUSTOM_BADGES;
   public static List<Map<String, ModBadge>> BADGES;

   public ModBadge(String id, String displayName, int outlineColor, int fillColor) {
      this(id, displayName, outlineColor, fillColor, 13290186);
   }

   public ModBadge(String id, String displayName, int outlineColor, int fillColor, int textColor) {
      this.id = id;
      this.component = Component.translatable(displayName);
      this.fillColor = fillColor;
      this.outlineColor = outlineColor;
      this.textColor = textColor;
   }

   public String getId() {
      return this.id;
   }

   public Component getComponent() {
      return this.component;
   }

   public int getOutlineColor() {
      return this.outlineColor;
   }

   public int getFillColor() {
      return this.fillColor;
   }

   public int getTextColor() {
      return this.textColor;
   }

   public static Set<ModBadge> convert(Set<String> badgeKeys, String modId) {
      return (Set)badgeKeys.stream().map((key) -> {
         if (DEFAULT_BADGES.containsKey(key)) {
            return (ModBadge)DEFAULT_BADGES.get(key);
         } else if (CUSTOM_BADGES.containsKey(key)) {
            return (ModBadge)CUSTOM_BADGES.get(key);
         } else {
            ModMenu.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
            return null;
         }
      }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   static {
      DEFAULT_BADGES = Map.of("library", LIBRARY, "client", new ModBadge("client", "modmenu.badge.clientsideOnly", -13939844, -15848875), "deprecated", new ModBadge("deprecated", "modmenu.badge.deprecated", -8121306, -11334633), "sinytra_fabric", new ModBadge("sinytra_fabric", "modmenu.badge.fabric", -3689333, -8884904), "sinytra_neoforge", new ModBadge("sinytra_neoforge", "modmenu.badge.neoforge", -1668041, -6009289), "modpack", new ModBadge("modpack", "modmenu.badge.modpack", -8770692, -11465388), "minecraft", new ModBadge("minecraft", "modmenu.badge.minecraft", -9474966, -13553617));
      CUSTOM_BADGES = new LinkedHashMap();
      BADGES = List.of(DEFAULT_BADGES, CUSTOM_BADGES);
   }
}
