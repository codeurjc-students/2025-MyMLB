/**
 * DTO that maps the status of a game (SCHEDULED, IN PROGRESS, FINSISHED)
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Status(String detailedState) {}