package com.example.utils;

import com.example.generated.GeneratedEnemyConfig;
import com.example.generated.TrackedEnemy;
import com.example.enemydata.TrackingMode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Adapter between the generated per-enemy config menu and the rest of the
 * plugin. Enemies without an annotation are left alone.
 */
@Singleton
public class EnemyTrackingConfig {
    @Inject
    private GeneratedEnemyConfig config;

    public Optional<TrackedEnemy> forNpcId(int npcId) {
        return TrackedEnemy.forNpcId(npcId);
    }

    public boolean isTracked(int npcId) {
        return mode(npcId) != TrackingMode.OFF;
    }

    public boolean shouldHighlight(int npcId) {
        return mode(npcId).highlights();
    }

    public boolean shouldHide(int npcId) {
        return mode(npcId).hides();
    }

    private TrackingMode mode(int npcId) {
        return TrackedEnemy.forNpcId(npcId)
                .map(enemy -> enemy.mode(config))
                .orElse(TrackingMode.HIGHLIGHT_AND_HIDE);
    }
}
