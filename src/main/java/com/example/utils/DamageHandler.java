package com.example.utils;

import com.example.AkkhaPredictorConfig;
import com.example.enemydata.Enemy;
import com.example.events.EntityDamaged;
import com.example.raids.Cox;
import com.example.raids.Toa;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Damage handler manages the damaging of enemies, the raid classes places and removes enemies from here.
 */
@Singleton
@Slf4j
public class DamageHandler {
    @Getter
    private final ConcurrentMap<Integer, Enemy> activeEnemies = new ConcurrentHashMap<>();

    @Getter
    private final Predictor predictor = new Predictor();

    @Inject
    private Client client;

    @Inject
    ClientThread clientThread;

    // We need to use the instanced one because getVarbitValue requires it to run on the clientThread
    @Inject
    Cox cox;

    @Inject
    private PartyService party;

    @Inject
    private AkkhaPredictorConfig config;
    /**
     * Map of current XP amounts in each skill
     */
    private Map<Skill, Integer> previousXps;

    private final Set<Skill> validSkills = Set.of(Skill.HITPOINTS);

    private static final Set<Integer> POWERED_STAVES = new HashSet<>(Arrays.asList(
            ItemID.TOTS_CHARGED, // Trident of the seas (full)
            ItemID.TOTS, // Trident of the seas
            ItemID.TOXIC_TOTS_CHARGED, // Toxic trident of the swamp
            ItemID.TOXIC_TOTS_I_CHARGED,
            ItemID.SANGUINESTI_STAFF,
            ItemID.SANGUINESTI_STAFF_OR,
            ItemID.TUMEKENS_SHADOW,
            ItemID.DEADMAN_BLIGHTED_TUMEKENS_SHADOW,
            ItemID.VOIDWAKER));
    private static final Set<Integer> CHINCHOMPAS = Set.of(
            ItemID.CHINCHOMPA_CAPTURED, // Grey chinchompa
            ItemID.CHINCHOMPA_BLACK, // Black chin
            ItemID.CHINCHOMPA_BIG_CAPTURED // Red chinchompa
    );

    // Multi-target multi-hit weapons. XP drop is the sum across all hit
    // NPCs, so we cannot attribute it to a single target — route through
    // handleAoe (same pattern as chinchompa AoE) for clump-kill prediction.
    // Single-target multi-hits (claws, dagger, etc.) don't need special
    // handling: precise(D1)+precise(D2)+precise(D3) = precise(sum) thanks
    // to linearity, so treePredict on the summed XP returns the total
    // damage and the carry tracks correctly.
    private static final Set<Integer> SCYTHES = Set.of(
            ItemID.SCYTHE_OF_VITUR,
            ItemID.SCYTHE_OF_VITUR_OR,
            ItemID.SCYTHE_OF_VITUR_BL,
            ItemID.SCYTHE_OF_VITUR_UNCHARGED,
            ItemID.SCYTHE_OF_VITUR_UNCHARGED_OR,
            ItemID.SCYTHE_OF_VITUR_UNCHARGED_BL);

    private static final Set<Integer> MULTI_HIT_WEAPONS = Set.of(
            ItemID.DRAGON_CLAWS,
            // Burning claws
            ItemID.BONE_CLAWS,
            ItemID.DRAGON_DAGGER,
            ItemID.DRAGON_DAGGER_P,
            ItemID.DRAGON_DAGGER_P_,
            ItemID.DRAGON_DAGGER_P__,
            // Tonalztics of ralos (2-hit standard attack)
            ItemID.TONALZTICS_OF_RALOS_CHARGED,
            ItemID.DRAGON_KNIFE,
            ItemID.DRAGON_KNIFE_P,
            ItemID.DRAGON_KNIFE_P_,
            ItemID.DRAGON_KNIFE_P__);

    public void initXpMap() {
        previousXps = new HashMap<>();
        Arrays.stream(Skill.values()).forEach(skill -> previousXps.put(skill, client.getSkillExperience(skill)));
    }

    public boolean shouldProcess() {
        return Toa.isAtToa(client) || cox.getCachedInCox().get();
    }

