import { TestBed, ComponentFixture } from '@angular/core/testing';
import { EmailPhaseComponent } from '../../app/components/password-recovery/email/email.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { UserRole } from '../../app/models/auth/user-role.model';

describe('Email Phase Component Integration Tests', () => {
	let fixture: ComponentFixture<EmailPhaseComponent>;
	let component: EmailPhaseComponent;
	let httpMock: HttpTestingController;

	const apiUrl = 'https://localhost:8443/api/auth';
	const forgotPasswordUrl = `${apiUrl}/forgot-password`;
	const defaultGuestUser: UserRole = { username: '', roles: ['GUEST'] };

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [EmailPhaseComponent],
			providers: [
				FormBuilder,
				AuthService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		httpMock = TestBed.inject(HttpTestingController);

		fixture = TestBed.createComponent(EmailPhaseComponent);
		component = fixture.componentInstance;

		const initReq = httpMock.expectOne(`${apiUrl}/me`);
		expect(initReq.request.method).toBe('GET');
		initReq.flush(defaultGuestUser);
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
		const expectedErrorMessage = 'Resource Not Found';
		component.emailForm.setValue({ email: 'fail@example.com' });

		component.submitEmail();

		const req = httpMock.expectOne(forgotPasswordUrl);

		req.flush(expectedErrorMessage, { status: 404, statusText: 'Not Found' });

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