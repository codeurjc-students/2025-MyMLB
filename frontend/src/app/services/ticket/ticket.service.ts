import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { PurchaseRequest } from '../../models/ticket/purchase-request.model';
import { Observable } from 'rxjs';
import { Ticket } from '../../models/ticket/ticket.model';
import { PaginatedResponse } from '../../models/pagination.model';
import { environment } from '../../../environments/environment';

@Injectable({
	providedIn: 'root',
})
export class TicketService {
	private http = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/tickets`;

	public purchaseTicket(request: PurchaseRequest): Observable<PaginatedResponse<Ticket>> {
		return this.http.post<PaginatedResponse<Ticket>>(this.apiUrl, request, { withCredentials: true });
	}
}