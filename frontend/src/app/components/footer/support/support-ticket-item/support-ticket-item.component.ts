import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupportTicket } from '../../../../models/support/support-ticket.model';

@Component({
    selector: 'app-support-ticket-item',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './support-ticket-item.component.html'
})
export class SupportTicketItemComponent {
    @Input() ticket!: SupportTicket;
    @Output() open = new EventEmitter<void>();
}