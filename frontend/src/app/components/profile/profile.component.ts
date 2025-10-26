import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
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

	public logoutButton() {
		this.showPanel = true;
	}
}