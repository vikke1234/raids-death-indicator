package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OverlayFormatTests {

    @Test
    public void plainTokenSubstitution() {
        assertEquals("550 (50)", AkkhaPredictorOverlay.formatHp("%ch (%qd)", 550, 50));
        assertEquals("500", AkkhaPredictorOverlay.formatHp("%qh", 550, 50));
    }

    @Test
    public void bracketBlockKeptWhenTokensNonZero() {
        assertEquals("550 (50)", AkkhaPredictorOverlay.formatHp("%ch%[ (%qd)]", 550, 50));
    }

    @Test
    public void bracketBlockStrippedWhenTokenZero() {
        // %qd = 0 → the bracketed " (%qd)" disappears entirely.
        assertEquals("550", AkkhaPredictorOverlay.formatHp("%ch%[ (%qd)]", 550, 0));
    }

    @Test
    public void multipleBracketBlocks() {
        // The queue block strips when qd=0, but the est block stays because
        // qh = ch - 0 = 550 is non-zero.
        assertEquals("hp: 550, est: 550",
                AkkhaPredictorOverlay.formatHp("hp: %ch%[, queue: %qd]%[, est: %qh]", 550, 0));
        assertEquals("hp: 550, queue: 30, est: 520",
                AkkhaPredictorOverlay.formatHp("hp: %ch%[, queue: %qd]%[, est: %qh]", 550, 30));
    }

    @Test
    public void unmatchedBracketPassesThrough() {
        // No closing ']': the rest of the string is appended verbatim, tokens
        // still get substituted at the end.
        assertEquals("550%[ no close",
                AkkhaPredictorOverlay.formatHp("%ch%[ no close", 550, 50));
    }
}
