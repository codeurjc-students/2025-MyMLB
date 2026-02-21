import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventCreateRequest, EventEditRequest, EventManager, EventResponse, Seat } from '../../models/ticket/event.model';
import { PaginatedResponse } from '../../models/pagination.model';
import { environment } from '../../../environments/environment';

@Injectable({
	providedIn: 'root',
})
export class EventService {
	private httpMock = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/events`;

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