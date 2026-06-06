package com.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compile-time gates for verbose diagnostic logging.
 *
 * <p>
 * Flip a category constant to true when investigating that subsystem and
 * recompile. When the flag is false the gate is a {@code static final} check,
 * so the JIT eliminates the call site (including the varargs array under
 * escape analysis) and there is no runtime cost in production.
 * </p>
 *
 * <p>
 * All channels share a single SLF4J logger named {@code com.example.trace}
 * so they can be filtered together in logback config.
 * </p>
 */
public final class Trace {
    /** Tree branching, fraction resolution, prediction decisions. */
    public static final boolean PREDICTOR = Boolean.getBoolean("trace.predictor");

    /** XP drops, hitsplats, predicted-vs-actual damage. */
    public static final boolean DAMAGE = Boolean.getBoolean("trace.damage");

    /** Akkha phase highlight decisions. */
    public static final boolean AKKHA = Boolean.getBoolean("trace.akkha");

    private static final Logger LOG = LoggerFactory.getLogger("com.example.trace");

    public static void predictor(String format, Object... args) {
        if (PREDICTOR)
            LOG.info(format, args);
    }

    public static void damage(String format, Object... args) {
        if (DAMAGE)
            LOG.info(format, args);
    }

    public static void akkha(String format, Object... args) {
        if (AKKHA)
            LOG.info(format, args);
    }

    private Trace() {
    }
}
