import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from '../../app/components/navbar/navbar.component';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../app/services/auth.service';
import { ThemeService } from '../../app/services/theme.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UserRole } from '../../app/models/auth/user-role.model';
import { BackgroundColorService } from '../../app/services/background-color.service';
import { SelectedTeamService } from '../../app/services/selected-team.service';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';

describe('Navigation Bar Integration Tests', () => {
	let fixture: ComponentFixture<NavbarComponent>;
	let component: NavbarComponent;
	let httpMock: HttpTestingController;

	const authUrl = 'https://localhost:8443/api/v1/auth/me';
	const mockBackgroundService = {
		navBarBackground: (abbr: string | undefined) =>
			abbr ? `bg-${abbr.toLowerCase()}` : 'bg-default',
		navBarItemsHover: (abbr: string | undefined) =>
			abbr ? `bg-${abbr.toLowerCase()}` : 'bg-default',
		navBarItemsActive: (abbr: string | undefined) =>
			abbr ? `bg-${abbr.toLowerCase()}` : 'bg-default',
		toggleButton: (abbr: string | undefined) =>
			abbr ? `bg-${abbr.toLowerCase()}` : 'bg-default'
	};

	const selectedTeamSubject = new Subject<any>();
	const mockSelectedTeamService = {
		selectedTeam$: selectedTeamSubject.asObservable(),
		clearSelectedTeam: () => {}
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [NavbarComponent],
			providers: [
				AuthService,
				ThemeService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				provideRouter([]),
				{ provide: BackgroundColorService, useValue: mockBackgroundService },
				{ provide: SelectedTeamService, useValue: mockSelectedTeamService },
			],
		});

		fixture = TestBed.createComponent(NavbarComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		const pending = httpMock.match(() => true);
		pending.forEach((req) => req.flush({}));
		httpMock.verify();
	});

	it('should obtain the current active user', () => {
		const response: UserRole = {
			username: 'testUser',
			roles: ['GUEST', 'USER']
		};

		const req = httpMock.expectOne(authUrl);
		expect(req.request.method).toBe('GET');
		req.flush(response);

		expect(component.username).toEqual('testUser');
		expect(component.roles).toContain('USER');
	});

	it('should set the role of the active user to GUEST if there is no user authenticated', () => {
		const response: UserRole = {
			username: '',
			roles: ['GUEST']
		};

		const req = httpMock.expectOne(authUrl);
		expect(req.request.method).toBe('GET');
		req.flush(response);

		expect(component.username).toEqual('');
		expect(component.roles).toEqual(['GUEST']);
	});

	it('should update navBarStyleClass when a team is selected', () => {
		selectedTeamSubject.next({
			teamStats: { abbreviation: 'NYY' },
		});
		expect(component.navBarStyleClass).toBe('bg-nyy');
	});

	it('should reset navBarStyleClass when route is not /team', () => {
		component.currentRoute = '/home';
		component.navBarStyleClass = 'bg-nyy';
		const result = component.navBarBackgroundColor(undefined);
		expect(result).toBe('bg-default');
	});

	it('should emit toggleDarkMode event when button is clicked', () => {
		spyOn(component.toggleDarkMode, 'emit');
		component.toggleDarkModeButton();
		expect(component.toggleDarkMode.emit).toHaveBeenCalled();
	});
});