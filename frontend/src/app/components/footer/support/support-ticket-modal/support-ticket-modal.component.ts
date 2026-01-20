import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupportService } from '../../../../services/support/support.service';
import { SupportMessage } from '../../../../models/support/support-message.model';
import { FormsModule } from '@angular/forms';
import { SuccessModalComponent } from "../../../success-modal/success-modal.component";
import { ErrorModalComponent } from "../../../modal/error-modal/error-modal.component";
import { LoadingModalComponent } from "../../../modal/loading-modal/loading-modal.component";
import { EscapeCloseDirective } from "../../../../directives/escape-close.directive";

@Component({
    selector: 'app-support-ticket-modal',
    standalone: true,
    imports: [CommonModule, FormsModule, SuccessModalComponent, ErrorModalComponent, LoadingModalComponent, EscapeCloseDirective],
    templateUrl: './support-ticket-modal.component.html'
})
export class SupportTicketModalComponent implements OnInit {
    private supportService = inject(SupportService);

    @Input() ticketId!: string;
    @Output() close = new EventEmitter<void>();

	public isClosing = false;

    public messages: SupportMessage[] = [];
    public replyBody = '';

	public loading = false;
	public success = false;
	public error = false;

	public successMessage = '';
	public errorMessage = '';

	constructor() {
		this.handleCancel = this.handleCancel.bind(this);
	}

    ngOnInit(): void {
        this.loadConversation();
    }

	public handleCancel() {
		this.isClosing = true;
		setTimeout(() => {
			this.close.emit();
			this.isClosing = false;
		}, 300);
	}

    public loadConversation() {
        this.supportService.getConversation(this.ticketId).subscribe({
            next: (data) => this.messages = data,
            error: (_) => {
				this.error = true;
				this.errorMessage = 'Unexpected error ocurred loading the conversation';
			}
        });
    }

    public sendReply() {
        const request = {
            adminEmail: 'mlbportal29@gmail.com',
            body: this.replyBody
        };

		this.loading = true;
        this.supportService.reply(this.ticketId, request).subscribe({
            next: (_) => {
				this.loading = false;
				this.success = true;
				this.successMessage = 'Response Sent Correctly';
                this.replyBody = '';
                this.loadConversation();
            },
			error: (_) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = 'An error occurred while sending the response.';
			}
        });
    }

    public closeTicket() {
        this.supportService.closeTicket(this.ticketId).subscribe({
            next: (_) => this.close.emit(),
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Unexpected error ocurred closing this ticket.';
			}
        });
    }
}