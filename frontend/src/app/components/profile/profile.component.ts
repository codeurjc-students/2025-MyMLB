import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { RemoveConfirmationModalComponent } from '../remove-confirmation-modal/remove-confirmation-modal.component';
import { ProfileButtons } from './profile-buttons/profile-buttons.component';

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
	imports: [RemoveConfirmationModalComponent, ProfileButtons],
})
export class ProfileComponent implements OnInit {
	private authService = inject(AuthService);
	private router = inject(Router);

	public username = '';
	public errorMessage = '';
	public activeAction: 'logout' | 'delete' | null = null;

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe({
			next: (response) => (this.username = response.username),
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
}