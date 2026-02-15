import { Pictures } from './../../../models/pictures.model';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Stadium } from '../../../models/stadium.model';
import { StadiumService } from '../../../services/stadium.service';
import { CommonModule } from '@angular/common';
import { RemoveConfirmationModalComponent } from "../../remove-confirmation-modal/remove-confirmation-modal.component";
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { finalize } from 'rxjs';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { ActionButtonsComponent } from "../action-buttons/action-buttons.component";

@Component({
	selector: 'app-edit-stadium',
	standalone: true,
	imports: [CommonModule, RemoveConfirmationModalComponent, SuccessModalComponent, LoadingModalComponent, ErrorModalComponent, ActionButtonsComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './edit-stadium.component.html'
})
export class EditStadiumComponent implements OnInit {
	@Input() stadium!: Stadium;

	@Output() backToMenu = new EventEmitter<void>();

	public pictures: Pictures[] = [];
	public pictureToDelete!: Pictures;

	public success = false;
	public successMessage = '';
	public error = false;
	public errorMessage = '';

	public openConfirmationModal = false;
	public loading = false;
	public finish = false;

	private readonly maxPictureSize = 1 * 1024 * 1024; // 1MB

	constructor(private stadiumService: StadiumService) {}

	ngOnInit(): void {
		this.pictures = this.stadium.pictures;
	}

	public verifyPictures(): boolean {
		return this.pictures.length < 5;
	}

	private resetState() {
		this.error = false;
		this.errorMessage = '';
		this.success = false;
		this.successMessage = '';
		this.loading = false;
	}

	public uploadPicture(stadiumName: string, file: File) {
		this.resetState();
		this.loading = true;

		if (file.size > this.maxPictureSize) {
			this.error = true;
			this.errorMessage = 'The picture must be less than 1MB';
			return;
		}

		this.stadiumService.uploadPicture(stadiumName, file).pipe(finalize(() => this.loading = false)).subscribe({
			next: (savedPic) => {
				this.success = true;
				this.successMessage = 'Picture uploaded successfully';
				this.pictures.push(savedPic);
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occurred trying to store the picture';
			}
		});
	}

	public removePicture() {
		this.resetState();

		this.stadiumService.removePicture(this.stadium.name, this.pictureToDelete.publicId).subscribe({
			next: (_) => {
				this.success = true;
				this.successMessage = 'Picture successfully deleted';
				this.pictures = this.pictures.filter(p => p.publicId !== this.pictureToDelete.publicId);
				this.openConfirmationModal = false;
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occurred while trying to delete the image';
			}
		});
	}

	public handleRemovePicture(pict: Pictures) {
		this.openConfirmationModal = true;
		this.pictureToDelete = pict;
	}

	public cancelConfirmationModal() {
		this.openConfirmationModal = false;
	}

	public handleFileUpload(event: Event): void {
		const input = event.target as HTMLInputElement;
		if (input.files && input.files.length > 0) {
			const file = input.files[0];

			if (file.size > this.maxPictureSize) {
				this.error = true;
				this.errorMessage = 'The picture must be less than 1MB';
				this.loading = false;
				return;
			}
			this.uploadPicture(this.stadium.name, file);
		}
	}

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
	}

	public confirm() {
		this.finish = true;
	}
}