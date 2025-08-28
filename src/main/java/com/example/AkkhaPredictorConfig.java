package com.example;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("raid-death-indicator")
public interface AkkhaPredictorConfig extends Config
{
    @Alpha
    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight color",
            description = ""
    )
    default Color highlightColor() {
        return new Color(98, 174, 253, 64);
    }

    @Alpha
    @ConfigItem(
            keyName = "textColor",
            name = "Text color",
            description = ""
    )
    default Color textColor() {
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
            keyName = "enableCox",
            name = "Enable cox (experimental)",
            description = ""
    )
    default boolean enableCox() {
        return true;
    }

    @ConfigItem(
            keyName = "enableExperimentalHitPrediction",
            name = "Hit prediction (experimental)",
            description = ""
    )
    default boolean experimentalHitPrediction() {
        return false;
    }

    @ConfigItem(
            keyName = "enableHpOverlay",
            name = "Enable HP overlay",
            description = "Draws the tracked health on the boss, please use this when reporting bugs"
    )
    default boolean enableHpOverlay() {
        return false;
    }


}
