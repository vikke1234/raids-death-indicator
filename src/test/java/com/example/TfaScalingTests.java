package com.example;

import com.example.enemydata.tfa.ArchiveGuardian;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TfaScalingTests {
    @Test
    public void reloadsPartySizeWhenBossActivates() {
        Map<Integer, Integer> varbits = new HashMap<>();
        Client client = proxy(Client.class, (proxy, method, args) -> {
            if (method.getName().equals("getVarbitValue")) {
                return varbits.getOrDefault((int) args[0], 0);
            }
            return method.getReturnType() == int.class ? 0 : null;
        });

        TestNPC npc = new TestNPC(16657);
        ArchiveGuardian guardian = new ArchiveGuardian(npc);
        guardian.syncFromClient(client);
        assertEquals(550, guardian.getShieldHealth());

        // Two other alive players join: 3-player scaling should apply on activation.
        varbits.put(VarbitID.TFA_CLIENT_P0 + 1, 20);
        varbits.put(VarbitID.TFA_CLIENT_P0 + 2, 15);
        guardian.onNpcChanged(16657, 16659, client);

        assertEquals(800, guardian.getShieldHealth());
        assertEquals(1600, guardian.getTrueHealth());

        guardian.onInertGuardianDestroyed();
        assertEquals(320, guardian.getQueuedDamage());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                TfaScalingTests.class.getClassLoader(),
                new Class<?>[] { type },
                handler);
    }
}
