import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventResponse } from '../../models/ticket/event-response.model';
import { PaginatedResponse } from '../../models/pagination.model';
import { Seat } from '../../models/ticket/seat.model';
import { EventManager } from '../../models/ticket/event-manager.model';

@Injectable({
	providedIn: 'root',
})
export class EventService {
	private httpMock = inject(HttpClient);
	private apiUrl = 'https://localhost:8443/api/v1/events';

	public getEventByMatchId(matchId: number): Observable<EventResponse> {
		return this.httpMock.get<EventResponse>(`${this.apiUrl}/match/${matchId}`);
	}

	public getAvailableSectors(eventId: number): Observable<PaginatedResponse<EventManager>> {
		return this.httpMock.get<PaginatedResponse<EventManager>>(`${this.apiUrl}/${eventId}/sectors`);
	}

	public getAvailableSeats(eventId: number, sectorId: number): Observable<PaginatedResponse<Seat>> {
		return this.httpMock.get<PaginatedResponse<Seat>>(`${this.apiUrl}/${eventId}/sector/${sectorId}`);
	}
}