    @Subscribe
    protected void onGameStateChanged(GameStateChanged gamestate) {
        if (gamestate == null) {
            return;
        }
        GameState state = Objects.requireNonNull(gamestate.getGameState());
        switch (state) {
            case LOGGED_IN:
                initXpMap();
                break;
            default:
                break;
        }
    }

    /**
     * Processes the xp drop and turns it into how much damage it did. Not entirely accurate due to fractional XP.
     * Rounds down so that in case it does get fractional it should not give false positives.
     *
     * @param skill Skill that xp was received in.
     * @param xp    Amount of XP that was received.
     */
    private void processXpDrop(Skill skill, int xp) {
        if (!validSkills.contains(skill)) {
            return;
        }

        Player player = Objects.requireNonNull(client.getLocalPlayer());
        Actor entity = player.getInteracting();
        if (!(entity instanceof NPC) || !shouldProcess()) {
            return;
        }

        PlayerComposition playerComposition = player.getPlayerComposition();
        NPC npc = (NPC) entity;
        int id = npc.getId();

        if (Enemy.blacklist.contains(id)) {
            predictor.reset();
            // We actually always receive range xp, perhaps this can be used to
            // calculate the magic xp, as you seem to always receive .3 xp from swarms
            return;
        }

        Enemy enemy;
        if (activeEnemies.containsKey(npc.getIndex())) {
            enemy = activeEnemies.get(npc.getIndex());
        } else {
            Trace.damage("xp drop on untracked NPC {} (id={} idx={}) tick={}",
                    npc.getName(), npc.getId(), npc.getIndex(), client.getTickCount());
            predictor.reset();
            return;
        }

        int bossHealth = client.getVarbitValue(VarbitID.HPBAR_HUD_HP);
        if (bossHealth > 0 && Enemy.bosses.contains(npc.getId())) {
            Trace.damage("boss-health varbit re-sync: {} tick={} {} -> {}",
                    npc.getName(), client.getTickCount(), enemy.getCurrentHealth(), bossHealth);
            enemy.setCurrentHealth(bossHealth); // re-synchronize the health
        }

        int attackStyle = client.getVarpValue(VarPlayerID.COM_MODE);
        int weapon = playerComposition.getEquipmentId(KitType.WEAPON);

        boolean isDefensiveCast = attackStyle == 3;
        boolean isPoweredStaff = POWERED_STAVES.contains(weapon);
        boolean isChinchompa = CHINCHOMPAS.contains(weapon);
        boolean isScythe = SCYTHES.contains(weapon);
        double scaling = enemy.getModifier();
        Predictor.Properties props = new Predictor.Properties(skill, isDefensiveCast, isPoweredStaff, npc, scaling);
        if ((skill == Skill.RANGED || skill == Skill.MAGIC) && isDefensiveCast) {
            // Ignore in order to not double hit, insert the drop into
            // the tree in order to track the fraction
            Trace.damage("xp drop {} on {} tick={} (defensive cast, tracking only)",
                    xp, npc.getName(), client.getTickCount());
            predictor.insertInto(xp, props);
            return;
        }

        int damage;
        if (config.experimentalHitPrediction()) {
            damage = predictor.treePredict2(xp, props);
        } else {
            damage = predictor.treePredict(xp, props);
        }
        Trace.damage("xp drop {} -> predicted {} damage on {} tick={} (hp={}, queued={}, calibrated={})",
                xp, damage, npc.getName(), client.getTickCount(),
                enemy.currentHealth, enemy.getQueuedDamage(),
                predictor.isAccurate(skill));

        if (MULTI_HIT_WEAPONS.contains(weapon)) {
            // Reset on multihit weapons as we can't tell the exact hits
            // that build the hitsplat
            predictor.reset();
        }

        assert (damage >= 0);
        if (isChinchompa || isScythe) {
            // Multi-target weapons: total damage is split across nearby NPCs
            // by the per-target hitsplats themselves; we route through
            // handleAoe so death prediction fires only when the summed
            // damage wipes the whole clump. Individual targets are then
            // updated by HitsplatApplied → enemy.hit(amount) as usual.
            handleAoe(props, damage);
        } else {
            sendDamage(player, damage);
        }
    }

