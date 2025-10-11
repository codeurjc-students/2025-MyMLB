import { TestBed } from '@angular/core/testing';
import { EmailPhaseComponent } from '../../../app/components/password-recovery/email/email.component';
import { AuthService } from '../../../app/services/auth.service';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { of, throwError } from 'rxjs';
import { AuthResponse } from '../../../app/Models/auth/auth-response.model';

describe('Email Phase Component Tests', () => {
	let component: EmailPhaseComponent;
	let authServiceSpy: jasmine.SpyObj<AuthService>;

	beforeEach(() => {
		authServiceSpy = jasmine.createSpyObj('AuthService', ['forgotPassword']);

		TestBed.configureTestingModule({
			imports: [EmailPhaseComponent, ReactiveFormsModule, CommonModule],
			providers: [{ provide: AuthService, useValue: authServiceSpy }],
		});

		const fixture = TestBed.createComponent(EmailPhaseComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create form with email control', () => {
		expect(component.emailForm.contains('email')).toBeTrue();
	});

	it('should not submit if form is invalid', () => {
		component.emailForm.setValue({ email: '' });
		component.submitEmail();
		expect(authServiceSpy.forgotPassword).not.toHaveBeenCalled();
	});

	it('should call forgotPassword and emit emailSent on success', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Recovery email successfully sended',
		};

		const emitSpy = spyOn(component.emailSent, 'emit');
		component.emailForm.setValue({ email: 'test@example.com' });
		authServiceSpy.forgotPassword.and.returnValue(of(mockResponse));

		component.submitEmail();

		expect(component.loading).toBeFalse();
		expect(authServiceSpy.forgotPassword).toHaveBeenCalledWith({ email: 'test@example.com' });
		expect(emitSpy).toHaveBeenCalled();
		expect(component.errorMessage).toBe('');
	});

	it('should set errorMessage on failure', () => {
		component.emailForm.setValue({ email: 'fail@example.com' });
		authServiceSpy.forgotPassword.and.returnValue(
			throwError(() => ({ message: 'Resource Not Found' }))
		);

		component.submitEmail();

		expect(component.loading).toBeFalse();
		expect(component.errorMessage).toBe('Resource Not Found');
	});
});