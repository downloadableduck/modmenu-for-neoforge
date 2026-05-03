package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Component title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "createTestWorldButton")
    private void createModMenuButton(int topPos, int spacing, CallbackInfoReturnable<Integer> cir) {
        //dont use anything other than component.literal("Mod Menu") since it doesnt work otherwise for some reason
        if (!FMLEnvironment.isProduction()) {
            this.addRenderableWidget(new ModMenuButtonWidget(this.width / 2 + 5, topPos += spacing, 98, 20, Component.literal("Mod Menu"), this));
        } else {
            this.addRenderableWidget(new ModMenuButtonWidget(this.width / 2 - 100, topPos += spacing - 25, 200, 20, Component.literal("Mod Menu"), this));
        }
    }
}
