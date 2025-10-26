import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from '../../app/components/login/login.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/models/auth/auth-response.model';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UserRole } from '../../app/models/auth/user-role.model';

describe("Login Component Integration Test", () => {
    let fixture: ComponentFixture<LoginComponent>;
    let loginComponent: LoginComponent;
    let httpMock: HttpTestingController;
    let routerSpy: jasmine.SpyObj<Router>;

    const apiUrl = "https://localhost:8443/api/auth";
    const loginUrl = `${apiUrl}/login`;
    const meUrl = `${apiUrl}/me`;
    const defaultGuestUser: UserRole = { username: '', roles: ['GUEST'] };

    beforeEach(() => {
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                FormBuilder,
                AuthService,
                { provide: Router, useValue: routerSpy },
                provideHttpClient(withFetch()),
                provideHttpClientTesting(),
            ],
        });

        httpMock = TestBed.inject(HttpTestingController);

        fixture = TestBed.createComponent(LoginComponent);
        loginComponent = fixture.componentInstance;

        const initReq = httpMock.expectOne(meUrl);
        expect(initReq.request.method).toBe('GET');
        initReq.flush(defaultGuestUser);

        fixture.detectChanges();
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should submit valid form and navigate on success", () => {
        loginComponent.loginForm.setValue({ username: "testUser", password: "test" });
        loginComponent.login();

        const loginReq = httpMock.expectOne(loginUrl);
        expect(loginReq.request.method).toBe("POST");
        expect(loginReq.request.withCredentials).toBeTrue();
        expect(loginReq.request.body).toEqual(loginComponent.loginForm.value);

        const mockLoginResponse: AuthResponse = {
            status: "SUCCESS",
            message: "Login successful",
        };
        loginReq.flush(mockLoginResponse);

        const meReq = httpMock.expectOne(meUrl);
        expect(meReq.request.method).toBe('GET');

        const mockUserRole: UserRole = { username: 'testUser', roles: ['USER'] };
        meReq.flush(mockUserRole);

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