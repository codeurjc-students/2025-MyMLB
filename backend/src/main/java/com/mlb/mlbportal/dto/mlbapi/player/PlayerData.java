/**
 * This DTO map the basic data of the player which are the id (stat API ID) and his fullName.
 */

package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerData(int id, String fullName) {}