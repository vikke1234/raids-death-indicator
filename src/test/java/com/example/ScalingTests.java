package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScalingTests {

    @Test
    public void testSoloScaling() {
        Akkha akkha = new Akkha(35, 1, 0);
        assertEquals(460, akkha.getScaledHealth());
    }

    @Test
    public void testDuoScaling() {
        Akkha akkha = new Akkha(85, 2, 3);
        assertEquals(1200, akkha.getScaledHealth());
    }

}
