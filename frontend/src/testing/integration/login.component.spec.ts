import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from '../../app/components/login/login.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/Models/auth/auth-response.model';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe("LoginComponent Integration Test", () => {
	let fixture: ComponentFixture<LoginComponent>;
	let loginComponent: LoginComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const loginUrl = "https://localhost:8443/api/auth/login";

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			providers: [
				FormBuilder,
				AuthService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		fixture = TestBed.createComponent(LoginComponent);
		loginComponent = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it("should submit valid form and navigate on success", () => {
		loginComponent.loginForm.setValue({ username: "testUser", password: "test" });
		loginComponent.login();

		const req = httpMock.expectOne(loginUrl);
		expect(req.request.method).toBe("POST");
		expect(req.request.withCredentials).toBeTrue();
		expect(req.request.body).toEqual(loginComponent.loginForm.value);

		const mockResponse: AuthResponse = {
			status: "SUCCESS",
			message: "Login successful",
		};
		req.flush(mockResponse);

		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
		expect(loginComponent.errorMessage).toBe('');
	});

	it("should show error message on failed login", () => {
		loginComponent.loginForm.setValue({ username: "testUser", password: "test" });
		loginComponent.login();

		const req = httpMock.expectOne(loginUrl);
		req.flush("Invalid credentials", { status: 401, statusText: "Unauthorized" });
		expect(routerSpy.navigate).not.toHaveBeenCalled();
	});
});