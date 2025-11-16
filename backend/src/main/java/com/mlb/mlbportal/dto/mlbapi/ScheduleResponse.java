package com.mlb.mlbportal.dto.mlbApi;

import java.util.List;

public record ScheduleResponse(List<DateEntry> dates) {}