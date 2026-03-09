import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { Ticket } from '../../../models/ticket/ticket.model';
import { AuthService } from '../../../services/auth.service';
import { TicketService } from '../../../services/ticket/ticket.service';
import {MatIconModule} from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";

@Component({
	selector: 'app-my-tickets',
	imports: [CommonModule, MatIconModule, MatTooltip, ErrorModalComponent],
	standalone: true,
	templateUrl: './my-tickets.component.html',
})
export class MyTicketsComponent implements OnInit {
	private userService = inject(UserService);
	private authService = inject(AuthService);
	private ticketService = inject(TicketService);

	public tickets: Ticket[] = [];
	public username = '';

	public error = false;
	public errorMessage = '';

	ngOnInit() {
		this.authService.getActiveUser().subscribe(response => {
			this.username = response.username;
		});
		this.fetchTickets();
	}

	private fetchTickets() {
		this.userService.getPurchasedTickets().subscribe({
			next: (response) => this.tickets = response,
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while loading the tickets';
			}
		});
	}

	public downloadPdf(ticketId: number) {
		this.ticketService.downloadPdf(ticketId).subscribe({
			next: (pdf) => {
				const url = window.URL.createObjectURL(pdf);
				const link = document.createElement('a');
				link.href = url;
				link.download = 'MLB_Portal_Ticket.pdf';
				link.click();
				window.URL.revokeObjectURL(url);
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur with the pdf download';
			}
		});
	}
}