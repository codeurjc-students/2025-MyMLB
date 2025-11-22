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

@Component({
	selector: 'app-edit-stadium',
	standalone: true,
	imports: [CommonModule, RemoveConfirmationModalComponent, SuccessModalComponent, LoadingModalComponent, ErrorModalComponent],
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
	public showEditMenu = false;
	public finish = false;

	public removeModalIcon = `<svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20" class="w-12 h-12 mx-auto">
		<path fill-rule="evenodd" clip-rule="evenodd"
			d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
		></path>
	</svg>`;

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

		if (file.type !== 'image/webp') {
			this.error = true;
			this.errorMessage = 'Only .webp images are allowed';
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
				this.errorMessage = 'Cannot delete the last picture of a stadium';
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

			if (file.type !== 'image/webp') {
				this.error = true;
				this.errorMessage = 'Only .webp images are allowed';
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