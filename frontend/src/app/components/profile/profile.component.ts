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