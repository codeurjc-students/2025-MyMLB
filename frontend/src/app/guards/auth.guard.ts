import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class AuthGuard implements CanActivate {
	constructor(private authService: AuthService, private router: Router) {}

	canActivate() {
		return this.authService.getActiveUser().pipe(
			map((user) => {
				if (user && user.username) {
					return true;
				} else {
					this.router.navigate(['auth']);
					return false;
				}
			}),
			catchError(() => {
				this.router.navigate(['auth']);
				return of(false);
			})
		);
	}
}