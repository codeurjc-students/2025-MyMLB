import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from '../../app/components/profile/profile.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { UserRole } from '../../app/models/auth/user-role.model';
import { Router } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Profile Component Integration Test', () => {
	let fixture: ComponentFixture<ProfileComponent>;
	let component: ProfileComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const apiUrl = 'https://localhost:8443/api/auth';

	const mockUser: UserRole = {
		username: 'testUser',
		roles: ['USER'],
	};

	const mockLogoutResponse: AuthResponse = {
		status: 'SUCCESS',
		message: 'Logout Successful',
	};

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [ProfileComponent],
			providers: [
				AuthService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		fixture = TestBed.createComponent(ProfileComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should load active user and render username', () => {
		fixture.detectChanges();

		const requests = httpMock.match(`${apiUrl}/me`);
		expect(requests.length).toBe(2);
		requests.forEach((req) => req.flush(mockUser));

		fixture.detectChanges();
		expect(component.username).toBe('testUser');
	});

	it('should show error message if getActiveUser fails', () => {
		fixture.detectChanges();

		const requests = httpMock.match(`${apiUrl}/me`);
		expect(requests.length).toBe(2);
		requests.forEach((req) => req.error(new ErrorEvent('Network error')));

		fixture.detectChanges();
		expect(component.errorMessage).toBe('Unexpected error while retrieving the user');
	});

	it('should trigger logout and navigate to root', () => {
		fixture.detectChanges();

		httpMock.match(`${apiUrl}/me`).forEach((req) => req.flush(mockUser));

		component.confirmLogout();

		const logoutReq = httpMock.expectOne(`${apiUrl}/logout`);
		expect(logoutReq.request.method).toBe('POST');
		logoutReq.flush(mockLogoutResponse);

		fixture.detectChanges();
		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
	});

	it('should show logout confirmation panel when logoutButton is clicked', () => {
		fixture.detectChanges();

		httpMock.match(`${apiUrl}/me`).forEach((req) => req.flush(mockUser));

		expect(component.showPanel).toBeFalse();
		component.logoutButton();
		expect(component.showPanel).toBeTrue();
	});
});