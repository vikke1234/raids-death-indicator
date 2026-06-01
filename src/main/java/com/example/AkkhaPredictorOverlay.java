package com.example;

import com.example.enemydata.Enemy;
import com.example.raids.Toa;
import com.example.utils.DamageHandler;
import com.example.utils.Predictor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class AkkhaPredictorOverlay extends Overlay {
    @Inject
    private AkkhaPredictorConfig config;

    @Inject
    private Client client;

    @Inject
    private DamageHandler damageHandler;

    @Inject
    public void initialize() {
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        var enemies = damageHandler.getActiveEnemies();

        for (Enemy enemy : enemies.values()) {
            NPC npc = enemy.getNpc();
            if (npc == null) {
                continue;
            }
            // Wrap per-enemy so a transitional NPC state (e.g. Akkha during her
            // memory special, where the model is briefly unavailable and the
            // RuneLite NPC APIs throw internally) doesn't take down the rest of
            // the overlay. RuneLite wraps the root NPE in its own exception
            // type, so we widen to Throwable to catch all flavours.
            try {
                if (enemy.shouldHighlight()) {
                    renderPoly(graphics, null, 0, config.highlightColor(), npc.getConvexHull());
                }
                if (config.enableHpOverlay()) {
                    int hp = enemy.getCurrentHealth();
                    int queued = enemy.getQueuedDamage();
                    String str = queued > 0
                            ? (hp - queued) + " (" + queued + ")"
                            : Integer.toString(hp);
                    renderText(graphics, npc, str, config.textColor());
                }
            } catch (Throwable ignored) {
                // NPC is in a half-spawned state; nothing meaningful to draw this frame.
            }
        }

        if (!Toa.isAtToa(client) || !config.status()) {
            return null;
        }

        Skill[] skills = new Skill[] { Skill.HITPOINTS };
        int start = 20;
        Predictor predictor = damageHandler.getPredictor();

        for (Skill skill : skills) {
            if (predictor.isAccurate(skill)) {
                graphics.setColor(Color.green);
                graphics.fillRect(10, start, 10, 10);
            } else if (predictor.isDead(skill)) {
                graphics.setColor(Color.pink);
                graphics.fillRect(10, start, 10, 10);
            } else {
                graphics.setColor(Color.red);
                graphics.fillRect(10, start, 10, 10);
            }
            start += 20;
        }
        return null;
    }

    private void renderText(Graphics2D graphics, NPC npc, String str, Color c) {
        if (npc == null) {
            return;
        }
        Shape hull = npc.getConvexHull();
        if (hull == null) {
            return;
        }
        Rectangle bounds = hull.getBounds();
        graphics.setFont(FontManager.getRunescapeBoldFont().deriveFont((float) config.hpOverlayFontSize()));
        FontMetrics fm = graphics.getFontMetrics();
        int x = bounds.x + bounds.width / 2 - fm.stringWidth(str) / 2;
        int y = bounds.y - config.hpOverlayHeightOffset();
        OverlayUtil.renderTextLocation(graphics, new Point(x, y), str, c);
    }

    private void renderPoly(Graphics2D graphics, Color borderColor, float borderWidth, Color fillColor, Shape polygon) {
        if (polygon != null) {
            graphics.setColor(borderColor);
            graphics.setStroke(new BasicStroke(borderWidth));
            graphics.draw(polygon);
            graphics.setColor(fillColor);
            graphics.fill(polygon);
        }
    }
}
