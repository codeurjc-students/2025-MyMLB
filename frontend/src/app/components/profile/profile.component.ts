import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { RemoveConfirmationModalComponent } from '../remove-confirmation-modal/remove-confirmation-modal.component';
import { ProfileButtons } from './profile-buttons/profile-buttons.component';
import { EditProfileRequest } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { SuccessModalComponent } from "../success-modal/success-modal.component";
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ErrorModalComponent } from "../modal/error-modal/error-modal.component";
import { LoadingModalComponent } from "../modal/loading-modal/loading-modal.component";
import { Pictures } from '../../models/pictures.model';

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
	imports: [CommonModule, FormsModule, RemoveConfirmationModalComponent, ProfileButtons, SuccessModalComponent, ErrorModalComponent, LoadingModalComponent],
})
export class ProfileComponent implements OnInit {
	private authService = inject(AuthService);
	private router = inject(Router);
	private userService = inject(UserService);

	public username = '';

	public error = false;
	public errorMessage = '';
	public activeAction: 'logout' | 'delete' | null = null;

	public editRequest: EditProfileRequest = {};
	public pictureSrc: Pictures | null = null;
	public emailInput = '';
	public passwordInput = '';

	public currentEmail = '';
	public oldEmail = '';

	public notificationsEnabled = true;

	public sucess = false;
	public successMessage = '';

	public loading = false;

	private readonly maxPictureSize = 1 * 1024 * 1024; // 1MB

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe({
			next: (response) => {
				this.username = response.username;
			},
			error: () => {
				this.error = true;
				this.errorMessage = 'Unexpected error while retrieving the user';
			}
		});

		this.retrieveProfileData();
	}

	private retrieveProfileData() {
		this.userService.getUserProfile().subscribe({
			next: (response) => {
				this.currentEmail = response.email;
				this.oldEmail = response.email;
				this.pictureSrc = response.picture;
				this.notificationsEnabled = response.enableNotifications ?? true;
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Unexpected error while retrieving the profile data';
			}
		});
	}

	public openModal(action: 'logout' | 'delete') {
		this.activeAction = action;
	}

	public cancel() {
		this.activeAction = null;
	}

	public confirm() {
		if (this.activeAction === 'logout') {
			this.authService.logoutUser().subscribe(() => this.router.navigate(['/']));
		}

		if (this.activeAction === 'delete') {
			this.authService.deleteAccount().subscribe(() => this.router.navigate(['/']));
		}

		this.activeAction = null;
	}

	private prepareRequest() {
		if (this.emailInput !== '') {
			this.editRequest.email = this.emailInput;
		}
		if (this.passwordInput !== '') {
			this.editRequest.password = this.passwordInput;
		}
		this.editRequest.enableNotifications = this.notificationsEnabled;
	}

	public editProfile() {
		this.prepareRequest();
		this.userService.editProfile(this.editRequest).subscribe({
			next: (_) => {
				this.sucess = true;
				this.successMessage = 'Profile Updated!';
				this.currentEmail = (this.editRequest.email === null || this.editRequest.email === undefined) ? this.oldEmail : this.editRequest.email;
				this.emailInput = '';
				this.passwordInput = '';
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Invalid format for some of the fields entered';
			}
		});
	}

	public handleProfilePicture(event: Event): void {
		const input = event.target as HTMLInputElement;

		if (input.files && input.files.length > 0) {
			const file = input.files[0];
			if (file.size > this.maxPictureSize) {
				this.error = true;
				this.errorMessage = 'The picture must be less than 1MB';
				this.loading = false;
				input.value = '';
				return;
			}
			this.changeProfilePicture(file);
			input.value = '';
		}
	}

	public getPictureUrl() {
		if (this.pictureSrc === null) {
			return 'assets/account-avatar.png';
		}
		return this.pictureSrc.url;
	}

	private changeProfilePicture(file: File) {
		if (file.size > this.maxPictureSize) {
			this.error = true;
			this.errorMessage = 'The picture must be less than 1MB';
			return;
		}

		this.loading = true;
		this.userService.editProfilePicture(file).pipe(finalize(() => this.loading = false)).subscribe({
			next: (response) => {
				this.sucess = true;
				this.successMessage = 'Picture uploaded successfully';
				this.pictureSrc = response;
				this.userService.setProfilePicture(response.url);
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occurred trying to upload the picture';
			}
		});
	}

	public removeProfilePicture() {
		this.userService.deleteProfilePicture().subscribe({
			next: (_) => {
				this.sucess = true;
				this.successMessage = 'Profile Picture Successfully Deleted';
				this.pictureSrc = null;
				this.userService.setProfilePicture('');
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error ocurr while deleting the profile picture';
			}
		});
	}

	public toggleNotifications() {
		this.notificationsEnabled = !this.notificationsEnabled;
	}
}