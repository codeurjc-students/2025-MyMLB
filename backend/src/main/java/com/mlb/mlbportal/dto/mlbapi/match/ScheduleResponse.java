/**
 * DTO that maps the direct response from the API.
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScheduleResponse(List<DateEntry> dates) {}