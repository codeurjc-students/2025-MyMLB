package com.mlb.mlbportal.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlayerPositions {
    C("C"),
    FIRST_BASE("1B"),
    SECOND_BASE("2B"),
    THIRD_BASE("3B"),
    DH("DH"),
    SS("SS"),
    RF("RF"),
    LF("LF"),
    CF("CF");

    private final String label;

    PlayerPositions(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static PlayerPositions fromLabel(String label) {
        for (PlayerPositions pos : values()) {
            if (pos.name().equalsIgnoreCase(label) || pos.getLabel().equalsIgnoreCase(label)) {
                return pos;
            }
        }
        throw new IllegalArgumentException("Invalid player position: " + label);
    }
}