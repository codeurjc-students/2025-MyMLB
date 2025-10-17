import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NavbarComponent } from "../navbar/navbar.component";

@Component({
	selector: 'app-home',
	standalone: true,
	templateUrl: './home.component.html'
})
export class HomeComponent {

	constructor(private router: Router) {}

	public button() {
		this.router.navigate(['/error'], {
			state: { code: 403, message: 'Access Denied' }
		});
	}
}