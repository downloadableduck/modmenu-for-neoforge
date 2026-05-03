package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PauseScreen.class})
public abstract class MixinPauseScreen extends Screen {
   protected MixinPauseScreen(Component title) {
      super(title);
   }

   @Inject(
      method = {"createPauseMenu"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"
)}
   )
   private void onInitWidgets(CallbackInfo ci, @Local GridLayout gridlayout) {
      if (gridlayout != null) {
         List<GridLayout.ChildContainer> buttons = ((AccessorGridLayout)gridlayout).getChildren();
         if (ModMenu.getConfig().MODIFY_GAME_MENU.get()) {
            int optionsY = -1;
            Iterator<GridLayout.ChildContainer> var5 = buttons.iterator();

            while (var5.hasNext()) {
               LayoutElement widget = var5.next().child;
               if (ModMenuEventHandler.buttonHasText(widget, "menu.options")) {
                  optionsY = widget.getY();
                  break;
               }
            }

            buttons.removeIf((button) -> {
               return ModMenuEventHandler.buttonHasText(button.child, new String[]{"fml.menu.mods"});
            });
            if (optionsY != -1) {
               int modMenuY = optionsY + 24;
               gridlayout.addChild(new ModMenuButtonWidget(this.width / 2 - 102, modMenuY, 204, 20, ModMenu.createModsButtonText(false), this), gridlayout.getY(), gridlayout.getX());
            }
         }
      }

   }
}
