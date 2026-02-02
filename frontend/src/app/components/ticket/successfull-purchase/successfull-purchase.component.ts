import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-successfull-purchase',
	imports: [CommonModule],
	standalone: true,
	templateUrl: './successfull-purchase.component.html'
})
export class SuccessfullPurchaseComponent {
	private router = inject(Router);

	public navigateToHome() {
		this.router.navigate(['/']);
	}
}