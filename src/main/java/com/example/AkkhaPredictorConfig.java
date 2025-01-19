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

    // TODO: add hidden options to store the fraction
}
