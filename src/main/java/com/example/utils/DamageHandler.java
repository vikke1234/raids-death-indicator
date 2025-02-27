package com.example.utils;

import com.example.enemydata.Enemy;
import com.example.events.EntityDamaged;
import com.example.raids.Cox;
import com.example.raids.Toa;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * Damage handler manages the damaging of enemies, the raid classes places and removes enemies from here.
 */
@Singleton
@Slf4j
public class DamageHandler {
    @Getter
    private final Map<Integer, Enemy> activeEnemies = new HashMap<>();

    @Getter
    private final Predictor predictor = new Predictor();

    @Inject
    private Client client;

    @Inject
    ClientThread clientThread;

    @Inject
    DamageHandler damageHandler;

    @Inject
    private PartyService party;
    /**
     * Map of current XP amounts in each skill
     */
    private Map<Skill, Integer> previousXps;

    private final Set<Skill> validSkills = Set.of(Skill.HITPOINTS);

    private static final Set<Integer> POWERED_STAVES = new HashSet<>(Arrays.asList(
            ItemID.SANGUINESTI_STAFF,
            ItemID.TRIDENT_OF_THE_SEAS_FULL,
            ItemID.TRIDENT_OF_THE_SEAS,
            ItemID.TRIDENT_OF_THE_SWAMP,
            ItemID.TRIDENT_OF_THE_SWAMP_E,
            ItemID.HOLY_SANGUINESTI_STAFF,
            ItemID.TUMEKENS_SHADOW,
            ItemID.CORRUPTED_TUMEKENS_SHADOW,
            ItemID.VOIDWAKER
    ));
    private static final Set<Integer> CHINCHOMPAS = Set.of(
            ItemID.CHINCHOMPA_10033,
            ItemID.BLACK_CHINCHOMPA,
            ItemID.RED_CHINCHOMPA);


    public void initXpMap() {
        previousXps = new HashMap<>();
        Arrays.stream(Skill.values()).forEach(skill -> previousXps.put(skill, client.getSkillExperience(skill)));
    }

    public boolean shouldProcess() {
        return Toa.isAtToa(client) || Cox.isInCox(client);
    }

    @Subscribe
    protected void onGameStateChanged(GameStateChanged state) {
        if (Objects.requireNonNull(state.getGameState()) == GameState.LOGGED_IN) {
            initXpMap();
        }
    }

    /**
     * Processes the xp drop and turns it into how much damage it did. Not entirely accurate due to fractional XP.
     * Rounds down so that in case it does get fractional it should not give false positives.
     *
     * @param skill Skill that xp was received in.
     * @param xp Amount of XP that was received.
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
            //System.out.println("Unknown enemy \"" + npc.getName() + "\": " + npc.getId() + " (idx: " + npc.getIndex() + ")");
            return;
        }

        int bossHealth = client.getVarbitValue(Varbits.BOSS_HEALTH_CURRENT);
        if (bossHealth > 0 && Enemy.bosses.contains(npc.getId())) {
            enemy.current_health = bossHealth; // re-synchronize the health
        }

        int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);
        int weapon = playerComposition.getEquipmentId(KitType.WEAPON);

        boolean isDefensiveCast = attackStyle == 3;
        boolean isPoweredStaff = POWERED_STAVES.contains(weapon);
        boolean isChinchompa = CHINCHOMPAS.contains(weapon);
        double scaling = enemy.getModifier();
        Predictor.Properties props = new Predictor.Properties(skill, isDefensiveCast, isPoweredStaff, npc, scaling);
        if ((skill == Skill.RANGED || skill == Skill.MAGIC) && isDefensiveCast) {
            // Ignore in order to not double hit, insert the drop into
            // the tree in order to track the fraction
            predictor.insertInto(xp, scaling, props);
            return;
        }
        int damage = predictor.treePredict(xp, props);
        assert (damage >= 0);
        if (isChinchompa) {
            // TODO: barrage? prio: low, tldr: check for barrage animation
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

        final int clumpHp = nearby.stream().mapToInt(Enemy::getCurrent_health).sum();
        if (damage >= clumpHp) {
            sendClumpDamage(nearby);
        }
    }

    private void sendClumpDamage(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            final int npcIndex = enemy.getNpc().getIndex();
            final EntityDamaged ev = new EntityDamaged(npcIndex, enemy.getCurrent_health());
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
     * @param skill Skill XP was received in.
     * @param xp Updated XP amount.
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
            enemy.queueDamage(entityDamaged.getDamage());
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
        Hitsplat hitsplat = hit.getHitsplat();
        if (!shouldProcess() || hitsplat.getHitsplatType() == HitsplatID.HEAL || hitsplat.getAmount() <= 0) {
            return;
        }
        Actor actor = hit.getActor();
        if (actor instanceof NPC) {
            NPC npc = (NPC) actor;
            Enemy enemy = activeEnemies.getOrDefault(npc.getIndex(), null);
            if (enemy == null) {
                //log.info("Unknown target: " + npc.getId() + " index: " + npc.getIndex());
                return;
            }

            int amount = hitsplat.getAmount();
            int hp = enemy.hit(amount);
            //log.info("Damage: " + amount + " " + hit.getActor().getName() + " (" + hp +")");
        }
    }
}
