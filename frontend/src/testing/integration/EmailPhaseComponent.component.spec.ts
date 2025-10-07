import { TestBed, ComponentFixture } from '@angular/core/testing';
import { EmailPhaseComponent } from '../../app/Components/PasswordRecovery/EmailPhase/EmailPhase.component';
import { AuthService } from '../../app/Services/Auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { AuthResponse } from '../../app/Models/Auth/AuthResponse';

describe('Email Phase Component Integration Tests', () => {
	let fixture: ComponentFixture<EmailPhaseComponent>;
	let component: EmailPhaseComponent;
	let httpMock: HttpTestingController;

	const forgotPasswordUrl = 'https://localhost:8443/api/auth/forgot-password';

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [EmailPhaseComponent],
			providers: [
				FormBuilder,
				AuthService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			]
		});

		fixture = TestBed.createComponent(EmailPhaseComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should submit valid email and emit emailSent on success', () => {
		const emitSpy = spyOn(component.emailSent, 'emit');
		component.emailForm.setValue({ email: 'test@example.com' });

		component.submitEmail();

		const req = httpMock.expectOne(forgotPasswordUrl);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual({ email: 'test@example.com' });

		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Recovery email sent successfully',
		};
		req.flush(mockResponse);

		expect(component.loading).toBeFalse();
		expect(component.errorMessage).toBe('');
		expect(emitSpy).toHaveBeenCalled();
	});

	it('should show error message on failed request', () => {
		component.emailForm.setValue({ email: 'fail@example.com' });

		component.submitEmail();

		const req = httpMock.expectOne(forgotPasswordUrl);
		req.flush('Resource Not Found', { status: 404, statusText: 'Not Found' });

		expect(component.loading).toBeFalse();
	});

	it('should not submit if form is invalid', () => {
		const emitSpy = spyOn(component.emailSent, 'emit');
		component.emailForm.setValue({ email: '' });

		component.submitEmail();

		httpMock.expectNone(forgotPasswordUrl);
		expect(emitSpy).not.toHaveBeenCalled();
	});
});