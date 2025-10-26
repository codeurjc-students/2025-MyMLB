import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from '../../app/components/navbar/navbar.component';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../app/services/auth.service';
import { ThemeService } from '../../app/services/theme.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UserRole } from '../../app/models/auth/user-role.model';
import { provideRouter } from '@angular/router';

describe('Navigation Bar Integration Tests', () => {
	let fixture: ComponentFixture<NavbarComponent>;
	let component: NavbarComponent;
	let httpMock: HttpTestingController;

	const url = 'https://localhost:8443/api/auth/me';

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [NavbarComponent],
			providers: [
				AuthService,
				ThemeService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				provideRouter([])
			],
		});

		fixture = TestBed.createComponent(NavbarComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should obtain the current active user', () => {
		const response: UserRole = {
			username: 'testUser',
			roles: ['GUEST', 'USER'],
		};

		const req = httpMock.expectOne(url);
		expect(req.request.method).toBe('GET');
		req.flush(response);

		expect(component.username).toEqual('testUser');
		expect(component.roles).toContain('USER');
	});

	it('should set the role of the active user to GUEST if there is no user authenticated', () => {
		const response: UserRole = {
			username: '',
			roles: ['GUEST'],
		};

		const req = httpMock.expectOne(url);
		expect(req.request.method).toBe('GET');
		req.flush(response);

		expect(component.username).toEqual('');
		expect(component.roles).toEqual(['GUEST']);
	});
});