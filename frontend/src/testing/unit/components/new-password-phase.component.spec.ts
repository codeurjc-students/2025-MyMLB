import { TestBed } from '@angular/core/testing';
import { PasswordPhaseComponent } from '../../../app/components/password-recovery/new-password/new-password.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../app/services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { of, throwError } from 'rxjs';
import { AuthResponse } from '../../../app/Models/auth/auth-response.model';

describe('Password Phase Component Tests', () => {
	let component: PasswordPhaseComponent;
	let authServiceSpy: jasmine.SpyObj<AuthService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		authServiceSpy = jasmine.createSpyObj('AuthService', ['resetPassword']);
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [PasswordPhaseComponent, ReactiveFormsModule, CommonModule],
			providers: [
				{ provide: AuthService, useValue: authServiceSpy },
				{ provide: Router, useValue: routerSpy },
			],
		});

		const fixture = TestBed.createComponent(PasswordPhaseComponent);
		component = fixture.componentInstance;
		component.code = '1234';
		fixture.detectChanges();
	});

	it('should create form with newPassword control', () => {
		expect(component.passwordForm.contains('newPassword')).toBeTrue();
	});

	it('should not submit if form is invalid', () => {
		component.passwordForm.setValue({ newPassword: '' });
		component.submitPassword();
		expect(authServiceSpy.resetPassword).not.toHaveBeenCalled();
	});

	it('should call resetPassword and show success message on success', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Password restored',
		};

		component.passwordForm.setValue({ newPassword: 'newPassword' });
		authServiceSpy.resetPassword.and.returnValue(of(mockResponse));

		component.submitPassword();

		expect(authServiceSpy.resetPassword).toHaveBeenCalledWith({
			code: '1234',
			newPassword: 'newPassword',
		});
		expect(component.successMessage).toBe('Password restored');
		expect(component.showSuccess).toBeTrue();
		expect(component.errorMessage).toBe('');
	});

	it('should set errorMessage on failure', () => {
		component.passwordForm.setValue({ newPassword: 'anyPassword' });
		authServiceSpy.resetPassword.and.returnValue(
			throwError(() => ({ message: 'Invalid request. Please check the submitted data.' }))
		);

		component.submitPassword();

		expect(component.errorMessage).toBe('Invalid request. Please check the submitted data.');
		expect(component.showSuccess).toBeFalse();
	});

	it('should navigate to the login form on toggleToLoginForm()', () => {
		component.toggleToLoginForm();
		expect(routerSpy.navigate).toHaveBeenCalledWith(['login']);
	});
});