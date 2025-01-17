package com.example;

import com.example.enemydata.Enemy;
import com.example.enemydata.ampken.*;
import com.example.enemydata.het.Akkha;
import com.example.enemydata.scabaras.Kephri;
import com.example.enemydata.scabaras.Kephri721;
import com.example.enemydata.scabaras.Spitter;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

class TestNPC implements NPC {
    int id;
    public TestNPC(int id) {
        this.id = id;
    }
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public WorldView getWorldView() {
        return null;
    }
    @Override
    public boolean isInteracting() {
        return false;
    }

    @Override
    public Actor getInteracting() {
        return null;
    }

    @Override
    public int getHealthRatio() {
        return 0;
    }

    @Override
    public int getHealthScale() {
        return 0;
    }

    @Override
    public WorldPoint getWorldLocation() {
        return null;
    }

    @Override
    public LocalPoint getLocalLocation() {
        return null;
    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public int getCurrentOrientation() {
        return 0;
    }

    @Override
    public int getAnimation() {
        return 0;
    }

    @Override
    public int getPoseAnimation() {
        return 0;
    }

    @Override
    public void setPoseAnimation(int animation) {

    }

    @Override
    public int getPoseAnimationFrame() {
        return 0;
    }

    @Override
    public void setPoseAnimationFrame(int frame) {

    }

    @Override
    public int getIdlePoseAnimation() {
        return 0;
    }

    @Override
    public void setIdlePoseAnimation(int animation) {

    }

    @Override
    public int getIdleRotateLeft() {
        return 0;
    }

    @Override
    public void setIdleRotateLeft(int animationID) {

    }

    @Override
    public int getIdleRotateRight() {
        return 0;
    }

    @Override
    public void setIdleRotateRight(int animationID) {

    }

    @Override
    public int getWalkAnimation() {
        return 0;
    }

    @Override
    public void setWalkAnimation(int animationID) {

    }

    @Override
    public int getWalkRotateLeft() {
        return 0;
    }

    @Override
    public void setWalkRotateLeft(int animationID) {

    }

    @Override
    public int getWalkRotateRight() {
        return 0;
    }

    @Override
    public void setWalkRotateRight(int animationID) {

    }

    @Override
    public int getWalkRotate180() {
        return 0;
    }

    @Override
    public void setWalkRotate180(int animationID) {

    }

    @Override
    public int getRunAnimation() {
        return 0;
    }

    @Override
    public void setRunAnimation(int animationID) {

    }

    @Override
    public void setAnimation(int animation) {

    }

    @Override
    public int getAnimationFrame() {
        return 0;
    }

    @Override
    public void setActionFrame(int frame) {

    }

    @Override
    public void setAnimationFrame(int frame) {

    }

    @Override
    public IterableHashTable<ActorSpotAnim> getSpotAnims() {
        return null;
    }

    @Override
    public boolean hasSpotAnim(int spotAnimId) {
        return false;
    }

    @Override
    public void createSpotAnim(int id, int spotAnimId, int height, int delay) {

    }

    @Override
    public void removeSpotAnim(int id) {

    }

    @Override
    public void clearSpotAnims() {

    }

    @Override
    public int getGraphic() {
        return 0;
    }

    @Override
    public void setGraphic(int graphic) {

    }

    @Override
    public int getGraphicHeight() {
        return 0;
    }

    @Override
    public void setGraphicHeight(int height) {

    }

    @Override
    public int getSpotAnimFrame() {
        return 0;
    }

    @Override
    public void setSpotAnimFrame(int spotAnimFrame) {

    }

    @Override
    public Polygon getCanvasTilePoly() {
        return null;
    }

    @Nullable
    @Override
    public net.runelite.api.Point getCanvasTextLocation(Graphics2D graphics, String text, int zOffset) {
        return null;
    }

    @Override
    public net.runelite.api.Point getCanvasImageLocation(BufferedImage image, int zOffset) {
        return null;
    }

    @Override
    public net.runelite.api.Point getCanvasSpriteLocation(SpritePixels sprite, int zOffset) {
        return null;
    }

    @Override
    public Point getMinimapLocation() {
        return null;
    }

    @Override
    public int getLogicalHeight() {
        return 0;
    }

    @Override
    public Shape getConvexHull() {
        return null;
    }


    @Override
    public WorldArea getWorldArea() {
        return null;
    }

    @Override
    public String getOverheadText() {
        return null;
    }

    @Override
    public void setOverheadText(String overheadText) {

    }

    @Override
    public int getOverheadCycle() {
        return 0;
    }

    @Override
    public void setOverheadCycle(int cycles) {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public void setDead(boolean dead) {

    }

    @Override
    public int getCombatLevel() {
        return 0;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public NPCComposition getComposition() {
        return null;
    }

    @Nullable
    @Override
    public NPCComposition getTransformedComposition() {
        return null;
    }

    @Nullable
    @Override
    public NpcOverrides getModelOverrides() {
        return null;
    }

    @Nullable
    @Override
    public NpcOverrides getChatheadOverrides() {
        return null;
    }

    @Override
    public Model getModel() {
        return null;
    }

    @Override
    public int getModelHeight() {
        return 0;
    }

    @Override
    public void setModelHeight(int modelHeight) {

    }


    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public Node getPrevious() {
        return null;
    }

    @Override
    public long getHash() {
        return 0;
    }
}
public class ScalingTests {
    private static final float delta = 0.00000001F;
    @Test
    public void testThrowerScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_THROWER);
        Thrower thrower = new Thrower(npc, 515, 1, 2);
        assertEquals(1.0, thrower.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_THROWER_11713);
        thrower = new Thrower(npc, 515, 1, 2);
        assertEquals(1.0, thrower.getModifier(), delta);
    }

    @Test
    public void testMageScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_MAGE);
        Mage mage = new Mage(npc, 515, 1, 2);
        assertEquals(12, mage.getScaledHealth());
        assertEquals(1.1, mage.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_MAGE_11714);
        mage = new Mage(npc, 515, 1, 2);
        assertEquals(1.175, mage.getModifier(), delta);
    }

    @Test
    public void testBrawlerScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_BRAWLER);
        Brawler brawler = new Brawler(npc, 515, 1, 2);
        assertEquals(1.1, brawler.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_BRAWLER_11712);
        brawler = new Brawler(npc, 515, 1, 2);
        assertEquals(1.175, brawler.getModifier(), delta);
    }

    @Test
    public void testMiscAmpkenScaling() {
        TestNPC shamanNpc = new TestNPC(NpcID.BABOON_SHAMAN);
        Shaman shaman = new Shaman(shamanNpc, 515, 1, 2);
        assertEquals(1.2, shaman.getModifier(), delta);

        TestNPC cursedNPC = new TestNPC(NpcID.CURSED_BABOON);
        Cursed cursed = new Cursed(cursedNPC, 515, 1, 2);
        assertEquals(1.175, cursed.getModifier(), delta);

        TestNPC thrallNPC = new TestNPC(NpcID.BABOON_THRALL);
        Thrall thrall = new Thrall(thrallNPC, 515, 1, 2);
        assertEquals(1.0, thrall.getModifier(), delta);

        TestNPC volatileNPC = new TestNPC(NpcID.VOLATILE_BABOON);
        Volatile vola = new Volatile(volatileNPC, 515, 1, 2);
        assertEquals(1.175, vola.getModifier(), delta);
    }

    @Test
    public void spitterScalingTest() {
        TestNPC spitterNpc = new TestNPC(NpcID.SPITTING_SCARAB);
        Spitter spitter = new Spitter(spitterNpc, 305, 1, 0);
        assertEquals(1.025, spitter.getModifier(), delta);

        TestNPC kephriNpc = new TestNPC(NpcID.KEPHRI);
        Kephri kephri = new Kephri(kephriNpc, 305, 1, 0);
        assertEquals(1.075, kephri.getModifier(), delta);
        assertEquals(330, kephri.getScaledHealth());

        TestNPC kephri721Npc = new TestNPC(NpcID.KEPHRI);
        Kephri721 kephri721 = new Kephri721(kephri721Npc, 305, 1, 0);
        assertEquals(180, kephri721.getScaledHealth());
        assertEquals(1.025, kephri721.getModifier(), delta);
    }

    @Test
    public void akkhaScalingTest() {
        TestNPC akkhaNpc = new TestNPC(NpcID.AKKHA_11790);
        Enemy akkha = new Akkha(akkhaNpc, 305, 2, 0);
        assertEquals(1.575, akkha.getModifier(), delta);
        akkha.fixupStats(305, 2, 0);
        assertEquals(1.575, akkha.getModifier(), delta);
    }
}
