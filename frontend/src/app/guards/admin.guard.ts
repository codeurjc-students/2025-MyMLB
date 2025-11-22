import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class AdminGuard implements CanActivate {
	constructor(private authService: AuthService, private router: Router) {}

	canActivate() {
		return this.authService.getActiveUser().pipe(
			map((user) => {
				if (user.roles.includes('ADMIN')) {
					return true;
				}
				else {
					this.router.navigate(['auth']); // CAMBIAR A /error
					return false;
				}
			}),
			catchError(() => {
				this.router.navigate(['auth']); // CAMBIAR A /error
				return of(false);
			})
		);
	}
}