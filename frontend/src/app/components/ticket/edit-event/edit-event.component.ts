import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../services/ticket/event.service';
import { ActivatedRoute, Router } from '@angular/router';
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { EventResponse } from '../../../models/ticket/event-response.model';
import { EventEditRequest } from '../../../models/ticket/event-edit-request.model';

@Component({
	selector: 'app-edit-event',
	standalone: true,
	imports: [CommonModule, FormsModule, SuccessModalComponent, ErrorModalComponent],
	templateUrl: './edit-event.component.html',
})
export class EditEventComponent implements OnInit {
	private eventService = inject(EventService);
	private route = inject(ActivatedRoute);
	private router = inject(Router);

	private matchId!: number;
	private eventId!: number;

	public eventInfo: EventResponse | null = null;

	public success = false;
	public error = false;
	public successMessage = '';
	public errorMessage = '';

	ngOnInit() {
		this.route.queryParams.subscribe(param => {
			this.eventId = +param['eventId'];
			if (this.eventId) {
				this.eventService.getEventById(this.eventId).subscribe({
					next: (response) => {
						this.eventInfo = response;
					},
					error: (_) => {
						this.error = true;
						this.errorMessage = 'Error fetching the match information';
					}
				});
			}
		});
	}

	private prepareRequest() {
		return {
			eventId: this.eventId,
			sectorIds: this.eventInfo?.sectors.map(sector => sector.sectorId),
			prices: this.eventInfo?.sectors.map(sector => sector.price)
		} as EventEditRequest
	}

	public editEvent() {
		const request = this.prepareRequest();
		this.eventService.editEvent(request).subscribe({
			next: (_) => {
				this.success = true;
				this.successMessage = 'Event Successfully Modified';
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while attempting to modified the event';
			}
		});
	}

	public goToMenu() {
		this.router.navigate(['/']);
	}
}