import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { RouterModule } from '@angular/router';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule],
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
	public roles: Array<string> = ['GUEST'];
	public username: string = '';

	constructor(private authService: AuthService) {}

	public ngOnInit() {
		this.authService.getActiveUser().subscribe(response => {
			this.roles = response.roles;
			this.username = response.username;
		});
	}
}