package com.terraformersmc.modmenu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeDummyParentMod;

import java.util.*;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;

public class ModMenuConfig {
    public final EnumValue<Sorting> SORTING;
    public final BooleanValue COUNT_LIBRARIES;
    public final BooleanValue COMPACT_LIST;
    public final BooleanValue COUNT_CHILDREN;
    public final EnumValue<TitleMenuButtonStyle> MODS_BUTTON_STYLE;
    public final BooleanValue COUNT_HIDDEN_MODS;
    public final EnumValue<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE;
    public final EnumValue<ModCountLocation> MOD_COUNT_LOCATION;
    public final BooleanValue HIDE_MOD_LINKS;
    public final BooleanValue SHOW_LIBRARIES;
    public final BooleanValue HIDE_MOD_LICENSE;
    public final BooleanValue HIDE_BADGES;
    public final BooleanValue HIDE_MOD_CREDITS;
    public final BooleanValue EASTER_EGGS;
    public final BooleanValue RANDOM_JAVA_COLORS;
    public final BooleanValue TRANSLATE_NAMES;
    public final BooleanValue TRANSLATE_DESCRIPTIONS;
    public final BooleanValue QUICK_CONFIGURE;
    public final BooleanValue USE_CATALOGUE_ICON;
    public final BooleanValue MODIFY_TITLE_SCREEN;
    public final BooleanValue MODIFY_GAME_MENU;
    public final BooleanValue HIDE_CONFIG_BUTTONS;
    public final BooleanValue HIDE_BADGE_BUTTONS;
    public final BooleanValue HIDE_SCREEN_TOP;
    public final BooleanValue CONFIG_MODE;
    public final BooleanValue DISABLE_DRAG_AND_DROP;
    public final ConfigValue<List<? extends String>> HIDDEN_MODS;
    public final ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
    public final ConfigValue<List<? extends String>> LIBRARY_LIST;
    public final ConfigValue<List<? extends String>> MOD_BADGES;
    public final ConfigValue<List<? extends String>> MOD_PARENTS;
    public final ConfigValue<List<? extends String>> DISABLE_DEFAULT_BADGES;
    public final BooleanValue DISABLE_DEFAULT_BADGES_ALL;
    public final ConfigValue<List<? extends String>> HIDE_BADGE;
    public final Map<String, Set<String>> mod_badges = new HashMap();
    public final Map<String, Set<String>> disabled_mod_badges = new HashMap();

