import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ErrorService } from '../../services/error.service';

@Component({
	selector: 'app-error',
	standalone: true,
	templateUrl: './error.component.html',
})
export class ErrorComponent implements OnInit {
	public errorCode: number = 0;
	public message: string = '';

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private errorService: ErrorService
	) {}

	ngOnInit() {
		const payload = this.errorService.getError();
		this.errorCode = payload?.code ?? this.route.snapshot.data['code'] ?? 0;
		this.message = payload?.message ?? this.route.snapshot.data['message'] ?? 'An error occurred';
	}

	public navigateToHome() {
		this.router.navigate(['/']);
	}
}