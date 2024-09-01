package com.example;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("akkha-predictor")
public interface AkkhaPredictorConfig extends Config
{
	// TODO: make this customizable?
    @Alpha
    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight color",
            description = ""
    )
    default Color highlightColor() {
        return new Color(0x85, 0xC1, 0xFF, 20);
    }

    // TODO: add hidden options to store the fraction
}
