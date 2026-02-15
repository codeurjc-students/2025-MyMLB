package com.mlb.mlbportal.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PitcherPositions {
    SP("SP"),
    RP("RP"),
    CL("CL"),
    P("P");

    private final String label;

    PitcherPositions(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static PitcherPositions fromLabel(String label) {
        for (PitcherPositions pos : values()) {
            if (pos.name().equalsIgnoreCase(label) || pos.getLabel().equalsIgnoreCase(label)) {
                return pos;
            }
        }
        throw new IllegalArgumentException("Invalid player position: " + label);
    }
}