import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { EventService } from '../../../services/ticket/event.service';
import { EventResponse } from '../../../models/ticket/event-response.model';
import { Seat } from '../../../models/ticket/seat.model';
import { EventManager } from '../../../models/ticket/event-manager.model';
import { FormsModule } from '@angular/forms';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { ActivatedRoute } from '@angular/router';
import { TicketPurchaseComponent } from "../ticket-purchase/ticket-purchase.component";
import { BackgroundColorService } from '../../../services/background-color.service';
import { NgxImageZoomModule } from 'ngx-image-zoom';

@Component({
	selector: 'app-ticket-selection',
	imports: [CommonModule, FormsModule, ErrorModalComponent, TicketPurchaseComponent, NgxImageZoomModule],
	standalone: true,
	templateUrl: './ticket-selection.component.html',
})
export class TicketSelectionComponent implements OnInit {
	private eventService = inject(EventService);
	private route = inject(ActivatedRoute);
	public backgroundColorService = inject(BackgroundColorService);

	private matchId!: number;

	public event!: EventResponse;
	public availableSectors: EventManager[] = []
	public availableSeats: Seat[] = [];

	public selectedSectorId : number = 0;
	public ticketAmount!: number;
	public selectedSeats : Seat[] = [];

	public error = false;
	public success = false;
	public confirm = false;

	public errorMessage = '';
	public successMessage = '';

	public totalPrice: number = 0;
	public isPictureOpen = false;

	ngOnInit() {
		this.route.queryParams.subscribe(param => {
			this.matchId = +param['matchId'];
			if (this.matchId) {
				this.loadEventInfo();
			}
		});
	}

	private loadEventInfo() {
		this.eventService.getEventByMatchId(this.matchId).subscribe({
			next: (response) => {
				this.event = response;
				this.loadAvailableSectors();
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while loading the event'
			}
		});
	}

	private loadAvailableSectors() {
		this.eventService.getAvailableSectors(this.event?.id).subscribe({
			next: (response) => {
				this.availableSectors = response.content;
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while loading the sectors'
			}
		});
	}

	private loadAvailableSeats() {
		this.eventService.getAvailableSeats(this.event?.id, this.selectedSectorId).subscribe({
			next: (response) => {
				this.availableSeats = response.content;
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while loading the seats'
			}
		});
	}

	public onTicketAmountChange() {
		this.selectedSeats = [];
		this.selectedSectorId = Number(this.selectedSectorId);
		this.totalPrice = this.calculateTotalPrice();
	}

	public onSectorChange() {
		this.selectedSectorId = Number(this.selectedSectorId);
		this.totalPrice = this.calculateTotalPrice();
		this.selectedSeats = [];
		if (this.selectedSectorId !== 0) {
			this.loadAvailableSeats();
		}
		else {
			this.availableSeats = [];
		}
	}

	private calculateTotalPrice() {
		if (!this.ticketAmount || !this.selectedSectorId || this.selectedSectorId === 0) {
			return 0;
		}
		const selectedSector = this.availableSectors.find(sector => sector.id === this.selectedSectorId);
		return selectedSector ? selectedSector.price * this.ticketAmount : 0;
	}

	public addSeats(seat: Seat) {
		const index = this.selectedSeats.findIndex(s => s.id === seat.id);
		if (index !== -1) {
			this.selectedSeats.splice(index, 1);
		}
		else {
			if (this.selectedSeats.length < this.ticketAmount) {
				this.selectedSeats.push(seat);
			}
		}
	}

	public isAlreadySelected(seatId: number) {
		return this.selectedSeats.some(seat => seat.id === seatId);
	}

	public openStadiumMap() {
		this.isPictureOpen = !this.isPictureOpen;
	}
}