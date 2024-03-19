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
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Example"
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
	private ModelOutlineRenderer renderer;

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
	}

	private void initXpMap() {
		previousXps = new HashMap<>();
		Arrays.stream(Skill.values()).forEach(skill -> previousXps.put(skill, client.getSkillExperience(skill)));
	}

	private int getInvocation() {

		return client.getVarbitValue(Varbits.TOA_RAID_LEVEL);
	}

	private int getPathLevel() {
		Widget pathLevelWidget = client.getWidget(481, 45);
		if (pathLevelWidget == null) {
			return -1;
		}
		return Integer.parseInt(pathLevelWidget.getText());
	}

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
		switch (state.getGameState()) {
			case LOGGED_IN:
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
			AkkhaShadow shadow = new AkkhaShadow(getInvocation(), getPartySize(), getPathLevel(), event.getNpc(), null);
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

	private void processXpDrop(Skill skill, int xp) {
		Player player = Objects.requireNonNull(client.getLocalPlayer());
		Actor entity = player.getInteracting();
		if (!(entity instanceof NPC) || !isAtAkkha()) {
			return;
		}

		PlayerComposition playerComposition = player.getPlayerComposition();
		int scaled = akkha.scaleXpDrop(xp);
		int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		int weapon = playerComposition.getEquipmentId(KitType.WEAPON);

		boolean isDefensiveCast = attackStyle == 3;
		boolean isPoweredStaff = POWERED_STAVES.contains(weapon);

		int damage = 0;
		switch (skill) {
			case DEFENCE:
				if (isPoweredStaff && isDefensiveCast) {
					damage = scaled;
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
		final NpcDamaged npcDamaged = new NpcDamaged(npcIndex, damage);

		if (party.isInParty()) {
			clientThread.invokeLater(() -> party.send(npcDamaged));
		}
		onNpcDamaged(npcDamaged);
	}

	private void preProcessXpDrop(Skill skill, int xp) {
		int diff = xp - previousXps.getOrDefault(skill, 0);
		previousXps.put(skill, xp);
		processXpDrop(skill, diff);
	}



	@Subscribe
	public void onNpcDamaged(NpcDamaged npcDamaged) {
		if (!isAtAkkha()) {
			return;
		}
		Integer npcIndex = npcDamaged.getNpcIndex();;
		if (shadows.containsKey(npcIndex)) {
			AkkhaShadow shadow = shadows.get(npcIndex);
			boolean isDead = shadow.queueDamage(npcDamaged.getDamage());
			if (isDead) {
				Optional<NPC> client_shadow = client.getNpcs().stream().filter(npc -> npc.getIndex() == npcIndex).findFirst();
				client_shadow.ifPresent(s -> s.setDead(true));
			}
		} else {
			akkha.queueDamage(npcDamaged.getDamage());
		}
	}
	@Subscribe
	public void onStatChanged(StatChanged xpDrop) {
		if (!isAtAkkha()) {
			return;
		}

		preProcessXpDrop(xpDrop.getSkill(), xpDrop.getXp());
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop xpDrop) {
		//TODO
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hit) {
		if (Objects.equals(hit.getActor().getName(), "Akkha")) {
			akkha.hit(hit.getHitsplat().getAmount());
		}
	}


	@Provides
	AkkhaPredictorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaPredictorConfig.class);
	}
}
