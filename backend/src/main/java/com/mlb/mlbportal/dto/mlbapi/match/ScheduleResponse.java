/**
 * DTO that maps the direct response from the API.
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import java.util.List;

public record ScheduleResponse(List<DateEntry> dates) {}