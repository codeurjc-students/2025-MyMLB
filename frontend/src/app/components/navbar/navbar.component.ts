import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../services/theme.service';
import { UserRole } from '../../models/auth/user-role.model';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule],
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
	@Input() isDarkMode: boolean = false;
	@Output() toggleDarkMode: EventEmitter<void> = new EventEmitter();

	public roles: string[] = ['GUEST'];
	public username: string = '';

	constructor(private authService: AuthService, private themeService: ThemeService) {}

	public ngOnInit() {
		this.authService.currentUser$.subscribe(user => {
			if (user) {
				this.roles = user.roles;
				this.username = user.username;
			} else {
				this.roles = ['GUEST'];
				this.username = '';
			}
		});
	}

	public toggleDarkModeButton(): void {
		this.toggleDarkMode.emit();
	}

	get profileLink(): string {
		return this.roles.includes('USER') || this.roles.includes('ADMIN') ? '/profile' : '/auth';
	}
}