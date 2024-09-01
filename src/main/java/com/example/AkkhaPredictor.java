package com.example;

import com.example.enemydata.Enemy;
import com.example.events.EntityDamaged;
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
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Akkha Predictor"
)
public class AkkhaPredictor extends Plugin
{
	@Inject
	private Client client;

	@Inject
	ClientThread clientThread;

	@Inject
	private AkkhaPredictorConfig config;

	@Inject
	private AkkhaPredictorOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PartyService party;

	@Inject
	private WSClient wsClient;


	/**
	 * Map of current XP amounts in each skill
	 */
	private Map<Skill, Integer> previousXps;

	@Getter
	private final Map<Integer, Enemy> activeEnemies = new HashMap<>();

	@Getter
	private Predictor predictor = new Predictor();

	private static final Set<Integer> POWERED_STAVES = new HashSet<>(Arrays.asList(
			ItemID.SANGUINESTI_STAFF,
			ItemID.TRIDENT_OF_THE_SEAS_FULL,
			ItemID.TRIDENT_OF_THE_SEAS,
			ItemID.TRIDENT_OF_THE_SWAMP,
			ItemID.TRIDENT_OF_THE_SWAMP_E,
			ItemID.HOLY_SANGUINESTI_STAFF,
			ItemID.TUMEKENS_SHADOW,
			ItemID.CORRUPTED_TUMEKENS_SHADOW
	));

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(this::initXpMap);
		overlayManager.add(overlay);
		wsClient.registerMessage(EntityDamaged.class);
	}

	private void initXpMap() {
		previousXps = new HashMap<>();
		Arrays.stream(Skill.values()).forEach(skill -> previousXps.put(skill, client.getSkillExperience(skill)));
	}

	/**
	 * @return TOA invocation level
	 */
	private int getInvocation() {
		return client.getVarbitValue(Varbits.TOA_RAID_LEVEL);
	}

	/**
	 * Fetches the path level from the TOA widget
	 * @return Path level
	 */
	private int getPathLevel() {
		Widget pathLevelWidget = client.getWidget(481, 45);
		if (pathLevelWidget == null) {
			return -1;
		}
		return Integer.parseInt(pathLevelWidget.getText());
	}

	/**
	 * Gets amount of people in the raid, amount makes the enemy health scale with 90%
	 * @return Amount of people in the raid
	 */
	private int getPartySize()
	{
		int partySize = 1;
		for (int i = 1; i < 8; i++) {
			if (client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH + i) != 0) {
				partySize++;
			}
		}
		return partySize;
	}

	private boolean partyDead() {
		boolean alldead = false;

		for (int i = Varbits.TOA_MEMBER_0_HEALTH; i <= Varbits.TOA_MEMBER_7_HEALTH; i++) {
			 alldead |= client.getVarbitValue(i) == 1;
		}

		return alldead;
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		if (partyDead()) {
			// TODO: does this need to be done each tick?
			activeEnemies.clear();
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged state) {
        if (Objects.requireNonNull(state.getGameState()) == GameState.LOGGED_IN) {
            initXpMap();
        }
	}

	@Subscribe
	protected void onNpcDespawned(NpcDespawned event) {
        activeEnemies.remove(event.getNpc().getIndex());
		for (Enemy enemy : activeEnemies.values()) {
			enemy.nearbyDied(event.getNpc());
		}
	}

	@Subscribe
	protected void onNpcSpawned(NpcSpawned event) {
		NPC npc = event.getNpc();
		TriFunction<NPC, Integer, Integer, Integer, Enemy> constructor = Enemy.enemies.getOrDefault(npc.getId(), null);
		if (constructor == null) {
			return;
		}

		Enemy enemy = constructor.apply(npc, getInvocation(), getPartySize(), getPathLevel());

		activeEnemies.put(npc.getIndex(), enemy);
	}

	/**
	 * Create the new Akkha object when the TOA path widget has loaded, we aren't able to get the path level otherwise.
	 * @param event
	 */
	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		// 481 is the TOA path/time/etc. widget
		if (event.getGroupId() != InterfaceID.TOA_RAID) {
			return;
		}
		int invo = getInvocation();
		int pathLevel = getPathLevel();
		int partySize = getPartySize();
		if (pathLevel < 0 || invo < 0 || partySize <= 0) {
			return;
		}
		for (Enemy enemy : activeEnemies.values()) {
			enemy.fixupStats(invo, partySize, pathLevel);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		previousXps = null;
	}

	private boolean isAtToa() {
		final int []TOA_REGIONS = {
				13455, // Lobby
				14160, // Nexus

				15698, // Crondis
				15700, // Zebak

				14162, // Scabaras
				14164, // Kephri

				15186, // Apmken
				15188, // Baba

				14674, // Het
				14676, // Akkha

				15184, // Wardens
				15696, // Wardens

				14672, // Tomb
		};

		return Arrays.stream(client.getMapRegions()).anyMatch(current -> Arrays.stream(TOA_REGIONS).anyMatch(reg -> reg == current));
	}

	/**
	 * Processes the xp drop and turns it into how much damage it did. Not entirely accurate due to fractional XP.
	 * Rounds down so that in case it does get fractional it should not give false positives.
	 *
	 * @param skill Skill that xp was received in.
	 * @param xp Amount of XP that was received.
	 */
	private void processXpDrop(Skill skill, int xp) {
		if (skill == Skill.HITPOINTS) {
			return;
		}

		Player player = Objects.requireNonNull(client.getLocalPlayer());
		Actor entity = player.getInteracting();
		if (!(entity instanceof NPC) || !isAtToa()) {
			return;
		}

		PlayerComposition playerComposition = player.getPlayerComposition();
		NPC npc = (NPC) entity;
		Enemy enemy;
		if (activeEnemies.containsKey(npc.getIndex())) {
			enemy = activeEnemies.get(npc.getIndex());
		} else {
			// Construct a new enemy
			enemy = Enemy.enemies.get(npc.getId()).apply(npc, getInvocation(), getPartySize(), getPathLevel());
			activeEnemies.put(npc.getIndex(), enemy);
		}

		int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		int weapon = playerComposition.getEquipmentId(KitType.WEAPON);

		boolean isDefensiveCast = attackStyle == 3;
		boolean isPoweredStaff = POWERED_STAVES.contains(weapon);
		Predictor.Properties props = new Predictor.Properties(skill, isDefensiveCast, isPoweredStaff, npc);
		System.out.println();
		double scaling = enemy.getModifier();
		if ((skill == Skill.RANGED || skill == Skill.MAGIC) && isDefensiveCast) {
			// Ignore in order to not double hit, insert the drop into
			// the tree in order to track the fraction
			predictor.insertInto(xp, scaling, props);
			return;
		}
		int damage = predictor.treePredict(xp, scaling, props);
		assert (damage >= 0);

		if (damage > 0) {
			System.out.println("Predicted: " + damage);
		}
		sendDamage(player, damage);
	}

	private void sendDamage(Player player, int damage) {
		if (damage <= 0) {
			return;
		}

		NPC npc = (NPC) player.getInteracting();

		final int npcIndex = npc.getIndex();
		final EntityDamaged entityDamaged = new EntityDamaged(npcIndex, damage);

		if (party.isInParty()) {
			clientThread.invokeLater(() -> party.send(entityDamaged));
		}
		onEntityDamaged(entityDamaged);
	}

	/**
	 * Computes the difference between the updated xp drop and the previous one
	 * @param skill Skill XP was received in.
	 * @param xp Updated XP amount.
	 */
	private void preProcessXpDrop(Skill skill, int xp) {
		if (!isAtToa()) {
			return;
		}

		int diff = xp - previousXps.getOrDefault(skill, 0);
		previousXps.put(skill, xp);
		processXpDrop(skill, diff);
	}

	/**
	 * Queues damage on an entity. If the entity is a shadow, it hides the shadow.
	 * If the entity is Akkha, it will highlight her.
	 * @param entityDamaged event
	 */
	@Subscribe
	public void onEntityDamaged(EntityDamaged entityDamaged) {
		if (!isAtToa()) {
			return;
		}

		PartyMember localPlayer = party.getLocalMember();

		if (localPlayer != null) {
			if (localPlayer.getMemberId() == entityDamaged.getMemberId()) {
				return; // Don't process your own events
			}
		}

		Integer npcIndex = entityDamaged.getNpcIndex();
		Enemy enemy = activeEnemies.getOrDefault(npcIndex, null);
		assert (enemy != null);
		enemy.queueDamage(entityDamaged.getDamage());
	}

	@Subscribe
	public void onStatChanged(StatChanged xpDrop) {
		preProcessXpDrop(xpDrop.getSkill(), xpDrop.getXp());
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop xpDrop) {
		//TODO hopefully this works. I don't have 200m in magic to find out.
		processXpDrop(xpDrop.getSkill(), xpDrop.getXp());
	}

	/**
	 * Removes the hit from queued damage.
	 * @param hit hitsplat
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hit) {
		if (!isAtToa()) {
			return;
		}
		Actor actor = hit.getActor();
		if (actor instanceof NPC) {
			NPC npc = (NPC) actor;
			Enemy enemy = activeEnemies.getOrDefault(npc.getIndex(), null);
			if (enemy == null) {
				System.out.println("Unknown target");
				return;
			}
			int amount = hit.getHitsplat().getAmount();
			System.out.println("Damage: " + amount + " " + hit.getActor().getName());
			enemy.hit(amount);
		}
	}


	@Provides
	AkkhaPredictorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaPredictorConfig.class);
	}
}
