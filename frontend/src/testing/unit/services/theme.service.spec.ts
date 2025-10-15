import { TestBed } from '@angular/core/testing';
import { ThemeService } from '../../../app/services/theme.service';

describe('Theme Service Tests', () => {
	let themeService: ThemeService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [ThemeService]
		});
		themeService = TestBed.inject(ThemeService);
		localStorage.clear();
		document.documentElement.className = '';
	});

	describe('Toggle Dark Mode', () => {
		it('should store in localstorage the dark mode', () => {
			themeService.toggleDarkMode();

			expect(document.documentElement.classList.contains('dark')).toBeTrue();
			expect(localStorage.getItem('theme')).toBe('dark');
		});

		it('should store in localstorage the light mode', () => {
			document.documentElement.classList.add('dark');
			themeService.toggleDarkMode();

			expect(document.documentElement.classList.contains('dark')).toBeFalse();
			expect(localStorage.getItem('theme')).toBe('light');
		});
	});

	describe('Init Theme', () => {
		it('should add the "dark" class to the <html> tag if the dark mode is active', () => {
			localStorage.setItem('theme', 'dark');
			themeService.initTheme();

			expect(document.documentElement.classList.contains('dark')).toBeTrue();
			expect(document.documentElement.classList.contains('light')).toBeFalse();
		});

		it('should remove the "dark" class from the <html> tag if the dark mode is deactivated', () => {
			localStorage.setItem('theme', 'light');
			themeService.initTheme();

			expect(document.documentElement.classList.contains('dark')).toBeFalse();
		});

		it('should follow the system preferences if no theme is set in localStorage', () => {
			const mediaSpy = spyOn(window, 'matchMedia').and.returnValue({
				matches: true,
				addEventListener: () => {},
				removeEventListener: () => {}
			} as any);

			themeService.initTheme();

			expect(mediaSpy).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
			expect(document.documentElement.classList.contains('dark')).toBeTrue();
		});
	});
});