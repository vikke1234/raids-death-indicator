package com.example;

import java.awt.Font;

public enum HpOverlayFontStyle {
    PLAIN(Font.PLAIN, "Plain"),
    BOLD(Font.BOLD, "Bold"),
    ITALIC(Font.ITALIC, "Italic"),
    BOLD_ITALIC(Font.BOLD | Font.ITALIC, "Bold italic");

    private final int awtStyle;
    private final String displayName;

    HpOverlayFontStyle(int awtStyle, String displayName) {
        this.awtStyle = awtStyle;
        this.displayName = displayName;
    }

    public int awtStyle() {
        return awtStyle;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
