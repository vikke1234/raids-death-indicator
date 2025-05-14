package com.example.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityDamaged extends PartyMemberMessage
{
    int npcIndex;
    int damage;
    /// Tick that the attack started on.
    int tick;
}

