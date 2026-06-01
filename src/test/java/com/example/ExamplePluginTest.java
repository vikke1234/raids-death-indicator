package com.example;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    @SuppressWarnings("unchecked") // ExternalPluginManager.loadBuiltin takes Class<? extends Plugin>... varargs
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(AkkhaPredictor.class);
        RuneLite.main(args);
    }
}
