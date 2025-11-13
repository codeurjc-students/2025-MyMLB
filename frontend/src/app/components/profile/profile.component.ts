import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { RemoveConfirmationModalComponent } from "../remove-confirmation-modal/remove-confirmation-modal.component";

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
 	imports: [RemoveConfirmationModalComponent],
})
export class ProfileComponent implements OnInit {
	public username = '';
	public errorMessage = '';
	public showPanel = false;
	public icon = `<svg
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
				stroke-width="2"
				stroke="currentColor"
				class="w-12 h-12 mx-auto text-red-500 dark:text-red-400"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h6a2 2 0 012 2v1"
				/>
			</svg>`;

	constructor(private authService: AuthService, private router: Router) {}

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe({
			next: response => this.username = response.username,
			error: (_) => this.errorMessage = 'Unexpected error while retrieving the user'
		});
	}

	public confirmLogout() {
		this.authService.logoutUser().subscribe((_) => this.router.navigate(['/']));
	}

	public cancelLogout() {
		this.showPanel = false;
	}

	public logoutButton() {
		this.showPanel = true;
	}
}