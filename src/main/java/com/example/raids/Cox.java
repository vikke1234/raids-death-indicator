package com.example.raids;

import com.example.utils.DamageHandler;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Cox {
    static class InternalVarbits {
        public static final int COX_CM = 6835;
        public static final int GROUP_SIZE = 9540;
    }

    @Inject
    private DamageHandler damageHandler;

    @Inject
    private Client client;

    boolean isCm;

    int groupSize;

    @Subscribe
    public void onVarbitChanged(VarbitChanged ev) {
        if (ev.getVarbitId() == Varbits.RAID_STATE && ev.getValue() == 1) {
            isCm = client.getVarbitValue(InternalVarbits.COX_CM) == 1;
            groupSize = client.getVarbitValue(InternalVarbits.GROUP_SIZE);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned ev) {

    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned ev) {

    }
}
