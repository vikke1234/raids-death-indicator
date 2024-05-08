package com.example;

import com.example.enemydata.Akkha;
import com.example.enemydata.Enemy;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

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
        var enemies = plugin.getActiveEnemies();

        for (Enemy enemy : enemies.values()) {
            if (enemy.shouldHighlight()) {
                renderPoly(graphics, Color.BLUE, 4, Color.BLUE, enemy.getNpc().getConvexHull());
            }
        }

        Skill []skills = new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.MAGIC, Skill.RANGED};
        int start = 140;
        for (Skill skill : skills) {
            if (plugin.getPredictor().isAccurate(skill)) {
                graphics.setColor(Color.green);
                graphics.fillRect(30, start, 10, 10);
            } else {
                graphics.setColor(Color.red);
                graphics.fillRect(30, start, 10, 10);
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
