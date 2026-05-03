package com.terraformersmc.modmenu.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig.TitleMenuButtonStyle;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.util.CompatUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

@EventBusSubscriber(
   modid = "modmenu",
   value = {Dist.CLIENT}
)
public class ModMenuEventHandler {
   public static final Identifier MODS_BUTTON_TEXTURE = Identifier.parse("modmenu:textures/gui/mods_button.png");
   public static KeyMapping MENU_KEY_BIND;
   static String key;

   @SubscribeEvent
   public static void onScreenInit(Post event) {
      Screen screen = event.getScreen();
      if (screen instanceof TitleScreen) {
         for(int i = 0; i < screen.renderables.size(); ++i) {
            Object renderable = screen.renderables.get(i);
            if (renderable instanceof Button) {
               Button b = (Button)renderable;
               if (buttonHasText(b, "menu.feedback") || buttonHasText(b, "menu.sendFeedback") || buttonHasText(b, "menu.reportBugs")) {
                  try {
                     b.visible = false;
                     b.active = false;
                  } catch (Exception var6) {
                  }
               }
            }
         }

         if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && !CompatUtils.isCustomMenu()) {
            removeModsButton(screen);
            afterTitleScreenInit(screen);
         }
      }

   }

   private static void removeModsButton(Screen screen) {
      int i;
      Object renderable;
      Button b;
      for(i = 0; i < screen.renderables.size(); ++i) {
         renderable = screen.renderables.get(i);
         if (renderable instanceof Button) {
            b = (Button)renderable;
            if (buttonHasText(b, "fml.menu.mods")) {
               try {
                  int fullWidth = 200;
                  int x = screen.width / 2 - fullWidth / 2;
                  set(screen, i, new ModMenuButtonWidget(x, b.getY(), fullWidth, b.getHeight(), ModMenu.createModsButtonText(true), screen));
               } catch (IndexOutOfBoundsException var8) {
                  b.setMessage(ModMenu.createModsButtonText(true));
               }
            }
         }
      }

      for(i = 0; i < screen.renderables.size(); ++i) {
         renderable = screen.renderables.get(i);
         if (renderable instanceof Button) {
            b = (Button)renderable;
            if (buttonHasText(b, "menu.feedback") || buttonHasText(b, "menu.sendFeedback") || buttonHasText(b, "menu.reportBugs")) {
               try {
                  b.visible = false;
                  b.active = false;
               } catch (Exception var7) {
               }
            }

            try {
               String btnText = b.getMessage().getString();
               if (btnText != null && btnText.contains("Create Test World")) {
                  b.visible = false;
                  b.active = false;
               }
            } catch (Exception var6) {
            }
         }
      }

   }

   private static void afterTitleScreenInit(Screen screen) {
      List<Renderable> buttons = screen.renderables;
      int modsButtonIndex = -1;
      int buttonsY = screen.height / 4 + 48;
      boolean replacedRealmButton = false;

      for(int i = 0; i < buttons.size(); ++i) {
         Renderable widget = buttons.get(i);
         if (widget instanceof Button) {
            Button button = (Button)widget;
            if (!(button instanceof PlainTextButton)) {
               shiftButtons(button, replacedRealmButton, 12 + (replacedRealmButton ? -12 : 8));
               boolean isRealmsButton = buttonHasText(button, "menu.online");
               if (isRealmsButton) {
                  replacedRealmButton = true;
               }

               if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.CLASSIC && button.visible) {
                  shiftButtons(button, modsButtonIndex == -1, 12);
                  if (modsButtonIndex == -1) {
                     buttonsY = button.getY();
                  }
               }

               if (isRealmsButton) {
                  if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.REPLACE_REALMS) {
                     set(screen, i, new ModMenuButtonWidget(button.getX(), button.getY(), button.getWidth(), button.getHeight(), ModMenu.createModsButtonText(true), screen));
                  } else {
                     if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.SHRINK) {
                        button.setWidth(98);
                     }

                     if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.SHRINK_LEFT) {
                        button.setWidth(98);
                        button.setX(screen.width / 2 + 2);
                     }

                     modsButtonIndex = i + 1;
                     if (button.visible) {
                        buttonsY = button.getY();
                     }
                  }
               }

               if (modsButtonIndex == -1 && buttonHasText(button, "fml.menu.mods")) {
                  if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() != TitleMenuButtonStyle.CLASSIC) {
                     buttonsY = button.getY();
                  }

                  modsButtonIndex = i;
               }
            }
         }
      }

      if (modsButtonIndex != -1) {
         boolean hasModMenuButton = buttons.stream().anyMatch((r) -> {
            return r instanceof ModMenuButtonWidget;
         });
         if (hasModMenuButton) {
            return;
         }

         if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.CLASSIC) {
            add(screen, modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY + 12, 200, 20, ModMenu.createModsButtonText(true), screen));
         } else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.SHRINK) {
            add(screen, modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 + 2, buttonsY, 98, 20, ModMenu.createModsButtonText(true), screen));
         } else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.SHRINK_LEFT) {
            add(screen, modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY, 98, 20, ModMenu.createModsButtonText(true), screen));
         } else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == TitleMenuButtonStyle.ICON) {
            add(screen, modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104, buttonsY, 20, 20, 0, 0, 20, MODS_BUTTON_TEXTURE, 32, 64, (buttonx) -> {
               Minecraft.getInstance().setScreen(new ModsScreen(screen));
            }, ModMenu.createModsButtonText(true)));
         }
      }

   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (MENU_KEY_BIND != null) {
         while(MENU_KEY_BIND.consumeClick()) {
            Minecraft.getInstance().setScreen(new ModsScreen(Minecraft.getInstance().screen));
         }
      }

   }

   public static boolean buttonHasText(LayoutElement element, String... translationKeys) {
      if (!(element instanceof Button)) {
         return false;
      } else {
         Button button = (Button)element;
         Component component = button.getMessage();
         ComponentContents textContent = component.getContents();
         if (textContent instanceof TranslatableContents) {
            key = ((TranslatableContents)textContent).getKey();
            if (Arrays.stream(translationKeys).anyMatch((s) -> {
               return key.equals(s);
            })) {
               return true;
            }

            if (key.equals("menu.mods") || key.equals("menu.modlist") || key.equals("menu.modmenu")) {
               return true;
            }
         }

         try {
            key = component.getString();
            if (key != null && key.toLowerCase().contains("mods")) {
               return true;
            }
         } catch (Exception var6) {
         }

         return false;
      }
   }

   public static void shiftButtons(LayoutElement element, boolean shiftUp, int spacing) {
      if (shiftUp) {
         element.setY(element.getY() - spacing / 2);
      } else {
         if (element instanceof AbstractWidget) {
            AbstractWidget button = (AbstractWidget)element;
            if (button.getMessage().equals(Component.translatable("title.credits"))) {
               return;
            }
         }

         element.setY(element.getY() + spacing / 2);
      }

   }

   public static AbstractWidget set(Screen screen, int index, AbstractWidget element) {
      int drawableIndex = translateIndex(screen.renderables, index, false);
      screen.renderables.set(drawableIndex, element);
      int selectableIndex = translateIndex(screen.narratables, index, false);
      screen.narratables.set(selectableIndex, element);
      int childIndex = translateIndex(screen.children, index, false);
      return (AbstractWidget)screen.children.set(childIndex, element);
   }

   public static void add(Screen screen, int index, AbstractWidget element) {
      int duplicateIndex = screen.renderables.indexOf(element);
      if (duplicateIndex >= 0) {
         screen.renderables.remove(element);
         screen.narratables.remove(element);
         screen.children.remove(element);
         if (duplicateIndex <= translateIndex(screen.renderables, index, true)) {
            --index;
         }
      }

      int drawableIndex = translateIndex(screen.renderables, index, true);
      screen.renderables.add(drawableIndex, element);
      int selectableIndex = translateIndex(screen.narratables, index, true);
      screen.narratables.add(selectableIndex, element);
      int childIndex = translateIndex(screen.children, index, true);
      screen.children.add(childIndex, element);
   }

   private static int translateIndex(List<?> list, int index, boolean allowAfter) {
      int remaining = index;
      int i = 0;

      for(int max = list.size(); i < max; ++i) {
         if (list.get(i) instanceof AbstractWidget) {
            if (remaining == 0) {
               return i;
            }

            --remaining;
         }
      }

      if (allowAfter && remaining == 0) {
         return list.size();
      } else {
         throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
      }
   }
    @EventBusSubscriber(
            modid = "modmenu",
            value = {Dist.CLIENT}
    )
    public class ModBusEvents {
        @SubscribeEvent
        public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
            try {
                Class<?> kmClass = KeyMapping.class;
                KeyMapping key = null;

                try {
                    key = (KeyMapping)kmClass.getConstructor(String.class, Integer.TYPE, String.class).newInstance("key.modmenu.open_menu", -1, "category.modmenu.name");
                } catch (NoSuchMethodException var5) {
                }

                if (key == null) {
                    try {
                        key = (KeyMapping)kmClass.getConstructor(String.class, InputConstants.Type.class, Integer.TYPE, String.class).newInstance("key.modmenu.open_menu", InputConstants.Type.KEYSYM, -1, "category.modmenu.name");
                    } catch (NoSuchMethodException var4) {
                    }
                }

                ModMenuEventHandler.MENU_KEY_BIND = key;
                if (ModMenuEventHandler.MENU_KEY_BIND != null) {
                    event.register(ModMenuEventHandler.MENU_KEY_BIND);
                } else {
                    ModMenu.LOGGER.warn("Could not construct KeyMapping for Mod Menu; keybind disabled on this environment.");
                }
            } catch (Throwable var6) {
                ModMenu.LOGGER.warn("Failed to register Mod Menu keybinding (reflection attempt)", var6);
            }

        }
    }

}
