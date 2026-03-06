/**
 * DTO that wraps split stats of a team from the API (such as Last 10 Games).
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecordsWrapper(List<SplitRecords> splitRecords) {}