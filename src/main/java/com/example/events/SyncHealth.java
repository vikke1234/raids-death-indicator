package com.example.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class SyncHealth extends PartyMemberMessage {
    int npcIndex;
    int health;
}
