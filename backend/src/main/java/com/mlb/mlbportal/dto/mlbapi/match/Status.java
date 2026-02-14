/**
 * DTO that maps the status of a game (SCHEDULED, IN PROGRESS, FINSISHED)
 */

package com.mlb.mlbportal.dto.mlbapi.match;

public record Status(String detailedState) {}