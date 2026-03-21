import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from '../../../app/app';
import { Router, NavigationEnd } from '@angular/router';
import { of, Subject } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';
import { ThemeService } from '../../../app/services/theme.service';
import { AnalyticsService } from '../../../app/services/analytics.service';
import { AuthResponse } from '../../../app/models/auth.model';

describe('App Component Tests', () => {
	let fixture: ComponentFixture<AppComponent>;
	let appComponent: AppComponent;
	let routerEvents$: Subject<any>;
	let analyticsServiceSpy: jasmine.SpyObj<AnalyticsService>;
	let mockRouter: any;

	beforeEach(() => {
		routerEvents$ = new Subject();
		analyticsServiceSpy = jasmine.createSpyObj('AnalyticsService', ['updateVisualizations']);

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
				{ provide: AnalyticsService, useValue: analyticsServiceSpy },
				{ provide: ThemeService, useValue: mockThemeService },
				{ provide: ChangeDetectorRef, useValue: { detectChanges: () => {} } },
			],
		});

		fixture = TestBed.createComponent(AppComponent);
		appComponent = fixture.componentInstance;
		const response: AuthResponse = {
			status: 'SUCCESS',
			message: 'Successfully updated the visualizations'
		}
		analyticsServiceSpy.updateVisualizations.and.returnValue(of(response));
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