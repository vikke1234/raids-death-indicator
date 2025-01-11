package com.example;

import com.example.enemydata.Enemy;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class AkkhaPredictorOverlay extends Overlay {
    private final AkkhaPredictor plugin;
    private final AkkhaPredictorConfig config;
    private final Client client;

    @Inject
    public AkkhaPredictorOverlay(AkkhaPredictor plugin, AkkhaPredictorConfig config, Client client, ModelOutlineRenderer renderer) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isAtToa()) {
            return null;
        }
        var enemies = plugin.getActiveEnemies();

        for (Enemy enemy : enemies.values()) {
            if (enemy.shouldHighlight()) {
                renderPoly(graphics, null, 0, config.highlightColor(), enemy.getNpc().getConvexHull());
            }
        }

        Skill []skills = new Skill[]{Skill.HITPOINTS};
        int start = 20;
        for (Skill skill : skills) {
            if (plugin.getPredictor().isAccurate(skill)) {
                graphics.setColor(Color.green);
                graphics.fillRect(10, start, 10, 10);
            } else {
                graphics.setColor(Color.red);
                graphics.fillRect(10, start, 10, 10);
            }
            start += 20;
        }
        return null;
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
