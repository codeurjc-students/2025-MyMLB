import { Component, OnInit, inject } from '@angular/core';
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

	private router = inject(Router);
	private route = inject(ActivatedRoute);
	private errorService = inject(ErrorService);

	ngOnInit() {
		const payload = this.errorService.getError();

		const nav = this.router.currentNavigation();
		const state = nav?.extras?.state as { code?: number; message?: string };

		this.errorCode = payload?.code ?? state?.code ?? this.route.snapshot.data['code'] ?? 0;

		this.message =
			payload?.message ??
			state?.message ??
			this.route.snapshot.data['message'] ??
			'An error occurred';
	}

	public navigateToHome() {
		this.router.navigate(['/']);
	}
}