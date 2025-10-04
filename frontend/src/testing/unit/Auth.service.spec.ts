import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { provideHttpClientTesting, HttpTestingController } from "@angular/common/http/testing";
import { AuthService } from "../../app/Services/Auth.service";
import { LoginRequest } from "../../app/Models/LoginRequest";
import { RegisterRequest } from "../../app/Models/RegisterRequest";
import { AuthResponse } from "../../app/Models/AuthResponse";

describe("AuthServiceTests", () => {
    let authService: AuthService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                AuthService,
                provideHttpClient(withFetch()),
                provideHttpClientTesting()
            ]
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
    const expectPostHelper = (url: string, body: any, response: any, withCredentials = false) => {
        const req = httpMock.expectOne(url);
        expect(req.request.method).toBe("POST");
        expect(req.request.body).toEqual(body);
        if (withCredentials) {
			expect(req.request.withCredentials).toBeTrue();
		}
        req.flush(response);
    };

	// Login Tests
    describe("loginUser", () => {
        let loginUrl: string;

		beforeEach(() => {
			loginUrl = `${authService['apiUrl']}/login`;
		});

        it("should login the user successfully", () => {
            const mockRequest: LoginRequest = { username: "testUser", password: "test" };

            const mockResponse: AuthResponse = {
                status: "SUCCESS",
                message: "Auth successful. Tokens are created in cookie."
            };

            authService.loginUser(mockRequest).subscribe(response => {
                expect(response).toEqual(mockResponse);
            });

            expectPostHelper(loginUrl, mockRequest, mockResponse, true);
        });

        it("should handle login failure with a 401 status code", () => {
            const mockRequest: LoginRequest = { username: "wrongUser", password: "wrongPassword" };
            const errorText = "Invalid credentials";

            authService.loginUser(mockRequest).subscribe({
                next: () => fail("Expected error, but got success"),
                error: err => {
                    expect(err.status).toBe(401);
                    expect(err.statusText).toBe("Unauthorized");
                    expect(err.error).toBe(errorText);
                }
            });

            const req = httpMock.expectOne(loginUrl);
            expect(req.request.method).toBe("POST");
            req.flush(errorText, { status: 401, statusText: "Unauthorized" });
        });
    });

	// Register Tests
    describe("registerUser", () => {
        let registerUrl: string;

		beforeEach(() => {
			registerUrl = `${authService['apiUrl']}/register`;
		});

        it("should register the user successfully", () => {
            const mockRequest: RegisterRequest = {
                email: "test@gmail.com", username: "testUser", password: "test"
            };

            const mockResponse: AuthResponse = {
                status: "SUCCESS",
                message: "User registered successfully"
            };

            authService.registerUser(mockRequest).subscribe(response => {
                expect(response).toEqual(mockResponse);
            });

            expectPostHelper(registerUrl, mockRequest, mockResponse);
        });

        it("should handle register failure with a 409 conflict status code", () => {
            const mockRequest: RegisterRequest = {
                email: "test@gmail.com", username: "testUser", password: "test"
            };

            const errorText = "User already exists";

            authService.registerUser(mockRequest).subscribe({
                next: () => fail("Expected error, but got success"),
                error: err => {
                    expect(err.status).toBe(409);
                    expect(err.statusText).toBe("Conflict");
                    expect(err.error).toBe(errorText);
                }
            });

            const req = httpMock.expectOne(registerUrl);
            expect(req.request.method).toBe("POST");
            req.flush(errorText, { status: 409, statusText: "Conflict" });
        });
    });
});