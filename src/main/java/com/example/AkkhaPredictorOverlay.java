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
                    String full = formatHp(config.hpOverlayFormat(), hp, queued);
                    // The "anchor" is the format with all %[...] blocks
                    // unconditionally stripped. We position the text based on
                    // this stable string's width so the always-present prefix
                    // (e.g. "%ch") never moves when a conditional block
                    // appears/disappears — extra content just extends to the
                    // right.
                    String anchorText = formatHpStripped(config.hpOverlayFormat(), hp, queued);
                    renderText(graphics, npc, full, anchorText, config.textColor());
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

    /**
     * Substitute %ch / %qh / %qd into the user's overlay format string.
     * %ch = current HP, %qh = current minus queued damage, %qd = queued damage.
     *
     * <p>Supports a bracketed conditional: {@code %[...]} blocks are stripped
     * if any numeric token inside them would resolve to 0. So a format of
     * {@code "%ch%[ (%qd)]"} renders as {@code "550 (50)"} when there's queued
     * damage and just {@code "550"} when there isn't.</p>
     */
    static String formatHp(String format, int currentHp, int queuedDamage) {
        int qh = currentHp - queuedDamage;
        StringBuilder out = new StringBuilder(format.length());
        int i = 0;
        while (i < format.length()) {
            if (i + 1 < format.length() && format.charAt(i) == '%' && format.charAt(i + 1) == '[') {
                int close = format.indexOf(']', i + 2);
                if (close < 0) {
                    out.append(format, i, format.length());
                    break;
                }
                String block = format.substring(i + 2, close);
                if (!blockHasZeroToken(block, currentHp, qh, queuedDamage)) {
                    out.append(substituteTokens(block, currentHp, qh, queuedDamage));
                }
                i = close + 1;
            } else {
                out.append(format.charAt(i));
                i++;
            }
        }
        return substituteTokens(out.toString(), currentHp, qh, queuedDamage);
    }

    /**
     * Same token substitution as {@link #formatHp} but always strips every
     * {@code %[...]} block. Used as the positioning anchor: text is rendered
     * starting at the X that would centre this stripped string, so the parts
     * of the format that are *always* present stay pinned in place when a
     * conditional block toggles on or off.
     */
    static String formatHpStripped(String format, int currentHp, int queuedDamage) {
        int qh = currentHp - queuedDamage;
        StringBuilder out = new StringBuilder(format.length());
        int i = 0;
        while (i < format.length()) {
            if (i + 1 < format.length() && format.charAt(i) == '%' && format.charAt(i + 1) == '[') {
                int close = format.indexOf(']', i + 2);
                if (close < 0) {
                    out.append(format, i, format.length());
                    break;
                }
                i = close + 1; // skip block entirely
            } else {
                out.append(format.charAt(i));
                i++;
            }
        }
        return substituteTokens(out.toString(), currentHp, qh, queuedDamage);
    }

    private static boolean blockHasZeroToken(String block, int ch, int qh, int qd) {
        if (block.contains("%ch") && ch == 0) return true;
        if (block.contains("%qh") && qh == 0) return true;
        if (block.contains("%qd") && qd == 0) return true;
        return false;
    }

    private static String substituteTokens(String s, int ch, int qh, int qd) {
        return s
                .replace("%ch", Integer.toString(ch))
                .replace("%qh", Integer.toString(qh))
                .replace("%qd", Integer.toString(qd));
    }

    private void renderText(Graphics2D graphics, NPC npc, String drawText, String anchorText, Color c) {
        if (npc == null) {
            return;
        }
        graphics.setFont(FontManager.getRunescapeBoldFont().deriveFont((float) config.hpOverlayFontSize()));
        // Tile-based anchor — projected from the NPC's LocalPoint + logical
        // height. Stays still through animation, and the anchorText (= the
        // user's format with all %[...] blocks stripped) keeps the always-on
        // prefix pinned in place: conditional content just extends to the
        // right when it toggles on.
        Point anchor = npc.getCanvasTextLocation(graphics, anchorText, npc.getLogicalHeight());
        if (anchor == null) {
            return;
        }
        int y = anchor.getY() - config.hpOverlayHeightOffset();
        OverlayUtil.renderTextLocation(graphics, new Point(anchor.getX(), y), drawText, c);
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
