package com.example;

import com.example.events.EntityDamaged;
import com.example.raids.Cox;
import com.example.raids.Toa;
import com.example.utils.AnimationIdentifier;
import com.example.utils.DamageHandler;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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
	AnimationIdentifier animationIdentifier;

	@Inject
	private Toa toa;

	@Inject
	private Cox cox;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(damageHandler);
		eventBus.register(toa);
		eventBus.register(cox);
		eventBus.register(animationIdentifier);
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
		eventBus.unregister(cox);
		eventBus.unregister(animationIdentifier);
		wsClient.unregisterMessage(EntityDamaged.class);
		damageHandler.getPredictor().reset();
	}

	@Provides
	AkkhaPredictorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaPredictorConfig.class);
	}
}
