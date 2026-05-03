package com.terraformersmc.modmenu.mixin;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig.ModCountLocation;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BrandingControl.class})
public abstract class MixinBrandingControl {
   @Shadow
   private static List<String> brandings;
   @Shadow
   private static List<String> brandingsNoMC;

   @Shadow
   private static void computeBranding() {
   }

   @Inject(
      method = {"forEachLine"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void replaceBranding(boolean includeMC, boolean reverse, BiConsumer<Integer, String> lineConsumer, CallbackInfo ci) {
      if ((Boolean)ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
         List<String> brandings = getBrandings(includeMC, reverse);
         String neoForge = (String)brandings.getFirst();
         if (((ModCountLocation)ModMenu.getConfig().MOD_COUNT_LOCATION.get()).isOnTitleScreen()) {
            String count = ModMenu.getDisplayedModCount();
            String specificKey = "modmenu.mods." + count;
            String replacementKey = I18n.exists(specificKey) ? specificKey : "modmenu.mods.n";
            if ((Boolean)ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
               replacementKey = specificKey + ".secret";
            }

            neoForge = neoForge.replace(I18n.get("fml.menu.branding", new Object[]{"", ModList.get().size()}), I18n.get(replacementKey, new Object[]{count}));
         } else {
            neoForge = neoForge.replace(I18n.get("fml.menu.branding", new Object[]{"", ModList.get().size()}), I18n.get("menu.modded", new Object[0]));
         }

         lineConsumer.accept(0, neoForge);
         lineConsumer.accept(1, (String)brandings.get(1));
         ci.cancel();
      }

   }

   @Unique
   private static List<String> getBrandings(boolean includeMC, boolean reverse) {
      computeBranding();
      if (includeMC) {
         return reverse ? Lists.reverse(brandings) : brandings;
      } else {
         return reverse ? Lists.reverse(brandingsNoMC) : brandingsNoMC;
      }
   }
}
