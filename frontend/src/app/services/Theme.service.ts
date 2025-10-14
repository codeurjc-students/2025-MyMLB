import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ThemeService {
	private readonly darkClass = 'dark';

	constructor() {}

	public toggleDarkMode() {
		const html = document.documentElement;
		html.classList.toggle(this.darkClass);
		localStorage.setItem('theme', html.classList.contains(this.darkClass) ? 'dark' : 'light');
	}

	public initTheme(): void {
		const theme = localStorage.getItem('theme');

		if (theme === 'dark') {
			document.documentElement.classList.add(this.darkClass);
		}
		else if (theme === 'light') {
			document.documentElement.classList.remove(this.darkClass);
		}
		else {
			const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
			document.documentElement.classList.toggle(this.darkClass, prefersDark);
		}
	}
}