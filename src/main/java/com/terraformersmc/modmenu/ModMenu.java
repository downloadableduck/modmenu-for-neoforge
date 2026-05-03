package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigScreen;
import com.terraformersmc.modmenu.config.ModMenuConfig.GameMenuButtonStyle;
import com.terraformersmc.modmenu.config.ModMenuConfig.TitleMenuButtonStyle;
import com.terraformersmc.modmenu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.java.JavaDummyMod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeDummyParentMod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeMod;
import java.awt.Color;
import java.awt.Dimension;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(
        value = "modmenu",
        dist = {Dist.CLIENT}
)
public class ModMenu {
    public static final String MOD_ID = "modmenu";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
    public static final Gson GSON;
    public static final Gson GSON_MINIFIED;
    public static final Pair<ModMenuConfig, ModConfigSpec> CONFIG;
    public static boolean shouldResetCache = false;
    public static final Map<String, com.terraformersmc.modmenu.util.mod.Mod> MODS;
    public static final Map<String, com.terraformersmc.modmenu.util.mod.Mod> ROOT_MODS;
    public static final LinkedListMultimap<com.terraformersmc.modmenu.util.mod.Mod, com.terraformersmc.modmenu.util.mod.Mod> PARENT_MAP;
    public static final Map<String, IConfigScreenFactory> configScreenFactories;
    private static int cachedDisplayedModCount;

    public static boolean hasConfigScreen(ModContainer container) {
        return getConfigScreenFactory(container) != null;
    }

    @Nullable
    public static Screen getConfigScreen(ModContainer container, Screen parent) {
        IConfigScreenFactory factory = getConfigScreenFactory(container);
        return factory != null ? factory.createScreen(container, parent) : null;
    }

    @Nullable
    private static IConfigScreenFactory getConfigScreenFactory(ModContainer container) {
        if (!((List)getConfig().HIDDEN_CONFIGS.get()).contains(container.getModId()) && !"java".equals(container.getModId())) {
            if (configScreenFactories.containsKey(container.getModId())) {
                return (IConfigScreenFactory)configScreenFactories.get(container.getModId());
            } else {
                configScreenFactories.putIfAbsent("minecraft", (modContainer, screen) -> {
                    return new OptionsScreen(screen, Minecraft.getInstance().options, false);
                });
                Optional<IConfigScreenFactory> factoryOptional = IConfigScreenFactory.getForMod(container.getModInfo());
                if (factoryOptional.isPresent()) {
                    configScreenFactories.putIfAbsent(container.getModId(), (IConfigScreenFactory)factoryOptional.get());
                    return (IConfigScreenFactory)configScreenFactories.get(container.getModId());
                } else {
                    String modId = container.getModId();
                    boolean hasNeoForgeConfig = hasNeoForgeConfig(modId);
                    if (hasNeoForgeConfig) {
                        IConfigScreenFactory autoFactory = (modContainer, screen) -> {
                            return new ConfigurationScreen(modContainer, screen);
                        };
                        configScreenFactories.putIfAbsent(modId, autoFactory);
                        LOGGER.debug("Auto-detected NeoForge config for mod: {}", modId);
                    }

                    return (IConfigScreenFactory)configScreenFactories.get(container.getModId());
                }
            }
        } else {
            return null;
        }
    }

