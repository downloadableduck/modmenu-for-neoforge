package com.terraformersmc.modmenu.mixin;

import java.util.List;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({GridLayout.class})
public interface AccessorGridLayout {
   @Accessor
   List<GridLayout.ChildContainer> getChildren();
}
