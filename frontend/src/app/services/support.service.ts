import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { CreateTicketRequest, ReplyRequest, SupportMessage, SupportTicket } from '../models/support.model';
import { AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class SupportService {
    private http = inject(HttpClient);
    private userApi = `${environment.apiUrl}/support`;
    private adminApi = `${environment.apiUrl}/admin/support/tickets`;

	private openTicketsSubject = new BehaviorSubject<number>(0);
	public opentTickets$: Observable<number> = this.openTicketsSubject.asObservable();

	public updateCurrentOpenTickets(): void {
		this.getOpenTickets().subscribe({
			next: (openTickets) => this.openTicketsSubject.next(openTickets ? openTickets.length : 0),
			error: (_) => this.openTicketsSubject.next(0)
		});
	}

    public createTicket(request: CreateTicketRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(this.userApi, request);
    }

    public getOpenTickets(): Observable<SupportTicket[]> {
        return this.http.get<SupportTicket[]>(this.adminApi);
    }

    public getConversation(ticketId: number): Observable<SupportMessage[]> {
        return this.http.get<SupportMessage[]>(`${this.adminApi}/${ticketId}/conversation`);
    }

    public reply(ticketId: number, request: ReplyRequest): Observable<SupportMessage> {
        return this.http.post<SupportMessage>(`${this.adminApi}/${ticketId}/reply`, request);
    }

    public closeTicket(ticketId: number): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.adminApi}/${ticketId}/close`, {});
    }
}