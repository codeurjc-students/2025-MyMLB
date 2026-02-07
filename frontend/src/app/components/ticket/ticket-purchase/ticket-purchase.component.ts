import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, inject, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import flatpickr from 'flatpickr';
import monthSelectPlugin from 'flatpickr/dist/plugins/monthSelect/index.js';
import { Seat } from '../../../models/ticket/seat.model';
import { TicketService } from '../../../services/ticket/ticket.service';
import { PurchaseRequest } from '../../../models/ticket/purchase-request.model';
import { EventResponse } from '../../../models/ticket/event-response.model';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { SuccessfullPurchaseComponent } from "../successfull-purchase/successfull-purchase.component";

@Component({
	selector: 'app-ticket-purchase',
	imports: [CommonModule, ReactiveFormsModule, ErrorModalComponent, SuccessModalComponent, LoadingModalComponent, SuccessfullPurchaseComponent],
	standalone: true,
	templateUrl: './ticket-purchase.component.html',
})
export class TicketPurchaseComponent implements OnInit, AfterViewInit {
	@Input() event!: EventResponse;
	@Input() eventManagerId!: number;
	@Input() tickets!: number;
	@Input() seats!: Seat[];
	@Input() totalPrice!: number;
	@Output() goBack = new EventEmitter<void>();

	@ViewChild('datePicker') datePickerElement!: ElementRef;

	private ticketService = inject(TicketService);
	private fb = inject(FormBuilder);

	public request!: PurchaseRequest;

	public paymentForm!: FormGroup;
	public loading = false;
	public error = false;
	public showSuccesModal = false;
	public successfullPurchase = false;

	public errorMessage = '';
	public successMessage = '';

	ngOnInit() {
		this.initForm();
	}

	ngAfterViewInit() {
		flatpickr(this.datePickerElement.nativeElement, {
			disableMobile: true,
			plugins: [
				monthSelectPlugin({
					shorthand: true,
					dateFormat: "m/y",
					altFormat: "F Y",
				})
			],
			onChange: (_, dateStr) => {
				this.paymentForm.get('expirationDate')?.setValue(dateStr);
			}
		});
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
			eventManagerId: this.eventManagerId,
			ticketAmount: this.tickets,
			seats: this.seats,
			ownerName: value.ownerName,
			cardNumber: value.cardNumber,
			cvv: value.cvv,
			expirationDate: value.expirationDate
		} as PurchaseRequest;
	}

	public purchase() {
		if (this.paymentForm.invalid) {
			this.error = true;
			this.errorMessage = 'Please, fill all fields with the correct format';
			return;
		}

		this.loading = true;
		const request = this.prepareRequest();

		this.ticketService.purchaseTicket(request).subscribe({
			next: (_) => {
				this.loading = false;
				this.showSuccesModal = true;
				this.successMessage = 'Thank you for your purchase!';
			},
			error: (error) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = error.error.message;
			}
		});
	}

	public openSuccessPage() {
		this.showSuccesModal = false;
		this.successfullPurchase = true;
	}

	public previousPage() {
		this.goBack.emit();
	}
}