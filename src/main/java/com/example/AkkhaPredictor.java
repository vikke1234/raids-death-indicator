package com.example;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.het.Akkha;
import com.example.events.EntityDamaged;
import com.example.raids.Toa;
import com.example.utils.DamageHandler;
import com.example.utils.Predictor;
import com.example.utils.TriFunction;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Raid Death Indicator"
)
public class AkkhaPredictor extends Plugin {
	@Inject
	private AkkhaPredictorOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WSClient wsClient;

	@Inject
	private DamageHandler damageHandler;

	@Inject
	private Toa toa;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(damageHandler);
		eventBus.register(toa);
		clientThread.invoke(damageHandler::initXpMap);
		overlayManager.add(overlay);
		wsClient.registerMessage(EntityDamaged.class);
	}
	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		eventBus.unregister(damageHandler);
		eventBus.unregister(toa);
		wsClient.unregisterMessage(EntityDamaged.class);
		damageHandler.getPredictor().reset();
	}

	@Provides
	AkkhaPredictorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaPredictorConfig.class);
	}
}
