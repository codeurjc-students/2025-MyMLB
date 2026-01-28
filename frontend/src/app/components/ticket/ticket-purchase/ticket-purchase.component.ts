import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Seat } from '../../../models/ticket/seat.model';
import { TicketService } from '../../../services/ticket/ticket.service';
import { PurchaseRequest } from '../../../models/ticket/purchase-request.model';
import { EventResponse } from '../../../models/ticket/event-response.model';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { SuccessModalComponent } from "../../success-modal/success-modal.component";

@Component({
	selector: 'app-ticket-purchase',
	imports: [CommonModule, ReactiveFormsModule, ErrorModalComponent, SuccessModalComponent],
	standalone: true,
	templateUrl: './ticket-purchase.component.html',
})
export class TicketPurchaseComponent implements OnInit {
	@Input() event!: EventResponse;
	@Input() tickets!: number;
	@Input() seats!: Seat[];
	@Input() totalPrice!: number;

	private ticketService = inject(TicketService);
	private fb = inject(FormBuilder);

	public request!: PurchaseRequest;

	public paymentForm!: FormGroup;
	public loading = false;
	public error = false;
	public success = false;

	public errorMessage = '';
	public successMessage = '';

	ngOnInit() {
		this.initForm();
	}

	private initForm() {
		this.paymentForm = this.fb.group({
			ownerName: ['', [Validators.required]],
			cardNumber: ['', [Validators.required, Validators.pattern('^[0-9\\s]{13,19}$')]],
			cvv: ['', [Validators.required, Validators.pattern('^[0-9]{3,4}$')]],
			expirationDate: ['', [Validators.required]]
		});
	}

	public invalidField(field: string) {
		const control = this.paymentForm.get(field);
		return !!(control && control.invalid && (control.dirty || control.touched));
	}

	private prepareRequest() {
		const value = this.paymentForm.value;
		return {
			eventManagerId: this.event.id,
			ticketAmount: this.tickets,
			seats: this.seats,
			ownerName: value.ownerName,
			cardNumber: value.cardNumber,
			cvv: value.cvv,
			expirationDate: new Date(value.expirationDate)
		} as PurchaseRequest;
	}

	public purchase() {
		if (this.paymentForm.invalid) {
			return;
		}

		this.loading = true;
		const request = this.prepareRequest();

		this.ticketService.purchaseTicket(request).subscribe({
			next: (_) => {
				this.loading = false;
				this.success = true;
				this.successMessage = 'Thank you for your purchase';
			},
			error: (_) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = 'An error occur during the payment';
			}
		});
	}
}