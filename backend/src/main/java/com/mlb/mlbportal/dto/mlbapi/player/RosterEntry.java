/**
 * DTO that map the roster data, which is given by the player and his position.
 */
package com.mlb.mlbportal.dto.mlbapi.player;

public record RosterEntry(PlayerData person, PositionData position) {}