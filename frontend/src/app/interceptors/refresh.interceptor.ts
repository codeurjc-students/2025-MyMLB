import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError, Subject, filter, take } from 'rxjs';

let isRefreshing = false;
let refreshSubject = new Subject<boolean>();

export const RefreshInterceptor: HttpInterceptorFn = (req, next) => {
	const auth = inject(AuthService);

	return next(req).pipe(
		catchError((error) => {
			if (error.status === 401 && !req.url.endsWith('/refresh')) {
				if (!isRefreshing) {
					isRefreshing = true;

					return auth.silentRefresh().pipe(
						switchMap(() => {
							isRefreshing = false;
							refreshSubject.next(true);
							return next(req);
						}),
						catchError((err) => {
							isRefreshing = false;
							refreshSubject.next(false);
							auth.handleSessionExpired();
							return throwError(() => err);
						})
					);
				}

				return refreshSubject.pipe(
					filter((ok) => ok === true),
					take(1),
					switchMap(() => next(req))
				);
			}

			return throwError(() => error);
		})
	);
};