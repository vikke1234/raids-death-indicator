package com.example.utils;

import com.example.generated.GeneratedEnemyConfig;
import com.example.generated.TrackedEnemy;
import com.example.enemydata.TrackingMode;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Adapter between the generated per-enemy config menu and the rest of the
 * plugin. OFF still tracks the enemy; it only disables highlight and hide.
 * Enemies without an annotation keep the legacy behaviour.
 */
@Singleton
public class EnemyTrackingConfig {
    @Inject
    private GeneratedEnemyConfig config;

    public boolean shouldHighlight(int npcId) {
        return mode(npcId).highlights();
    }

    public boolean shouldHide(int npcId) {
        return mode(npcId).hides();
    }

    public boolean isBoss(int npcId) {
        return TrackedEnemy.forNpcId(npcId)
                .map(TrackedEnemy::isBoss)
                .orElse(false);
    }

    private TrackingMode mode(int npcId) {
        return TrackedEnemy.forNpcId(npcId)
                .map(enemy -> enemy.mode(config))
                .orElse(TrackingMode.HIGHLIGHT_AND_HIDE);
    }
}
