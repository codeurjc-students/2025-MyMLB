import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-coming-soon',
	standalone: true,
	templateUrl: './coming-soon.component.html'
})
export class ComingSoonComponent {
	private router = inject(Router);

	public navigateToHome() {
		this.router.navigate(['/']);
	}
}