import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ErrorService } from '../services/error.service';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const ErrorInterceptor: HttpInterceptorFn = (req, next) => {
	const router = inject(Router);
	const errorService = inject(ErrorService);

	return next(req).pipe(
		catchError((error: HttpErrorResponse) => {
			let message = 'An unexpected error occurred';
			let code = error.status;

			if (code === 0) {
				message = 'Cannot connect to the server. Please check your internet connection.';
			} else if (code === 400) {
				message = 'Invalid request. Please check the submitted data.';
			} else if (code === 401) {
				message = 'Invalid credentials';
			} else if (code === 403) {
				message = 'Forbidden. You do not have access to this page';
			} else if (code === 404) {
				message = 'Resource Not Found';
			} else if (code === 409) {
				message = 'User already exists.';
			} else if (code === 500) {
				message = 'Something went wrong in the server, try again later';
			} else if (error.error?.message) {
				message = error.error.message;
			}

			errorService.setError(code, message);
			if (code !== 401) {
				router.navigate(['/error']);
			}
			return throwError(() => new Error(message));
		})
	);
};