import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PasswordPhaseComponent } from '../../app/Components/PasswordRecovery/NewPasswordPhase/NewPasswordPhase.component';
import { FormBuilder } from '@angular/forms';
import { AuthService } from '../../app/Services/Auth.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/Models/Auth/AuthResponse';

describe('New Password Phase Integration Tests', () => {
	let fixture: ComponentFixture<PasswordPhaseComponent>;
	let component: PasswordPhaseComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const resetPasswordUrl = 'https://localhost:8443/api/auth/reset-password';

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);
		TestBed.configureTestingModule({
			imports: [PasswordPhaseComponent],
			providers: [
				FormBuilder,
				AuthService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		fixture = TestBed.createComponent(PasswordPhaseComponent);
		component = fixture.componentInstance;
		component.code = '1234';
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should submit the password and show a success message if everything is valid', () => {
		component.passwordForm.setValue({ newPassword: 'newPassword' });
		component.submitPassword();

		const request = httpMock.expectOne(resetPasswordUrl);
		expect(request.request.method).toBe('POST');
		expect(request.request.body).toEqual({
			code: '1234',
			newPassword: 'newPassword',
		});

		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Password restored',
		};

		request.flush(mockResponse);

		expect(component.successMessage).toBe('Password restored');
		expect(component.showSuccess).toBeTrue();
		expect(component.errorMessage).toBe('');
	});

	it('should show an error message with an invalid procedure', () => {
		component.passwordForm.setValue({ newPassword: 'anyPassword' });
		component.submitPassword();

		const request = httpMock.expectOne(resetPasswordUrl);
		request.flush('Invalid or expired code', { status: 400, statusText: 'Bad Request' });

		expect(component.showSuccess).toBeFalse();
	});

	it('should not submit if the form is invalid', () => {
		component.passwordForm.setValue({ newPassword: '' });
		component.submitPassword();

		httpMock.expectNone(resetPasswordUrl);
		expect(component.successMessage).toBe('');
		expect(component.showSuccess).toBeFalse();
	});

	it('should navigate to the login form on toggleToLoginForm()', () => {
		component.toggleToLoginForm();
		expect(routerSpy.navigate).toHaveBeenCalledWith(['login']);
	});
});