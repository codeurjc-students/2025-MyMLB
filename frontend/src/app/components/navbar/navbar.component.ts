import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../services/Theme.service';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule],
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
	@Input() isDarkMode: boolean = false;
	@Output() toggleDarkMode: EventEmitter<void> = new EventEmitter();

	public roles: Array<string> = ['GUEST'];
	public username: string = '';

	constructor(private authService: AuthService, private themeService: ThemeService) {}

	public ngOnInit() {
		this.authService.getActiveUser().subscribe((response) => {
			this.roles = response.roles;
			this.username = response.username;
		});
	}

	public toggleDarkModeButton(): void {
		this.toggleDarkMode.emit();
	}
}