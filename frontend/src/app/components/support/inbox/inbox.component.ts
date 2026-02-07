import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupportService } from '../../../services/support.service';
import { SupportTicket } from '../../../models/support/support-ticket.model';
import { SupportTicketItemComponent } from '../support-ticket-item/support-ticket-item.component';
import { SupportTicketModalComponent } from '../support-ticket-modal/support-ticket-modal.component';

@Component({
    selector: 'app-inbox',
    standalone: true,
    imports: [CommonModule, SupportTicketItemComponent, SupportTicketModalComponent],
    templateUrl: './inbox.component.html'
})
export class InboxComponent implements OnInit {
    private supportService = inject(SupportService);

    public tickets: SupportTicket[] = [];
    public selectedTicketId: number | null = null;

	public error = false;
	public errorMessage = '';

    ngOnInit(): void {
        this.loadTickets();
    }

    public loadTickets() {
        this.supportService.getOpenTickets().subscribe({
            next: (data) => this.tickets = data,
            error: (_) => {
				this.error = true;
				this.errorMessage = 'Failed to load tickets';
			}
        });
    }

    public openTicket(ticketId: number) {
        this.selectedTicketId = ticketId;
    }

    public closeModal() {
        this.selectedTicketId = null;
        this.loadTickets();
    }
}