package com.terraformersmc.modmenu;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.Component;

public class TextPlaceholderApiCompat {
    private static volatile NodeParser PARSER = null;
    private static volatile boolean INIT_ATTEMPTED = false;
    private static volatile boolean INIT_FAILED = false;

    private static NodeParser createParser() {
        try {
            return NodeParser.builder().quickText().build();
        } catch (Throwable var1) {
            ModMenu.LOGGER.warn("Placeholder API parser failed to initialize; placeholder parsing disabled. This is expected on NeoForge 1.21.10.");
            return null;
        }
    }

    private static NodeParser getParser() {
        if (INIT_FAILED) {
            return null;
        } else {
            NodeParser p = PARSER;
            if (p == null && !INIT_ATTEMPTED) {
                Class var1 = TextPlaceholderApiCompat.class;
                synchronized(TextPlaceholderApiCompat.class) {
                    if (!INIT_ATTEMPTED) {
                        INIT_ATTEMPTED = true;
                        PARSER = p = createParser();
                        if (p == null) {
                            INIT_FAILED = true;
                        }
                    } else {
                        p = PARSER;
                    }
                }
            }

            return p;
        }
    }

    public static Component parseText(String input, ParserContext ctx) {
        if (INIT_FAILED) {
            return Component.literal(input == null ? "" : input);
        } else {
            try {
                NodeParser p = getParser();
                if (p != null) {
                    return p.parseComponent(input, ctx);
                }
            } catch (Throwable var3) {
                if (!INIT_FAILED) {
                    INIT_FAILED = true;
                    ModMenu.LOGGER.warn("Placeholder parsing failed at runtime, disabling");
                }
            }

            return Component.literal(input == null ? "" : input);
        }
    }
}
