import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { initFlowbite } from 'flowbite';
import { NavbarComponent } from './components/navbar/navbar.component';
import { ThemeService } from './services/theme.service';
import { ViewportScroller } from '@angular/common';
import { Footer } from "./components/footer/footer.component";
import { AnalyticsService } from './services/analytics.service';
import { AuthService } from './services/auth.service';

@Component({
	selector: 'app-root',
	standalone: true,
	imports: [RouterOutlet, NavbarComponent, Footer],
	templateUrl: './app.html',
})
export class AppComponent implements OnInit {
	private analyticsService = inject(AnalyticsService);
	private authService = inject(AuthService);
	private router = inject(Router);
	private themeService = inject(ThemeService);
	private cdr = inject(ChangeDetectorRef);
	private viewportScroller = inject(ViewportScroller);
	public hideNavbar = false;
	public isDarkMode = false;

	constructor() {
		this.router.events.subscribe((event) => {
			const url = this.router.url;
			this.hideNavbar = url.startsWith('/auth') || url.startsWith('/recovery') || url.startsWith('/error') || url.startsWith('/coming-soon');
			if (event instanceof NavigationEnd) {
				this.viewportScroller.scrollToPosition([0, 0]);
			}
		});
	}

	ngOnInit(): void {
		this.handleTokenExpiration();
		initFlowbite();
		this.themeService.initTheme();
		this.isDarkMode = document.documentElement.classList.contains('dark');
		this.analyticsService.trackVisitor();
	}

	private handleTokenExpiration() {
		if (!localStorage.getItem('lastActive')) {
			localStorage.setItem('lastActive', Date.now().toString());
		}

		setInterval(() => {
            const lastActive = parseInt(localStorage.getItem('lastActive') || '0');
            const now = Date.now();
            const diff = (now - lastActive) / 1000 / 60;

            const user = this.authService.getCurrentUser();
            if (user.roles[0] !== 'GUEST' && diff > 20) {
                this.authService.logoutUser().subscribe(() => {
                    this.authService.handleSessionExpired();
                });
            }
        }, 60000);
	}

	public toggleDarkMode(): void {
		this.themeService.toggleDarkMode();
		this.isDarkMode = document.documentElement.classList.contains('dark');
		this.cdr.detectChanges();
	}
}