package com.example.utils;

import lombok.Value;

@Value
public class Attack {
    /// Expected tick that the attack should land
    int delay;

    /// Start tick, used to synchronize timers
    /// in case a player for example has logged out.
    int tick;
}
