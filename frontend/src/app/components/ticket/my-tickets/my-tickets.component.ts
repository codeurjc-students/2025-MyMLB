import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { Ticket } from '../../../models/ticket/ticket.model';

@Component({
	selector: 'app-my-tickets',
	imports: [CommonModule],
	standalone: true,
	templateUrl: './my-tickets.component.html',
})
export class MyTicketsComponent implements OnInit {
	private userService = inject(UserService);

	public tickets: Ticket[] = [];

	public error = false;
	public errorMessage = '';

	ngOnInit() {}

	private fetchTickets() {
		this.userService.getPurchasedTickets().subscribe({
			next: (response) => this.tickets = response,
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while loading the tickets';
			}
		});
	}
}