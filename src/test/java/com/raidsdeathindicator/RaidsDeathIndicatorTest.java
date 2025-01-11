package com.raidsdeathindicator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RaidsDeathIndicatorTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AkkhaPredictor.class);
		RuneLite.main(args);
	}
}