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

	constructor(private authService: AuthService, private router: Router) {}

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe(response => {
			this.username = response.username;
		});
	}

	public logoutButton() {
		this.authService.logoutUser().subscribe({
			next: (response) => {
				if (response.status === 'SUCCESS') {
					this.router.navigate(['/']);
				}
				else {
					this.errorMessage = response.message;
				}
			},
			error: (err) => this.errorMessage = err.message
		});
	}
}