/**
 * DTO that map all the stats of a certain player for the current season.
 */

package com.mlb.mlbportal.dto.mlbapi.player;

import java.util.List;

public record Stats(List<Split> splits) {}