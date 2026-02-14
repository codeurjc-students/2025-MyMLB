/**
 * DTO that maps the matches of a certain date
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import java.util.List;

public record DateEntry(List<GameEntry> games) {}