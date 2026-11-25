package com.example;

import com.example.events.EntityDamaged;
import com.example.generated.GeneratedEnemyConfig;
import com.example.raids.Cox;
import com.example.raids.Misc;
import com.example.raids.Tfa;
import com.example.raids.Toa;
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
	private Toa toa;

	@Inject
	private Cox cox;

	@Inject
	private Tfa tfa;

	@Inject
	private Misc misc;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Override
	protected void startUp() throws Exception
	{
		// The generated config interface extends AkkhaPredictorConfig, so RuneLite's
		// automatic default registration only sees the generated methods. Register
		// the inherited ones too; enum config items crash the panel when unset.
		configManager.setDefaultConfiguration(configManager.getConfig(AkkhaPredictorConfig.class), false);

		eventBus.register(damageHandler);
		eventBus.register(toa);
		eventBus.register(cox);
		eventBus.register(tfa);
		eventBus.register(misc);
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
		eventBus.unregister(tfa);
		eventBus.unregister(misc);
		wsClient.unregisterMessage(EntityDamaged.class);
		damageHandler.getPredictor().reset();
	}

	@Provides
	GeneratedEnemyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GeneratedEnemyConfig.class);
	}
}
