package com.mlb.mlbportal.handler.notFound;

public class SectorNotFoundException extends ResourceNotFoundException {
    public SectorNotFoundException(Long id) {
        super("Sector " + id + " Not Found");
    }
}