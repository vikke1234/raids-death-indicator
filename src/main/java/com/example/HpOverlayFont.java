package com.example;

import net.runelite.client.ui.FontManager;

import java.awt.Font;
import java.util.function.Supplier;

public enum HpOverlayFont {
    RUNESCAPE("RuneScape", FontManager::getRunescapeFont),
    RUNESCAPE_BOLD("RuneScape bold", FontManager::getRunescapeBoldFont),
    RUNESCAPE_SMALL("RuneScape small", FontManager::getRunescapeSmallFont),
    RUNELITE("RuneLite default", FontManager::getDefaultFont),
    RUNELITE_BOLD("RuneLite default bold", FontManager::getDefaultBoldFont),
    MONOSPACE("Monospaced", () -> new Font(Font.MONOSPACED, Font.PLAIN, 14));

    private final String displayName;
    private final Supplier<Font> baseFont;

    HpOverlayFont(String displayName, Supplier<Font> baseFont) {
        this.displayName = displayName;
        this.baseFont = baseFont;
    }

    public Font create(int size, HpOverlayFontStyle style) {
        int clampedSize = Math.max(6, Math.min(72, size));
        return baseFont.get().deriveFont(style.awtStyle(), (float) clampedSize);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
