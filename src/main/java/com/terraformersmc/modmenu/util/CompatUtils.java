package com.terraformersmc.modmenu.util;

import net.neoforged.fml.ModList;

public class CompatUtils {
   public static boolean isCustomMenu() {
      return ModList.get().isLoaded("cumulus_menus");
   }
}
