import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from '../../../app/app';
import { Router, NavigationEnd } from '@angular/router';
import { Subject } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';
import { ThemeService } from '../../../app/services/theme.service';

describe('App Component Tests', () => {
	let fixture: ComponentFixture<AppComponent>;
	let appComponent: AppComponent;
	let routerEvents$: Subject<any>;
	let mockRouter: any;

	beforeEach(() => {
		routerEvents$ = new Subject();

		mockRouter = {
			url: '',
			events: routerEvents$.asObservable(),
		};

		const mockThemeService = {
			initTheme: jasmine.createSpy('initTheme'),
			toggleDarkMode: jasmine.createSpy('toggleDarkMode'),
		};

		TestBed.configureTestingModule({
			imports: [AppComponent],
			providers: [
				{ provide: Router, useValue: mockRouter },
				{ provide: ThemeService, useValue: mockThemeService },
				{ provide: ChangeDetectorRef, useValue: { detectChanges: () => {} } },
			],
		});

		fixture = TestBed.createComponent(AppComponent);
		appComponent = fixture.componentInstance;
	});

	it('should hide navbar for /auth route', () => {
		mockRouter.url = '/auth';
		routerEvents$.next(new NavigationEnd(1, '/auth', '/auth'));
		expect(appComponent.hideNavbar).toBeTrue();
	});

	it('should hide navbar for /recovery route', () => {
		mockRouter.url = '/recovery/password';
		routerEvents$.next(new NavigationEnd(1, '/recovery/password', '/recovery/password'));
		expect(appComponent.hideNavbar).toBeTrue();
	});

	it('should show navbar for other routes', () => {
		mockRouter.url = '/';
		routerEvents$.next(new NavigationEnd(1, '/', '/'));
		expect(appComponent.hideNavbar).toBeFalse();
	});
});