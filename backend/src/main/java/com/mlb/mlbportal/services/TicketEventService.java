package com.mlb.mlbportal.services;

import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketEventService {
    private final EventRepository eventRepository;
    private final StadiumRepository stadiumRepository;


}