import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-home',
	standalone: true,
	templateUrl: './Home.component.html',
})
export class HomeComponent {

	constructor(private router: Router) {}

	public button() {
		this.router.navigate(['auth']);
	}
}