package com.terraformersmc.modmenu.util;

import java.util.Iterator;
import java.util.List;

public final class VersionUtil {
   private static final List<String> PREFIXES = List.of("version", "ver", "v");

   private VersionUtil() {
   }

   public static String stripPrefix(String version) {
      version = version.trim();
      Iterator var1 = PREFIXES.iterator();

      String prefix;
      do {
         if (!var1.hasNext()) {
            return version;
         }

         prefix = (String)var1.next();
      } while(!version.startsWith(prefix));

      return version.substring(prefix.length());
   }

   public static String getPrefixedVersion(String version) {
      return "v" + stripPrefix(version);
   }

   public static String removeBuildMetadata(String version) {
      return version.split("\\+")[0];
   }
}
