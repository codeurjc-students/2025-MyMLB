/**
 * DTO that map the direct response from the Stat API when obtaining the roster of a certain team
 */
package com.mlb.mlbportal.dto.mlbapi.player;

import java.util.List;

public record RosterResponse(List<RosterEntry> roster) {}