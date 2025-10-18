import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { initFlowbite } from 'flowbite';
import { NavbarComponent } from './components/navbar/navbar.component';
import { ThemeService } from './services/theme.service';

@Component({
	selector: 'app-root',
	standalone: true,
	imports: [RouterOutlet, NavbarComponent],
	templateUrl: './app.html',
})
export class AppComponent implements OnInit {
	title = 'web-app';
	public hideNavbar = false;
	public isDarkMode = false;

	constructor(private router: Router, private themeService: ThemeService, private cdr: ChangeDetectorRef) {
		this.router.events.subscribe(() => {
			const url = this.router.url;
			this.hideNavbar = url.startsWith('/auth') || url.startsWith('/recovery') || url.startsWith('/error');
		});
	}

	ngOnInit(): void {
		initFlowbite();
		this.themeService.initTheme();
		this.isDarkMode = document.documentElement.classList.contains('dark');
	}

	public toggleDarkMode(): void {
		this.themeService.toggleDarkMode();
		this.isDarkMode = document.documentElement.classList.contains('dark');
		this.cdr.detectChanges();
	}
}