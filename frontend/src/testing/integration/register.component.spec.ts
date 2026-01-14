import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from '../../app/components/register/register.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UserRole } from '../../app/models/auth/user-role.model';

describe('Register Component Integration Test', () => {
	let fixture: ComponentFixture<RegisterComponent>;
	let registerComponent: RegisterComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const apiUrl = 'https://localhost:8443/api/v1/auth';
	const registerUrl = `${apiUrl}/register`;
	const meUrl = `${apiUrl}/me`;
	const defaultGuestUser: UserRole = { username: '', roles: ['GUEST'], email: 'test@gmail.com', password: '123' };

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [RegisterComponent],
			providers: [
				FormBuilder,
				AuthService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		httpMock = TestBed.inject(HttpTestingController);

		fixture = TestBed.createComponent(RegisterComponent);
		registerComponent = fixture.componentInstance;

		const initReq = httpMock.expectOne(meUrl);
		expect(initReq.request.method).toBe('GET');
		initReq.flush(defaultGuestUser);

		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should submit valid form and show success message', () => {
		registerComponent.registerForm.setValue({
			email: 'test@gmail.com',
			username: 'testUser',
			password: 'test',
		});
		registerComponent.register();

		const req = httpMock.expectOne(registerUrl);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual(registerComponent.registerForm.value);

		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'User registered successfully',
		};

		req.flush(mockResponse);

		expect(registerComponent.showSuccess).toBeTrue();
		expect(registerComponent.successMessage).toBe(mockResponse.message);
		expect(registerComponent.errorMessage).toBe('');
	});

	it('should show error message on failed registration', () => {
		registerComponent.registerForm.setValue({
			email: 'test@gmail.com',
			username: 'testExistingUser',
			password: 'test',
		});

		registerComponent.register();

		const req = httpMock.expectOne(registerUrl);
		req.flush({ message: 'User already exists' }, { status: 409, statusText: 'Conflict' });

		expect(registerComponent.showSuccess).toBeFalse();
	});
});