    private void handleAoe(Predictor.Properties properties, int damage) {
        NPC npc = properties.npc;
        List<Enemy> nearby = activeEnemies.values().stream()
                .filter(enemy -> npc.getWorldLocation().distanceTo(enemy.getNpc().getWorldLocation()) <= 1)
                .collect(Collectors.toList());

        if (nearby.stream().anyMatch(e -> npc.getId() != e.getNpc().getId())) {
            predictor.reset(); // can't determine how much each npc was damaged, reset instead
        }

        final int clumpHp = nearby.stream().mapToInt(Enemy::getCurrentHealth).sum();
        if (damage >= clumpHp) {
            sendClumpDamage(nearby);
        }
    }

    private void sendClumpDamage(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            final int npcIndex = enemy.getNpc().getIndex();
            final EntityDamaged ev = new EntityDamaged(npcIndex, enemy.getCurrentHealth());
            if (party.isInParty()) {
                clientThread.invokeLater(() -> party.send(ev));
            }
            onEntityDamaged(ev);
        }
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
     *
     * @param skill Skill XP was received in.
     * @param xp    Updated XP amount.
     */
    private void preProcessXpDrop(Skill skill, int xp) {
        if (!shouldProcess()) {
            predictor.reset();
            return;
        }

        int diff = xp - previousXps.getOrDefault(skill, 0);
        previousXps.put(skill, xp);
        processXpDrop(skill, diff);
    }

    /**
     * Queues damage on an entity. If the entity is a shadow, it hides the shadow.
     * If the entity is Akkha, it will highlight her.
     *
     * @param entityDamaged event
     */
    @Subscribe
    public void onEntityDamaged(EntityDamaged entityDamaged) {
        if (!shouldProcess()) {
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
        if (enemy != null) {
            boolean died = enemy.queueDamage(entityDamaged.getDamage());
            if (died && enemy.isHideOnDeath()) {
                // npc.setDead is a RuneLite NPC API that requires the client thread.
                // onEntityDamaged can come from the party/WebSocket thread, so post
                // the side effect onto the client thread.
                NPC liveNpc = enemy.getNpc();
                if (liveNpc != null) {
                    clientThread.invoke(() -> liveNpc.setDead(true));
                }
            }
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
     *
     * @param hit hitsplat
     */
    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hit) {
        Hitsplat hitsplat = hit.getHitsplat();
        if (!shouldProcess() || hitsplat.getAmount() <= 0) {
            return;
        }
        Actor actor = hit.getActor();
        if (actor instanceof NPC) {
            NPC npc = (NPC) actor;
            Enemy enemy = activeEnemies.getOrDefault(npc.getIndex(), null);
            if (enemy == null) {
                Trace.damage("hitsplat on untracked NPC {} (id={} idx={}) tick={} amount={} type={} mine={} others={}",
                        npc.getName(), npc.getId(), npc.getIndex(),
                        client.getTickCount(), hitsplat.getAmount(), hitsplat.getHitsplatType(),
                        hitsplat.isMine(), hitsplat.isOthers());
                return;
            }

            int amount = hitsplat.getAmount();
            int type = hitsplat.getHitsplatType();
            int tick = client.getTickCount();
            int disappearsOn = hitsplat.getDisappearsOnGameCycle();
            boolean mine = hitsplat.isMine();
            boolean others = hitsplat.isOthers();
            if (type == HitsplatID.HEAL || type == HitsplatID.CYAN_UP) {
                enemy.heal(amount);
                Trace.damage("HEAL {} on {}(idx={} id={}) tick={} type={} mine={} others={} fade={} (hp now {})",
                        amount, npc.getName(), npc.getIndex(), npc.getId(),
                        tick, type, mine, others, disappearsOn,
                        enemy.getCurrentHealth());
            } else {
                enemy.hit(amount);
                Trace.damage("HIT  {} on {}(idx={} id={}) tick={} type={} mine={} others={} fade={} (hp now {}, queued now {})",
                        amount, npc.getName(), npc.getIndex(), npc.getId(),
                        tick, type, mine, others, disappearsOn,
                        enemy.getCurrentHealth(), enemy.getQueuedDamage());
            }
        }
    }
}
