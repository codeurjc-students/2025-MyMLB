import { Component, EventEmitter, OnInit, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DropdownMenuComponent } from "./dropdown-menu/dropdown-menu.component";
import { filter } from 'rxjs';

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
	public currentRoute = '';

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

		this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
			this.currentRoute = event.urlAfterRedirects;
		});

		this.currentRoute = this.router.url;
	}

	public toggleDarkModeButton(): void {
		this.toggleDarkMode.emit();
	}

	get profileLink(): string {
		return this.roles.includes('USER') || this.roles.includes('ADMIN') ? '/profile' : '/auth';
	}

	public applyStyle(page: string): string {
		return this.currentRoute === page || this.currentRoute.startsWith(page + '/') ? 'active-nav' : '';
	}
}