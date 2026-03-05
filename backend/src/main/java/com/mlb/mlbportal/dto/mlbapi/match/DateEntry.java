/**
 * DTO that maps the matches of a certain date
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DateEntry(List<GameEntry> games) {}