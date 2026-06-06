package com.example;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("raid-death-indicator")
public interface AkkhaPredictorConfig extends Config {

    @ConfigSection(
            name = "HP overlay",
            description = "Visual settings for the per-NPC HP overlay text.",
            position = 100,
            closedByDefault = true
    )
    String hpOverlaySection = "hpOverlaySection";

    @ConfigSection(
            name = "Advanced",
            description = "Experimental / debugging toggles. Don't touch unless you know what they do.",
            position = 200,
            closedByDefault = true
    )
    String advancedSection = "advancedSection";

    @Alpha
    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight color",
            description = ""
    )
    default Color highlightColor() {
        return new Color(98, 174, 253, 64);
    }

    @ConfigItem(
            keyName = "maxHp",
            name = "Max HP",
            description = "Max HP in CoX, affects scales"
    )
    default int maxHp() {
        return 99;
    }

    @ConfigItem(
            keyName = "showStatus",
            name = "Show predictor status",
            description = "Show or hide the box in top left"
    )
    default boolean status() {
        return true;
    }

    @ConfigItem(
            keyName = "isCm",
            name = "Challenge mode",
            description = "To use challenge mode stats or not."
    )
    default boolean isCM() {
        return true;
    }

    // ---------------- HP overlay section ----------------

    @ConfigItem(
            keyName = "enableHpOverlay",
            name = "Enable HP overlay",
            description = "Draws the tracked health on the boss, please use this when reporting bugs",
            section = hpOverlaySection
    )
    default boolean enableHpOverlay() {
        return false;
    }

    @Alpha
    @ConfigItem(
            keyName = "textColor",
            name = "Text color",
            description = "",
            section = hpOverlaySection
    )
    default Color textColor() {
        return new Color(98, 174, 253, 64);
    }

    @ConfigItem(
            keyName = "hpOverlayHeightOffset",
            name = "HP overlay height offset",
            description = "Pixels above the NPC's top edge to draw the HP overlay text. Larger = higher.",
            section = hpOverlaySection
    )
    default int hpOverlayHeightOffset() {
        return 14;
    }

    @ConfigItem(
            keyName = "hpOverlayFontSize",
            name = "HP overlay font size",
            description = "Point size of the HP overlay text.",
            section = hpOverlaySection
    )
    default int hpOverlayFontSize() {
        return 14;
    }

    @ConfigItem(
            keyName = "hpOverlayFormat",
            name = "HP overlay format",
            description = "<html>Tokens:"
                    + "<br>&nbsp;&nbsp;%ch = current HP"
                    + "<br>&nbsp;&nbsp;%qh = queued HP (current minus queued damage)"
                    + "<br>&nbsp;&nbsp;%qd = queued damage"
                    + "<br><br>Wrap a section in %[...] to hide it when a token inside is 0."
                    + "<br>Example: \"%ch%[ (%qd)]\" hides the parens when no damage is queued."
                    + "</html>",
            section = hpOverlaySection
    )
    default String hpOverlayFormat() {
        return "%ch%[ (%qd)]";
    }

    // ---------------- Advanced section ----------------

    @ConfigItem(
            keyName = "conservativeHitPrediction",
            name = "Conservative hit prediction",
            description = "Will not try to use internal fractions until the calibration is done",
            section = advancedSection
    )
    default boolean experimentalHitPrediction() {
        return false;
    }

}
