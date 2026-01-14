import { UserRole } from './../../../app/models/auth/user-role.model';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from '../../../app/services/auth.service';
import { LoginRequest } from './../../../app/models/auth/login-request.model';
import { RegisterRequest } from '../../../app/models/auth/register-request.model';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';
import { ForgotPasswordRequest } from '../../../app/models/auth/forgot-password.model';
import { ResetPasswordRequest } from '../../../app/models/auth/reset-password-request.model';
import { first, skip } from 'rxjs';

describe('Auth Service Tests', () => {
    let authService: AuthService;
    let httpMock: HttpTestingController;

    const defaultGuestUser: UserRole = { username: '', roles: ['GUEST'], email: '', password: '' };

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [AuthService, provideHttpClient(withFetch()), provideHttpClientTesting()],
        });
        authService = TestBed.inject(AuthService);
        httpMock = TestBed.inject(HttpTestingController);

        const initReq = httpMock.expectOne(`${authService['apiUrl']}/me`);
        expect(initReq.request.method).toBe('GET');
        initReq.flush(defaultGuestUser);
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
    const expectPostHelper = (url: string, body: any, response: any, withCredentials = false) => {
        const req = httpMock.expectOne(url);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(body);
        if (withCredentials) {
            expect(req.request.withCredentials).toBeTrue();
        }
        req.flush(response);
    };

    // Active User Test
    describe('Get Active User', () => {
        let activeUserUrl: string;

        beforeEach(() => {
            activeUserUrl = `${authService['apiUrl']}/me`;
        });

        it('should successfully return the active user', () => {
            const mockUserRole: UserRole = {
                username: 'testUser',
                roles: ['USER'],
				email: 'test@gamil.com',
				password: '123'
            };

            authService.getActiveUser().subscribe((response) => {
                expect(response).toEqual(mockUserRole);
                expect(response.username).toEqual(mockUserRole.username);
                expect(response.roles).toContain('USER');
            });

            const req = httpMock.expectOne(activeUserUrl);
            expect(req.request.method).toBe('GET');
            req.flush(mockUserRole);
        });

        it('should emit GUEST user if getActiveUser fails on explicit call', () => {
            const activeUserUrl = `${authService['apiUrl']}/me`;
            const errorText = 'Unauthorized';

            authService.getActiveUser().subscribe({
                next: () => fail('Expected error'),
                error: (err) => {
                    expect(err.status).toBe(401);
                    authService.currentUser$.pipe(first()).subscribe((user) => {
                        expect(user).toEqual(defaultGuestUser);
                    });
                },
            });

            const req = httpMock.expectOne(activeUserUrl);
            req.flush(errorText, { status: 401, statusText: 'Unauthorized' });
        });
    });

    // Login Tests
    describe('Login User', () => {
        let loginUrl: string;
        const mockUserRole: UserRole = { username: 'testUser', roles: ['USER'], email: 'test@gmail.com', password: '123' };
        const mockLoginRequest: LoginRequest = { username: 'testUser', password: 'test' };
        const mockLoginResponse: AuthResponse = { status: 'SUCCESS', message: 'Auth successful. Tokens are created in cookie.' };

        beforeEach(() => {
            loginUrl = `${authService['apiUrl']}/login`;
        });

        it('should login the user successfully and update the current user status', () => {
            authService.loginUser(mockLoginRequest).subscribe((response) => {
                expect(response).toEqual(mockLoginResponse);
            });

            const loginReq = httpMock.expectOne(loginUrl);
            expect(loginReq.request.method).toBe('POST');
            expect(loginReq.request.body).toEqual(mockLoginRequest);
            expect(loginReq.request.withCredentials).toBeTrue();
            loginReq.flush(mockLoginResponse);

            const meReq = httpMock.expectOne(`${authService['apiUrl']}/me`);
            expect(meReq.request.method).toBe('GET');
            meReq.flush(mockUserRole);

            authService.currentUser$.pipe(first()).subscribe((user) => {
                expect(user).toEqual(mockUserRole);
            });
        });

        it('should handle login failure with a 401 status code and keep GUEST status', () => {
            const mockRequest: LoginRequest = { username: 'wrongUser', password: 'wrongPassword' };
            const errorText = 'Invalid credentials';

            authService.loginUser(mockRequest).subscribe({
                next: () => fail('Expected error, but got success'),
                error: (err) => {
                    expect(err.status).toBe(401);

                    authService.currentUser$.pipe(first()).subscribe((user) => {
                        expect(user).toEqual(defaultGuestUser);
                    });
                },
            });
            const req = httpMock.expectOne(loginUrl);
            expect(req.request.method).toBe('POST');
            req.flush(errorText, { status: 401, statusText: 'Unauthorized' });
        });
    });

    // Logout Tests
    describe('Logout User', () => {
        let logoutUrl: string;
        const mockLogoutResponse: AuthResponse = { status: 'SUCCESS', message: 'Logout successful' };

        beforeEach(() => {
            logoutUrl = `${authService['apiUrl']}/logout`;
            authService['currentUserSubject'].next({ username: 'testUser', roles: ['USER'], email: 'test@gmail.com', password: '123' });
        });

        it('should successfully logout the user and update status to GUEST', () => {
            authService.currentUser$.pipe(skip(1), first()).subscribe((user) => {
                expect(user).toEqual(defaultGuestUser);
            });

            authService.logoutUser().subscribe((response) => {
                expect(response).toEqual(mockLogoutResponse);
            });

            expectPostHelper(logoutUrl, {}, mockLogoutResponse, true);
        });
    });

    describe('Register User', () => {
        let registerUrl: string;

        beforeEach(() => {
            registerUrl = `${authService['apiUrl']}/register`;
        });

        it('should register the user successfully', () => {
            const mockRequest: RegisterRequest = {
                email: 'test@gmail.com',
                username: 'testUser',
                password: 'test',
            };

            const mockResponse: AuthResponse = {
                status: 'SUCCESS',
                message: 'User registered successfully',
            };

            authService.registerUser(mockRequest).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            expectPostHelper(registerUrl, mockRequest, mockResponse);
        });

        it('should handle register failure with a 409 conflict status code', () => {
            const mockRequest: RegisterRequest = {
                email: 'test@gmail.com',
                username: 'testUser',
                password: 'test',
            };

            const errorText = 'User already exists';

            authService.registerUser(mockRequest).subscribe({
                next: () => fail('Expected error, but got success'),
                error: (err) => {
                    expect(err.status).toBe(409);
                    expect(err.statusText).toBe('Conflict');
                    expect(err.error).toBe(errorText);
                },
            });

            const req = httpMock.expectOne(registerUrl);
            expect(req.request.method).toBe('POST');
            req.flush(errorText, { status: 409, statusText: 'Conflict' });
        });
    });

    describe('Password Recovery', () => {
        let forgotPasswordUrl: string;
        let resetPasswordUrl: string;

        beforeEach(() => {
            forgotPasswordUrl = `${authService['apiUrl']}/forgot-password`;
            resetPasswordUrl = `${authService['apiUrl']}/reset-password`;
        });

        it('should send the email if the email is valid', () => {
            const request: ForgotPasswordRequest = { email: 'test@gmail.com' };
            const mockResponse: AuthResponse = {
                status: 'SUCCESS',
                message: 'Recovery email sent successfully',
            };

            authService.forgotPassword(request).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            expectPostHelper(forgotPasswordUrl, request, mockResponse);
        });

        it('should handle the 404 error if the email is not registered in the backend', () => {
            const request: ForgotPasswordRequest = { email: 'badEmail@example.com' };
            const errorMessage = 'Resource Not Found';

            authService.forgotPassword(request).subscribe({
                next: () => fail('Expected error, but got success'),
                error: (err) => {
                    expect(err.status).toBe(404);
                    expect(err.statusText).toBe('Not Found');
                    expect(err.error).toBe(errorMessage);
                },
            });

            const req = httpMock.expectOne(forgotPasswordUrl);
            expect(req.request.method).toBe('POST');
            req.flush(errorMessage, { status: 404, statusText: 'Not Found' });
        });

        it('should reset the user password if the code is valid', () => {
            const request: ResetPasswordRequest = {
                code: '1234',
                newPassword: 'newPassword',
            };
            const mockResponse: AuthResponse = {
                status: 'SUCCESS',
                message: 'Password restored',
            };

            authService.resetPassword(request).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            expectPostHelper(resetPasswordUrl, request, mockResponse);
        });

        it('should thrown an error if the code is invalid', () => {
            const request: ResetPasswordRequest = {
                code: 'abcd',
                newPassword: 'anyPassword',
            };
            const errorMessage = 'Invalid or expired code';

            authService.resetPassword(request).subscribe({
                next: () => fail('Expected error, but got success'),
                error: (err) => {
                    expect(err.status).toBe(400);
                    expect(err.statusText).toBe(
                        'Invalid request. Please check the submitted data.'
                    );
                    expect(err.error).toBe(errorMessage);
                },
            });

            const req = httpMock.expectOne(resetPasswordUrl);
            expect(req.request.method).toBe('POST');
            req.flush(errorMessage, {
                status: 400,
                statusText: 'Invalid request. Please check the submitted data.',
            });
        });
    });

	describe('Refresh', () => {
		it(`it should refresh the user's session successfully`, () => {
			const mockResponse: AuthResponse = {
				status: 'SUCCESS',
				message: 'Auth successful. Tokens are created in cookie.'
			};
			authService.silentRefresh().subscribe((response) => {
				expect(response.status).toBe('SUCCESS');
			});

			const req = httpMock.expectOne(`${authService['apiUrl']}/refresh`);
			expect(req.request.method).toBe('POST');
			expect(req.request.withCredentials).toBeTrue();
			req.flush(mockResponse);
		});
	});
});