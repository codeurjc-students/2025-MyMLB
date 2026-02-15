import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventResponse } from '../../models/ticket/event-response.model';
import { PaginatedResponse } from '../../models/pagination.model';
import { Seat } from '../../models/ticket/seat.model';
import { EventManager } from '../../models/ticket/event-manager.model';
import { EventCreateRequest } from '../../models/ticket/event-create-request-model';
import { EventEditRequest } from '../../models/ticket/event-edit-request.model';
import { environment } from '../../../environments/environment';

@Injectable({
	providedIn: 'root',
})
export class EventService {
	private httpMock = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/api/v1/events`;

	public getEventById(eventId: number): Observable<EventResponse> {
		return this.httpMock.get<EventResponse>(`${this.apiUrl}/${eventId}`);
	}

	public getEventByMatchId(matchId: number): Observable<EventResponse> {
		return this.httpMock.get<EventResponse>(`${this.apiUrl}/match/${matchId}`);
	}

	public getAvailableSectors(eventId: number): Observable<PaginatedResponse<EventManager>> {
		return this.httpMock.get<PaginatedResponse<EventManager>>(`${this.apiUrl}/${eventId}/sectors`);
	}

	public getAvailableSeats(eventId: number, sectorId: number): Observable<PaginatedResponse<Seat>> {
		return this.httpMock.get<PaginatedResponse<Seat>>(`${this.apiUrl}/${eventId}/sector/${sectorId}`);
	}

	public createEvent(request: EventCreateRequest): Observable<EventResponse> {
		return this.httpMock.post<EventResponse>(this.apiUrl, request);
	}

	public editEvent(request: EventEditRequest): Observable<EventResponse> {
		return this.httpMock.put<EventResponse>(this.apiUrl, request);
	}

	public deleteEvent(eventId: number): Observable<EventResponse> {
		return this.httpMock.delete<EventResponse>(`${this.apiUrl}/${eventId}`);
	}
}