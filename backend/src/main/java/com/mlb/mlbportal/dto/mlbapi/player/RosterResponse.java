/**
 * DTO that map the direct response from the Stat API when obtaining the roster of a certain team
 */
package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RosterResponse(List<RosterEntry> roster) {}