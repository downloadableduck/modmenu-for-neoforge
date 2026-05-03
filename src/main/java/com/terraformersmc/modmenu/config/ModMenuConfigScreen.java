package com.terraformersmc.modmenu.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import com.terraformersmc.modmenu.ModMenu;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.CaptionBasedToString;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.TranslationChecker;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ListValueSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Range;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.jetbrains.annotations.Nullable;

public class ModMenuConfigScreen extends ConfigurationSectionScreen {
    private static final CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, aBoolean) -> {
        ComponentContents patt0$temp = component.getContents();
        if (patt0$temp instanceof TranslatableContents) {
            TranslatableContents contents = (TranslatableContents)patt0$temp;
            String var10000 = contents.getKey();
            return Component.translatable(var10000 + "." + aBoolean);
        } else {
            return Component.empty();
        }
    };
    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    private static final String SECTION = "neoforge.configuration.uitext.section";
    private static final String SECTION_TEXT = "neoforge.configuration.uitext.sectiontext";
    private static final String CRUMB = "neoforge.configuration.uitext.breadcrumb";
    protected static final TranslationChecker translationChecker = new TranslationChecker();

    public ModMenuConfigScreen(Screen parent, Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    public ModMenuConfigScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key, Set<? extends Entry> entrySet, Component title) {
        super(parentContext, parent, valueSpecs, key, entrySet, title);
    }

    @Nullable
    protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
        return subconfig.isEmpty() ? null : new Element(Component.translatable("neoforge.configuration.uitext.section", new Object[]{this.getTranslationComponent(key)}), this.getTooltipComponent(key, (Range)null), Button.builder(Component.translatable("neoforge.configuration.uitext.section", new Object[]{Component.translatable(translationChecker.check(this.getTranslationKey(key) + ".button", "neoforge.configuration.uitext.sectiontext"))}), (button) -> {
            this.minecraft.setScreen((Screen)this.sectionCache.computeIfAbsent(key, (k) -> {
                return (new ModMenuConfigScreen(this.context, this, subconfig.valueMap(), key, subsection.entrySet(), Component.translatable(this.getTranslationKey(key)))).rebuild();
            }));
        }).tooltip(Tooltip.create(this.getTooltipComponent(key, (Range)null))).width(150).build(), false);
    }


    @Nullable
    protected Element createBooleanValue(String key, ValueSpec spec, Supplier<Boolean> source, Consumer<Boolean> target) {
        return !key.contains("modify") && !key.contains("config_mode") && !key.contains("drag_and_drop") ? new Element(this.getTranslationComponent(key), this.getTooltipComponent(key, (Range)null), new OptionInstance(this.getTranslationKey(key), this.getTooltip(key, (Range)null), BOOLEAN_TO_STRING, Custom.BOOLEAN_VALUES_NO_PREFIX, (Boolean)source.get(), key.contains("count") ? (newValue) -> {
            ModMenu.clearModCountCache();
            this.undoManager.add((v) -> {
                target.accept((Boolean) v);
                this.onChanged(key);
            }, newValue, (v) -> {
                target.accept((Boolean) v);
                this.onChanged(key);
            }, (Boolean)source.get());
        } : (newValue) -> {
            this.undoManager.add((v) -> {
                target.accept((Boolean) v);
                this.onChanged(key);
            }, newValue, (v) -> {
                target.accept((Boolean) v);
                this.onChanged(key);
            }, (Boolean)source.get());
        })) : super.createBooleanValue(key, spec, source, target);
    }

    @Nullable
    protected <T extends Enum<T>> Element createEnumValue(String key, ValueSpec spec, Supplier<T> source, Consumer<T> target) {
        Class<T> clazz = (Class) spec.getClazz();

        assert clazz != null;

        Stream var10000 = Arrays.stream((Enum[])clazz.getEnumConstants());
        Objects.requireNonNull(spec);
        List<T> list = var10000.filter(spec::test).toList();
        return new Element(this.getTranslationComponent(key), this.getTooltipComponent(key, (Range)null), new OptionInstance(this.getTranslationKey(key), this.getTooltip(key, (Range)null), (caption, displayvalue) -> {
            return Component.translatable("modmenu.configuration." + key + "." + ((Enum<?>) displayvalue).name());
        }, new Custom(list), (Enum)source.get(), (newValue) -> {
            this.undoManager.add((v) -> {
                target.accept((T) v);
                this.onChanged(key);
            }, newValue, (v) -> {
                target.accept((T) v);
                this.onChanged(key);
            }, (Enum)source.get());
        }));
    }

    @Nullable
    protected <T> Element createList(String key, ListValueSpec spec, ConfigValue<List<T>> list) {
        return !key.equals("mod_badges") && !key.equals("library_list") ? super.createList(key, spec, list) : null;
    }
}
