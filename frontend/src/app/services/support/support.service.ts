import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { CreateTicketRequest } from '../../models/support/create-ticket-request.model';
import { ReplyRequest } from '../../models/support/reply-request.model';
import { SupportTicket } from '../../models/support/support-ticket.model';
import { SupportMessage } from '../../models/support/support-message.model';
import { AuthResponse } from '../../models/auth/auth-response.model';

@Injectable({
    providedIn: 'root',
})
export class SupportService {
    private http = inject(HttpClient);
    private userApi = 'https://localhost:8443/api/v1/support';
    private adminApi = 'https://localhost:8443/api/v1/admin/support/tickets';

    public createTicket(request: CreateTicketRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(this.userApi, request);
    }

    public getOpenTickets(): Observable<SupportTicket[]> {
        return this.http.get<SupportTicket[]>(this.adminApi);
    }

    public getConversation(ticketId: string): Observable<SupportMessage[]> {
        return this.http.get<SupportMessage[]>(`${this.adminApi}/${ticketId}/conversation`);
    }

    public reply(ticketId: string, request: ReplyRequest): Observable<SupportMessage> {
        return this.http.post<SupportMessage>(`${this.adminApi}/${ticketId}/reply`, request);
    }

    public closeTicket(ticketId: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.adminApi}/${ticketId}/close`, {});
    }
}