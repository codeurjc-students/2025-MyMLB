import { Component, EventEmitter, OnInit, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DropdownMenuComponent } from "./dropdown-menu/dropdown-menu.component";

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule, CommonModule, DropdownMenuComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
	@Input() isDarkMode: boolean = false;
	@Output() toggleDarkMode: EventEmitter<void> = new EventEmitter();

	public roles: string[] = ['GUEST'];
	public username: string = '';

	constructor(private authService: AuthService, private router: Router) {}

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

	// public setActiveSection(section: string) {
	// 	if (this.router.url.includes(section)) {
	// 		return 'active-nav';
	// 	}
	// }
}