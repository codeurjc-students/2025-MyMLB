import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../services/ticket/event.service';
import { ActivatedRoute, Router } from '@angular/router';
import { EventCreateRequest } from '../../../models/ticket/event-create-request-model';
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { MatchService, ShowMatch } from '../../../services/match.service';

@Component({
	selector: 'app-create-event',
	standalone: true,
	imports: [CommonModule, FormsModule, SuccessModalComponent, ErrorModalComponent],
	templateUrl: './create-event.component.html'
})
export class CreateEventComponent implements OnInit {
	private eventService = inject(EventService);
	private matchService = inject(MatchService);
	private route = inject(ActivatedRoute);
	private router = inject(Router);

	private matchId!: number;
	public matchInfo: ShowMatch | null = null;

	public numSectors!: number;
	public sectorNames: string[] = [];
	public sectorPrices: number[] = [];
	public sectorCapacities: number[] = [];

	public success = false;
	public error = false;
	public errorMessage = '';
	public successMessage = '';

	ngOnInit() {
		this.route.queryParams.subscribe(param => {
			this.matchId = +param['matchId'];
			if (this.matchId) {
				this.matchService.getMatchById(this.matchId).subscribe({
					next: (response) => {
						this.matchInfo = response;
					},
					error: (_) => {
						this.error = true;
						this.errorMessage = 'Error fetching the match information';
					}
				});
			}
		});
	}

	public addSectorName(name: string) {
		const formattedInput = name.trim();

		if (formattedInput && this.sectorNames.length < this.numSectors) {
			this.sectorNames.push(formattedInput);
		}
	}

	public addSectorPrice(price: number) {
		if (price > 0 && this.sectorPrices.length < this.numSectors) {
			this.sectorPrices.push(price);
		}
	}

	public addSectorCapacity(capacity: number) {
		if (capacity > 0 && this.sectorCapacities.length < this.numSectors) {
			this.sectorCapacities.push(capacity);
		}
	}

	public removeSectorName(index: number) {
		this.sectorNames.splice(index, 1);
	}

	public removeSectorPrice(index: number) {
		this.sectorPrices.splice(index, 1);
	}

	public removeSectorCapacities(index: number) {
		this.sectorCapacities.splice(index, 1);
	}

	public resetInputs() {
		this.sectorNames = [];
		this.sectorPrices = [];
		this.sectorCapacities = [];
	}

	public isValid() {
		if (this.numSectors === 0) {
			return false;
		}
		if (this.sectorNames.length < this.numSectors || this.sectorPrices.length < this.numSectors || this.sectorCapacities.length < this.numSectors) {
			return false;
		}
		return true;
	}

	private prepareRequest() {
		return {
			matchId: this.matchId,
			prices: [...this.sectorPrices],
			sectors: this.sectorNames.map((name, index) => ({
				name: name,
				totalCapacity: this.sectorCapacities[index]
			}))
		} as EventCreateRequest
	}

	public createEvent() {
		const request = this.prepareRequest();
		this.eventService.createEvent(request).subscribe({
			next: (_) => {
				this.success = true;
				this.successMessage = 'Event Successfully Created';
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occur while attempting to create the event';
			}
		});
	}

	public goToMenu() {
		this.router.navigate(['/']);
	}
}