    private static boolean hasNeoForgeConfig(String modId) {
        try {
            Type[] var1 = Type.values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Type type = var1[var3];
                Set<ModConfig> configSet = ModConfigs.getConfigSet(type);
                if (configSet != null) {
                    Iterator var6 = configSet.iterator();

                    while(var6.hasNext()) {
                        ModConfig config = (ModConfig)var6.next();
                        if (modId.equals(config.getModId())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable var8) {
            LOGGER.warn("Error checking NeoForge configs for mod {}: {}", modId, var8.getMessage());
        }

        return false;
    }

    public ModMenu(IEventBus bus, ModContainer container) {
        bus.addListener(this::onClientSetup);
        container.registerConfig(Type.CLIENT, (IConfigSpec)CONFIG.getValue());
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainerx, screen) -> {
            return new ConfigurationScreen(container, screen, ModMenuConfigScreen::new);
        });
        Iterator var3 = ModList.get().getSortedMods().iterator();

        while(var3.hasNext()) {
            ModContainer modContainer = (ModContainer)var3.next();
            com.terraformersmc.modmenu.util.mod.Mod mod = new NeoforgeMod(modContainer);
            MODS.put(mod.getId(), mod);
        }

        Map<String, com.terraformersmc.modmenu.util.mod.Mod> dummyParents = new HashMap();
        HashSet<String> modParentSet = new HashSet();
        Iterator var11 = MODS.values().iterator();

        while(true) {
            while(var11.hasNext()) {
                com.terraformersmc.modmenu.util.mod.Mod mod = (com.terraformersmc.modmenu.util.mod.Mod)var11.next();
                String parentId = mod.getParent();
                if (parentId == null) {
                    ROOT_MODS.put(mod.getId(), mod);
                } else {
                    modParentSet.clear();

                    Object parent;
                    while(true) {
                        parent = (com.terraformersmc.modmenu.util.mod.Mod)MODS.getOrDefault(parentId, (com.terraformersmc.modmenu.util.mod.Mod)dummyParents.get(parentId));
                        if (parent == null) {
                            parent = new NeoforgeDummyParentMod(mod, parentId);
                            dummyParents.put(parentId, (com.terraformersmc.modmenu.util.mod.Mod) parent);
                        }

                        parentId = parent != null ? ((com.terraformersmc.modmenu.util.mod.Mod)parent).getParent() : null;
                        if (parentId == null) {
                            break;
                        }

                        if (modParentSet.contains(parentId)) {
                            LOGGER.warn("Mods contain each other as parents: {}", modParentSet);
                            parent = null;
                            break;
                        }

                        modParentSet.add(parentId);
                    }

                    if (parent == null) {
                        ROOT_MODS.put(mod.getId(), mod);
                    } else {
                        PARENT_MAP.put((com.terraformersmc.modmenu.util.mod.Mod) parent, mod);
                    }
                }
            }

            com.terraformersmc.modmenu.util.mod.Mod java = new JavaDummyMod();
            MODS.put("java", java);
            ROOT_MODS.put("java", java);
            MODS.putAll(dummyParents);
            return;
        }
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        getConfig().onLoad();
    }

    public static void clearModCountCache() {
        cachedDisplayedModCount = -1;
    }

    public static String getDisplayedModCount() {
        if (cachedDisplayedModCount == -1) {
            boolean includeChildren = (Boolean)getConfig().COUNT_CHILDREN.get();
            boolean includeLibraries = (Boolean)getConfig().COUNT_LIBRARIES.get();
            boolean includeHidden = (Boolean)getConfig().COUNT_HIDDEN_MODS.get();
            cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter((mod) -> {
                boolean isChild = mod.getParent() != null;
                if (!includeChildren && isChild) {
                    return false;
                } else {
                    boolean isLibrary = mod.getBadges().contains(ModBadge.LIBRARY);
                    if (!includeLibraries && isLibrary) {
                        return false;
                    } else {
                        return includeHidden || !mod.isHidden();
                    }
                }
            }).count());
        }

        return NumberFormat.getInstance().format((long)cachedDisplayedModCount);
    }

