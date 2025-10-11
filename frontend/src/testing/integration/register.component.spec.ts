import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from '../../app/components/register/register.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe("RegisterComponent Integration Test", () => {
	let fixture: ComponentFixture<RegisterComponent>;
	let registerComponent: RegisterComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const registerUrl = "https://localhost:8443/api/auth/register";

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			providers: [
				FormBuilder,
				AuthService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting()
			],
		});

		fixture = TestBed.createComponent(RegisterComponent);
		registerComponent = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it("should submit valid form and show success message", () => {
		registerComponent.registerForm.setValue({
			email: "test@gmail.com",
			username: "testUser",
			password: "test",
		});
		registerComponent.register();

		const req = httpMock.expectOne(registerUrl);
		expect(req.request.method).toBe("POST");
		expect(req.request.body).toEqual(registerComponent.registerForm.value);

		const mockResponse: AuthResponse = {
			status: "SUCCESS",
			message: "User registered successfully",
		};

		req.flush(mockResponse);

		expect(registerComponent.showSuccess).toBeTrue();
		expect(registerComponent.successMessage).toBe(mockResponse.message);
		expect(registerComponent.errorMessage).toBe('');
	});

	it("should show error message on failed registration", () => {
		registerComponent.registerForm.setValue({
			email: "test@gmail.com",
			username: "testExistingUser",
			password: "test",
		});

		registerComponent.register();

		const req = httpMock.expectOne(registerUrl);
		req.flush({ message: "User already exists" }, { status: 409, statusText: "Conflict" });
		expect(registerComponent.showSuccess).toBeFalse();
	});
});