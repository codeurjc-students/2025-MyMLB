package com.mlb.mlbportal.models.enums;

public enum PlayerPositions {
    C("C"),
    FIRST_BASE("1B"),
    SECOND_BASE("2B"),
    THIRD_BASE("3B"),
    SS("SS"),
    RF("RF"),
    LF("LF"),
    CF("CF");

    private final String label;

    PlayerPositions(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}