    public static Component createModsButtonText(boolean title) {
        TitleMenuButtonStyle titleStyle = (TitleMenuButtonStyle)getConfig().MODS_BUTTON_STYLE.get();
        GameMenuButtonStyle gameMenuStyle = (GameMenuButtonStyle)getConfig().GAME_MENU_BUTTON_STYLE.get();
        boolean isIcon = title ? titleStyle == TitleMenuButtonStyle.ICON : gameMenuStyle == GameMenuButtonStyle.ICON;
        MutableComponent modsText = ModMenuScreenTexts.TITLE.copy().withStyle((style) -> {
            return style.withColor(16747520);
        });
        if (!isIcon) {
            try {
                String count = getDisplayedModCount();
                modsText.append(Component.literal(" (" + count + ")").withStyle((style) -> {
                    return style.withColor(16747520);
                }));
            } catch (Exception var6) {
            }
        }

        return modsText;
    }

    public static ModMenuConfig getConfig() {
        return (ModMenuConfig)CONFIG.getLeft();
    }

    public static void createBadgesAndIcons() {
        ModBadge.CUSTOM_BADGES.clear();
        NeoforgeIconHandler.modResourceIconCache.clear();
        Stream<PackResources> resourcePacks = Minecraft.getInstance().getResourceManager().listPacks();
        resourcePacks.forEach((packResources) -> {
            packResources.getNamespaces(PackType.CLIENT_RESOURCES).forEach((namespace) -> {
                packResources.listResources(PackType.CLIENT_RESOURCES, namespace, "badge", (key, value) -> {
                    try {
                        JsonObject jsonObject = GsonHelper.parse(new InputStreamReader((InputStream)value.get()));
                        JsonArray fillColor = jsonObject.getAsJsonArray("fill_color");
                        JsonArray outlineColor = jsonObject.getAsJsonArray("outline_color");

                        JsonArray textColor;
                        try {
                            textColor = jsonObject.getAsJsonArray("text_color");
                        } catch (Exception var8) {
                            textColor = null;
                        }

                        String id = key.getPath().replace("badge/", "").replace(".json", "");
                        ModBadge badge = new ModBadge(id, jsonObject.get("name").getAsString(), (new Color(outlineColor.get(0).getAsInt(), outlineColor.get(1).getAsInt(), outlineColor.get(2).getAsInt())).getRGB(), (new Color(fillColor.get(0).getAsInt(), fillColor.get(1).getAsInt(), fillColor.get(2).getAsInt())).getRGB(), textColor == null ? -3487030 : (new Color(textColor.get(0).getAsInt(), textColor.get(1).getAsInt(), textColor.get(2).getAsInt())).getRGB());
                        ModBadge.CUSTOM_BADGES.put(id, badge);
                    } catch (Exception var9) {
                        LOGGER.warn("incorrect badge json from {} because {}", key, var9.getMessage());
                    }

                });
                packResources.listResources(PackType.CLIENT_RESOURCES, namespace, "modicon", (key, value) -> {
                    try {
                        NativeImage image = NativeImage.read((InputStream)value.get());
                        Tuple<DynamicTexture, Dimension> tex = new Tuple(new DynamicTexture(() -> {
                            return key.toString();
                        }, image), new Dimension(image.getWidth(), image.getHeight()));
                        String id = key.getPath().replace("modicon/", "").replace(".png", "");
                        NeoforgeIconHandler.modResourceIconCache.put(id, tex);
                    } catch (Exception var5) {
                        LOGGER.warn(var5.getMessage());
                    }

                });
            });
        });
        shouldResetCache = true;
        MODS.values().forEach(com.terraformersmc.modmenu.util.mod.Mod::reCalculateBadge);
    }

    static {
        GsonBuilder builder = (new GsonBuilder()).registerTypeHierarchyAdapter(Enum.class, new EnumToLowerCaseJsonConverter()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        GSON = builder.setPrettyPrinting().create();
        GSON_MINIFIED = builder.create();
        CONFIG = (new Builder()).configure(ModMenuConfig::new);
        MODS = new HashMap();
        ROOT_MODS = new HashMap();
        PARENT_MAP = LinkedListMultimap.create();
        configScreenFactories = new HashMap();
        cachedDisplayedModCount = -1;
    }
}
