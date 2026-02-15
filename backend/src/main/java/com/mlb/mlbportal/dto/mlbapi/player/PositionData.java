/**
 * DTO that map the data regarding the player's position
 * code is related to the position's number:
 * 1 --> Pitcher
 * 2 --> Catcher
 * 3 --> 1B
 * 4 --> 2B
 * 5 --> 3B
 * 6 --> SS
 * 7 --> LF
 * 8 --> CF
 * 9 --> RF
 * 10 --> DH.
 *
 * The abbreviation is the shortname of the position, for example, for first base is 1B
 */
package com.mlb.mlbportal.dto.mlbapi.player;

public record PositionData(String code, String abbreviation) {}