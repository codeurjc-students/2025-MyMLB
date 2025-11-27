import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SuccessModalComponent } from '../../success-modal/success-modal.component';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { ActionButtonsComponent } from '../action-buttons/action-buttons.component';
import { StadiumService } from '../../../services/stadium.service';
import { Router } from '@angular/router';
import { CreateStadiumRequest } from '../../../models/stadium.model';

@Component({
	selector: 'app-create-stadium',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule,
		SuccessModalComponent,
		ErrorModalComponent,
		ActionButtonsComponent,
	],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './create-stadium.component.html',
})
export class CreateStadiumComponent {
	public nameInput = '';
	public openingDateInput: number | undefined = undefined;

	public success = false;
	public error = false;

	public successMessage = '';
	public errorMessage = '';

	constructor(private stadiumService: StadiumService, private router: Router) {}

	private buildRequest(): CreateStadiumRequest {
		return {
			name: this.nameInput,
			openingDate: this.openingDateInput
		} as CreateStadiumRequest;
	}

	public saveChanges() {
		const request = this.buildRequest();

		this.stadiumService.createStadium(request).subscribe({
			next: (response) => {
				this.success = true;
				this.successMessage = `${response.name} successfully created`;
			},
			error: (_) => {
				this.error = true;
				if (this.nameInput === '' || this.openingDateInput === undefined) {
					this.errorMessage = 'All the fields are required';
				}
				else {
					this.errorMessage = 'A stadium with this name already exists';
				}
			}
		});
	}

	public returnToHome() {
		this.router.navigate(['/']);
	}

	public getCurrentYear() {
		return new Date().getFullYear();
	}
}