    public ModMenuConfig(Builder builder) {
        builder.push("main");
        this.SORTING = builder.defineEnum("sorting", com.terraformersmc.modmenu.config.ModMenuConfig.Sorting.ASCENDING);
        this.COMPACT_LIST = builder.comment("Makes list more compacted").define("compact_list", false);
        this.MODS_BUTTON_STYLE = builder.defineEnum("mods_button_style", com.terraformersmc.modmenu.config.ModMenuConfig.TitleMenuButtonStyle.CLASSIC);
        this.GAME_MENU_BUTTON_STYLE = builder.defineEnum("game_menu_button_style", com.terraformersmc.modmenu.config.ModMenuConfig.GameMenuButtonStyle.REPLACE);
        this.MOD_COUNT_LOCATION = builder.defineEnum("mod_count_location", com.terraformersmc.modmenu.config.ModMenuConfig.ModCountLocation.TITLE_SCREEN);
        this.EASTER_EGGS = builder.comment("Shows secret mod count translations defined by modmenu.mods.MOD_COUND.secret").define("easter_eggs", true);
        this.RANDOM_JAVA_COLORS = builder.comment("Makes java mod have random colors").define("random_java_colors", false);
        this.TRANSLATE_NAMES = builder.comment("Make mod names translatable defining by modmenu.nameTranslation.modid").define("translate_names", true);
        this.TRANSLATE_DESCRIPTIONS = builder.comment("Make mod descriptions translatable defining by modmenu.descriptionTranslation.modid").define("translate_descriptions", true);
        this.QUICK_CONFIGURE = builder.comment(new String[0]).comment("Shows config button above mod icon on the left").define("quick_configure", true);
        this.MODIFY_TITLE_SCREEN = builder.comment("Modifies title screen, if false will be neoforge default with default mods button").define("modify_title_screen", true);
        this.MODIFY_GAME_MENU = builder.comment("Changes pause screen's button position and replaces neoforge's mods button with modmenu's one").define("modify_game_menu", true);
        this.CONFIG_MODE = builder.comment("Will only show mods with config available").define("config_mode", false);
        this.DISABLE_DRAG_AND_DROP = builder.comment("Disables drag and drop mods adding").define("disable_drag_and_drop", false);
        this.USE_CATALOGUE_ICON = builder.comment("Will use catalogue's icon if present").define("use_catalogue_icon", true);
        builder.pop();
        builder.push("hide");
        this.SHOW_LIBRARIES = builder.comment("Shows mods with library badge").define("show_libraries", false);
        this.HIDE_MOD_LINKS = builder.comment("Hides links of the mod").define("hide_mod_links", false);
        this.HIDE_MOD_LICENSE = builder.comment("Hides mod's license").define("hide_mod_license", false);
        this.HIDE_BADGES = builder.comment("Hides mod's badges").define("hide_badges", false);
        this.HIDE_BADGE = builder.comment("Add id of the badge to hide it").defineList("hide_badge", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        this.HIDE_MOD_CREDITS = builder.comment("Hides mod's credits").define("hide_mod_credits", false);
        this.HIDE_CONFIG_BUTTONS = builder.comment("Hides mod's config button").define("hide_config_buttons", false);
        this.HIDE_BADGE_BUTTONS = builder.comment("hides button which allows changing mod's badge").define("hide_badge_buttons", true);
        this.HIDE_SCREEN_TOP = builder.comment("Hides search bar and drag and drop text, also moves mod's icon up").define("hide_screen_top", false);
        this.HIDDEN_MODS = builder.comment("Add modid of the mod to hide it from the modlist").defineList("hidden_mods", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        this.HIDDEN_CONFIGS = builder.comment("Add modid of the mod to hide its config").defineList("hidden_configs", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        this.LIBRARY_LIST = builder.comment("deprecated").defineList("library_list", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        builder.comment("deprecated").push("disable_default_badges_section");
        this.DISABLE_DEFAULT_BADGES_ALL = builder.define("disable_default_badges_all", false);
        this.DISABLE_DEFAULT_BADGES = builder.defineList("disable_default_badges", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        builder.pop();
        builder.pop();
        builder.push("count");
        this.COUNT_HIDDEN_MODS = builder.comment("Makes hidden mods count added to the total mods count").define("count_hidden_mods", true);
        this.COUNT_CHILDREN = builder.comment("Makes childrens count get added to the total mods count").define("count_children", true);
        this.COUNT_LIBRARIES = builder.comment("Makes libraries count get added to the total mods count").define("count_libraries", true);
        builder.pop();
        this.MOD_BADGES = builder.comment("Adds badge to mod in this format \"modid=badge1, badge2\"").defineList("mod_badges", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
        this.MOD_PARENTS = builder.comment("Make mods apear under another mod in this format \"parenModId=childId1, childId2\"").defineList("mod_parents", ArrayList::new, String::new, (object) -> {
            return object instanceof String;
        });
    }

    public void onLoad() {
        ((List)this.MOD_BADGES.get()).forEach((badge) -> {
            String[] badgeKeyValue = ((String) badge).split("=");
            if (badgeKeyValue.length != 1) {
                Set<String> badges = new LinkedHashSet();
                Set<String> disabledBadges = new LinkedHashSet();
                Arrays.stream(badgeKeyValue[1].split(", ")).toList().forEach((badgeId) -> {
                    if (badgeId.startsWith("!")) {
                        disabledBadges.add(badgeId.substring(1));
                    } else {
                        badges.add(badgeId);
                    }

                });
                this.mod_badges.put(badgeKeyValue[0], badges);
                this.disabled_mod_badges.put(badgeKeyValue[0], disabledBadges);
            }

        });
        if (!((List)this.LIBRARY_LIST.get()).isEmpty()) {
            ((List)this.LIBRARY_LIST.get()).forEach((string) -> {
                this.mod_badges.putIfAbsent((String) string, new LinkedHashSet());
                ((Set)this.mod_badges.get(string)).add("library");
            });
            this.LIBRARY_LIST.set(new ArrayList());
        }

        Map<String, Mod> dummyParents = new HashMap();
        HashSet<String> modParentSet = new HashSet();
        Map<String, List<String>> modParents = new HashMap();
        ((List)this.MOD_PARENTS.get()).forEach((parentToMods) -> {
            if (!((String)parentToMods).isEmpty()) {
                String[] parentToMod = ((String) parentToMods).split("=");
                modParents.put(parentToMod[0], Arrays.stream(parentToMod[1].split(", ")).toList());
            }
        });
        modParents.forEach((parentString, children) -> {
            Iterator var6 = children.iterator();

            while(true) {
                Mod mod;
                while(true) {
                    if (!var6.hasNext()) {
                        return;
                    }

                    String id = (String)var6.next();
                    mod = (Mod)ModMenu.MODS.getOrDefault(id, (Mod)dummyParents.get(id));
                    if (mod != null) {
                        break;
                    }

                    Mod fakeModHost = this.getModHost(modParents, dummyParents, id);
                    if (fakeModHost != null) {
                        mod = new NeoforgeDummyParentMod(fakeModHost, id);
                        dummyParents.put(id, mod);
                        break;
                    }
                }

                String parentId = parentString;
                modParentSet.clear();

                Mod parent;
                while(true) {
                    parent = (Mod)ModMenu.MODS.getOrDefault(parentId, (Mod)dummyParents.get(parentId));
                    if (parent == null) {
                        parent = new NeoforgeDummyParentMod(mod, parentId);
                        dummyParents.put(parentId, parent);
                    }

                    parentId = parent != null ? parent.getParent() : null;
                    if (parentId == null) {
                        break;
                    }

                    if (modParentSet.contains(parentId)) {
                        ModMenu.LOGGER.warn("Mods contain each other as parents: {}", modParentSet);
                        parent = null;
                        break;
                    }

                    modParentSet.add(parentId);
                }

                if (parent != null) {
                    ModMenu.ROOT_MODS.remove(mod.getId(), mod);
                    ModMenu.PARENT_MAP.put(parent, mod);
                }
            }
        });
        ModMenu.MODS.putAll(dummyParents);
    }

    private Mod getModHost(Map<String, List<String>> modParents, Map<String, Mod> dummyParents, String id) {
        if (!modParents.containsKey(id)) {
            return null;
        } else {
            String hostId = (String)((List)modParents.get(id)).getFirst();
            if (hostId == null) {
                return null;
            } else {
                Mod host = (Mod)ModMenu.MODS.get(hostId);
                if (host == null) {
                    host = this.getModHost(modParents, dummyParents, hostId);
                    if (host == null) {
                        return null;
                    }

                    host = new NeoforgeDummyParentMod(host, hostId);
                    dummyParents.put(id, host);
                }

                return (Mod)host;
            }
        }
    }

    public void save() {
        List<String> list = new ArrayList();
        this.mod_badges.forEach((key, values) -> {
            Set<String> disabledBadges = (Set)this.disabled_mod_badges.get(key);
            if (!values.isEmpty() || disabledBadges != null) {
                StringBuilder string = new StringBuilder();

                Iterator var6;
                String value;
                for(var6 = values.iterator(); var6.hasNext(); string.append(value)) {
                    value = (String)var6.next();
                    if (!string.isEmpty()) {
                        string.append(", ");
                    }
                }

                if (disabledBadges != null) {
                    for(var6 = disabledBadges.iterator(); var6.hasNext(); string.append("!").append(value)) {
                        value = (String)var6.next();
                        if (!string.isEmpty()) {
                            string.append(", ");
                        }
                    }
                }

                if (!string.isEmpty()) {
                    list.add(key + "=" + String.valueOf(string));
                }

            }
        });
        this.MOD_BADGES.set(list);
        ((ModConfigSpec)ModMenu.CONFIG.getRight()).save();
    }
    public enum GameMenuButtonStyle {
        @SerializedName(
                value = "replace",
                alternate = {"replace_bugs"}
        )
        REPLACE,
        @SerializedName(
                value = "insert",
                alternate = {"below_bugs"}
        )
        INSERT,
        ICON;
    }
    public enum ModCountLocation {
        TITLE_SCREEN(true, false),
        MODS_BUTTON(false, true),
        TITLE_SCREEN_AND_MODS_BUTTON(true, true),
        NONE(false, false);

        private final boolean titleScreen;
        private final boolean modsButton;

        private ModCountLocation(boolean titleScreen, boolean modsButton) {
            this.titleScreen = titleScreen;
            this.modsButton = modsButton;
        }

        public boolean isOnTitleScreen() {
            return this.titleScreen;
        }

        public boolean isOnModsButton() {
            return this.modsButton;
        }
    }
    public enum Sorting {
        ASCENDING(Comparator.comparing((mod) -> {
            return mod.getTranslatedName().toLowerCase(Locale.ROOT);
        })),
        DESCENDING(ASCENDING.getComparator().reversed());

        private final Comparator<Mod> comparator;

        private Sorting(Comparator<Mod> comparator) {
            this.comparator = comparator;
        }

        public Comparator<Mod> getComparator() {
            return this.comparator;
        }

        public void cycleValue() {
            ModMenu.getConfig().SORTING.set(values()[this.ordinal() + 1 == values().length ? 0 : this.ordinal() + 1]);
        }
    }
    public enum TitleMenuButtonStyle {
        CLASSIC,
        REPLACE_REALMS,
        SHRINK,
        SHRINK_LEFT,
        ICON;
    }
}
