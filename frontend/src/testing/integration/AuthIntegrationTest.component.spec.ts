import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../app/Services/Auth.service';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { LoginRequest } from '../../app/Models/LoginRequest';
import { AuthResponse } from '../../app/Models/AuthResponse';
import { RegisterRequest } from '../../app/Models/RegisterRequest';

describe('AuthService Integration Test', () => {
	let authService: AuthService;
	let httpMock: HttpTestingController;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [AuthService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});
		authService = TestBed.inject(AuthService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	/**
	 * Helper function to validate an HTTP POST requests, designed to support test refactoring.
	 *
	 * @param url - The expected URL of the POST request.
	 * @param body - The expected request payload.
	 * @param response - The mock response to return to the subscriber.
	 * @param withCredentials - Whether the request should include credentials (default is false).
	 */
	const expectPostRequest = (url: string, body: any, response: any, withCredentials = false) => {
		const req = httpMock.expectOne(url);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual(body);
		if (withCredentials) {
			expect(req.request.withCredentials).toBeTrue();
		}
		req.flush(response);
	};

	describe('Login', () => {
		it('should send the login request and receive a successfull response', () => {
			const loginRequest: LoginRequest = {
				username: 'testUser',
				password: 'test',
			};

			const mockResponse: AuthResponse = {
				status: 'SUCCESS',
				message: 'Auth successful. Tokens are created in cookie.',
			};

			authService.loginUser(loginRequest).subscribe((response) => {
				expect(response).toEqual(mockResponse);
				expect(response.status).toEqual('SUCCESS');
			});
			expectPostRequest(`${authService['apiUrl']}/login`, loginRequest, mockResponse, true);
		});

		it('should handle login failure with 401', () => {
			const loginRequest: LoginRequest = {
				username: 'wrongUser',
				password: 'wrongPass',
			};

			authService.loginUser(loginRequest).subscribe({
				next: () => fail('Expected error, but got success'),
				error: (err) => {
					expect(err.status).toBe(401);
					expect(err.statusText).toBe('Unauthorized');
				},
			});

			const req = httpMock.expectOne('https://localhost:8443/api/auth/login');
			req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });
		});
	});

	describe('Register', () => {
		it('should send register request and receive success response', () => {
			const registerRequest: RegisterRequest = {
				email: 'test@example.com',
				username: 'newUser',
				password: 'securePass',
			};

			const mockResponse: AuthResponse = {
				status: 'SUCCESS',
				message: 'User registered successfully',
			};

			authService.registerUser(registerRequest).subscribe((response) => {
				expect(response).toEqual(mockResponse);
			});
			expectPostRequest(`${authService['apiUrl']}/register`, registerRequest, mockResponse);
		});

		it('should handle register failure with 409 Conflict', () => {
			const registerRequest: RegisterRequest = {
				email: 'test@example.com',
				username: 'existingUser',
				password: 'securePass',
			};

			const errorMessage = 'User already exists';

			authService.registerUser(registerRequest).subscribe({
				next: () => fail('Expected error, but got success'),
				error: (err) => {
					expect(err.status).toBe(409);
					expect(err.statusText).toBe('Conflict');
					expect(err.error).toBe(errorMessage);
				},
			});

			const req = httpMock.expectOne('https://localhost:8443/api/auth/register');
			expect(req.request.method).toBe('POST');
			expect(req.request.body).toEqual(registerRequest);
			req.flush(errorMessage, { status: 409, statusText: 'Conflict' });
		});
	});
});