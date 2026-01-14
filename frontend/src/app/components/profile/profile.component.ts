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

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
	imports: [CommonModule, FormsModule, RemoveConfirmationModalComponent, ProfileButtons, SuccessModalComponent],
})
export class ProfileComponent implements OnInit {
	private authService = inject(AuthService);
	private router = inject(Router);
	private userService = inject(UserService);

	public username = '';
	public errorMessage = '';
	public activeAction: 'logout' | 'delete' | null = null;

	public editRequest!: EditProfileRequest;
	public emailInput = '';
	public passwordInput = '';

	public currentEmail = '';

	public sucess = false;
	public successMessage = '';

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe({
			next: (response) => {
				this.username = response.username;
				this.currentEmail = response.email;
				this.passwordInput = response.password;
			},
			error: () => (this.errorMessage = 'Unexpected error while retrieving the user'),
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
	}

	public editProfile() {
		this.prepareRequest();
		this.userService.editProfile(this.editRequest).subscribe({
			next: (_) => {
				this.sucess = true;
				this.successMessage = 'Profile Updated!';
				this.currentEmail = this.editRequest.email;
			},
			error: (_) => this.errorMessage = 'Invalid format for some of the fields entered'
		});
	}
}