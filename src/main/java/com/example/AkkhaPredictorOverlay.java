package com.example;

import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Akkha akkha = plugin.getAkkha();

        if (akkha == null) {
            return null;
        }
        if (akkha.isShouldDraw() && akkha.isCanPhase()) {
            Optional<NPC> client_akkha = client.getNpcs().stream().filter(npc -> Objects.equals(npc.getName(), "Akkha")).findFirst();
            client_akkha.ifPresent(npc -> renderPoly(graphics, Color.BLUE, 4, Color.BLUE, npc.getConvexHull()));
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
