import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { Ticket } from '../../../models/ticket/ticket.model';
import { AuthService } from '../../../services/auth.service';

@Component({
	selector: 'app-my-tickets',
	imports: [CommonModule],
	standalone: true,
	templateUrl: './my-tickets.component.html',
})
export class MyTicketsComponent implements OnInit {
	private userService = inject(UserService);
	private authService = inject(AuthService);

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
}