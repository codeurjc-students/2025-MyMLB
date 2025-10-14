import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
	selector: 'app-profile',
	standalone: true,
	templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
	public username = '';

	constructor(private authService: AuthService) {}

	ngOnInit(): void {
		this.authService.getActiveUser().subscribe(response => {
			this.username = response.username;
		});
	}
}