package com.example;

import com.example.enemydata.Enemy;
import com.example.raids.Toa;
import com.example.utils.DamageHandler;
import com.example.utils.Predictor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
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
            if (enemy.shouldHighlight()) {
                NPC npc = enemy.getNpc();
                renderPoly(graphics, null, 0, config.highlightColor(), npc.getConvexHull());
            }

            if (config.enableHpOverlay()) {
                String str = enemy.getCurrent_health() + " (" + enemy.getQueuedDamage() + ")";
                renderText(graphics, enemy.getNpc(), str, config.textColor());
            }
        }

        if (Toa.isAtToa(client) && !config.status()) {
            return null;
        }

        Skill []skills = new Skill[]{Skill.HITPOINTS};
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
        Point p = npc.getCanvasTextLocation(graphics, str, npc.getLogicalHeight());
        if (p == null) {
            return;
        }

        p = new Point(p.getX(), p.getY() + 20);
        graphics.setFont(FontManager.getDefaultBoldFont());
        OverlayUtil.renderTextLocation(graphics, p, str, c);
    }

    private void renderPoly(Graphics2D graphics, Color borderColor, float borderWidth, Color fillColor, Shape polygon)
    {
        if (polygon != null)
        {
            graphics.setColor(borderColor);
            graphics.setStroke(new BasicStroke(borderWidth));
            graphics.draw(polygon);
            graphics.setColor(fillColor);
            graphics.fill(polygon);
        }
    }
}
