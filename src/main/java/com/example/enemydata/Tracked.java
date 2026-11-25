package com.example.enemydata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an enemy class as configurable. The build-time processor reads these
 * declarations and generates the config menu and the NPC-id registry.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Tracked {
    /** Display name in settings. Defaults to a spaced version of the class name. */
    String name() default "";

    /** All NPC ids that map to this logical enemy, including phase variants. */
    int[] npcIds();

    Raid raid() default Raid.MISC;

    TrackingMode defaultMode() default TrackingMode.HIGHLIGHT_AND_HIDE;

    /** Whether this NPC uses the client's boss health-bar varbit. */
    boolean boss() default false;

    /** Config key suffix. Defaults to a snake_case version of the class name. */
    String key() default "";
}
