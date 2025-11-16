package com.mlb.mlbportal.dto.mlbapi;

import java.util.List;

public record ScheduleResponse(List<DateEntry> dates) {}