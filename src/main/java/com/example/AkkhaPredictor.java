package com.example;

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
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

	@Inject
	private ModelOutlineRenderer renderer;

	/**
	 * Map of current XP amounts in each skill
	 */
	private Map<Skill, Integer> previousXps;

	@Getter
	private Akkha akkha;

	/**
	 * NPC Index -> Shadow
	 */
	@Getter
	private final Map<Integer, AkkhaShadow> shadows = new HashMap<>();

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

	@Subscribe
	protected void onGameStateChanged(GameStateChanged state) {
        if (Objects.requireNonNull(state.getGameState()) == GameState.LOGGED_IN) {
            initXpMap();
        }
	}

	@Subscribe
	protected void onNpcDespawned(NpcDespawned event) {
		String name = event.getNpc().getName();
		if (name == null) {
			return;
		}

		if (Objects.equals(Text.escapeJagex(name), "Akkha's Shadow")) {
			akkha.setCanPhase(true);
			shadows.clear();
		}
	}

	@Subscribe
	protected void onNpcSpawned(NpcSpawned event) {
		akkha.setShouldDraw(false);
		akkha.setCanPhase(false);
		String name = event.getNpc().getName();
		if (name == null) {
			return;
		}

        if (Objects.equals(Text.escapeJagex(name), "Akkha's Shadow")) {
			AkkhaShadow shadow = new AkkhaShadow(getInvocation(), getPartySize(), getPathLevel(), event.getNpc());
			shadows.put(event.getNpc().getIndex(), shadow);
		}
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
		List<NPC> npcs = client.getNpcs().stream()
				.filter(entity -> Objects.equals(entity.getName(), "Akkha"))
				.collect(Collectors.toList());
		int pathLevel = getPathLevel();
		int invo = getInvocation();
		int partySize = getPartySize();
		if (pathLevel < 0 || invo < 0 || partySize <= 0) {
			return;
		}

		akkha = new Akkha(invo, partySize, pathLevel);
	}

	@Override
	protected void shutDown() throws Exception
	{
		previousXps = null;
	}

	private boolean isAtAkkha() {
		final int AKKHA_REGION_ID = 14676;
		return Arrays.stream(client.getMapRegions()).anyMatch(region -> region == AKKHA_REGION_ID);
	}

	private boolean isInToa() {
		return true;
	}

	/**
	 * Processes the xp drop and turns it into how much damage it did. Not entirely accurate due to fractional XP.
	 * Rounds down so that in case it does get fractional it should not give false positives.
	 *
	 * @param skill Skill that xp was received in.
	 * @param xp Amount of XP that was received.
	 */
	private void processXpDrop(Skill skill, int xp) {
		Player player = Objects.requireNonNull(client.getLocalPlayer());
		Actor entity = player.getInteracting();
		if (!(entity instanceof NPC) || !isAtAkkha()) {
			return;
		}

		PlayerComposition playerComposition = player.getPlayerComposition();
		NPC npc = (NPC) entity;
		int scaled;
		if (Text.escapeJagex(Objects.requireNonNull(npc.getName())).equals("Akkha")) {
			scaled = akkha.scaleXpDrop(xp);
		} else {
			scaled = shadows.get(npc.getIndex()).scaleXpDrop(xp);
		}
		int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		int weapon = playerComposition.getEquipmentId(KitType.WEAPON);

		boolean isDefensiveCast = attackStyle == 3;
		boolean isPoweredStaff = POWERED_STAVES.contains(weapon);

		int damage = 0;
		switch (skill) {
			case DEFENCE:
				if (isPoweredStaff && isDefensiveCast) {
					damage = scaled;
					System.out.println("Predicted damage: " + damage + ", xp: " + xp + ", scaled: " + scaled + ", modifier: " + akkha.getModifier());
				}
				break;
			case MAGIC:
				if (isPoweredStaff && !isDefensiveCast) {
					damage = (int) ((double) scaled / 2.0D);
				}
				break;
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
		if (!isInToa()) {
			isAccurate = false;
		}
		if (!isAtAkkha()) {
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
		if (!isAtAkkha()) {
			return;
		}

		PartyMember localPlayer = party.getLocalMember();

		if (localPlayer != null) {
			if (localPlayer.getMemberId() == entityDamaged.getMemberId()) {
				return; // Don't process your own events
			}
		}

		Integer npcIndex = entityDamaged.getNpcIndex();;
		if (shadows.containsKey(npcIndex)) {
			AkkhaShadow shadow = shadows.get(npcIndex);
			boolean isDead = shadow.queueDamage(entityDamaged.getDamage());
			if (isDead) {
				Optional<NPC> client_shadow = client.getNpcs().stream().filter(npc -> npc.getIndex() == npcIndex).findFirst();
				client_shadow.ifPresent(s -> s.setDead(true));
			}
		} else {
			akkha.queueDamage(entityDamaged.getDamage());
		}
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
		if (!isAtAkkha()) {
			return;
		}

		if (Objects.equals(hit.getActor().getName(), "Akkha")) {
			akkha.hit(hit.getHitsplat().getAmount());
			System.out.println("Actual damage dealt: " + hit.getHitsplat().getAmount());
		} else {
			if (hit.getActor() instanceof NPC &&
					Text.escapeJagex(Objects.requireNonNull(hit.getActor().getName())).equals("Akkha's Shadow")) {
				NPC npc = (NPC) hit.getActor();
				AkkhaShadow shadow = shadows.get(npc.getIndex());
				shadow.hit(hit.getHitsplat().getAmount());
			}
		}
	}


	@Provides
	AkkhaPredictorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaPredictorConfig.class);
	}
}
