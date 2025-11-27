import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ErrorService } from '../services/error.service';
import { of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const ErrorInterceptor: HttpInterceptorFn = (req, next) => {
	const router = inject(Router);
	const errorService = inject(ErrorService);

	return next(req).pipe(
		catchError((error: HttpErrorResponse) => {
			let message = 'An unexpected error occurred';
			let code = error.status;
			const url = req.url;

			if (code === 400) {
				return throwError(() => error);
			}

			if (code === 401) {
                return throwError(() => error);
            }
			else if (code === 403) {
				message = 'Forbidden. You do not have access to this page';
			}
			else if (code === 404) {
				message = 'Resource Not Found';
				return throwError(() => error);
			}
			else if (code === 409) {
				message = 'User already exists.';
				return throwError(() => error);
			}
			else if (code === 500) {
				message = 'Something went wrong in the server, try again later';
			}
			else if (error.error?.message) {
				message = error.error.message;
			}

			errorService.setError(code, message);
			router.navigate(['error']);
			return throwError(() => error);
		})
	);
};