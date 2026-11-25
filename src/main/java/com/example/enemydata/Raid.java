package com.example.enemydata;

public enum Raid {
    COX("Chambers of Xeric"),
    TOA("Tombs of Amascut"),
    TFA("The Fractured Archive"),
    MISC("Miscellaneous");

    private final String displayName;

    Raid(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
