package com.example.enemydata;

public enum TrackingMode {
    /** Track normally for XP and HP, but never highlight or hide the NPC. */
    OFF,
    HIGHLIGHT,
    HIDE,
    HIGHLIGHT_AND_HIDE;

    public boolean highlights() {
        return this == HIGHLIGHT || this == HIGHLIGHT_AND_HIDE;
    }

    public boolean hides() {
        return this == HIDE || this == HIGHLIGHT_AND_HIDE;
    }
}
