import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from '../../app/Components/Login/Login.component';
import { FormBuilder } from '@angular/forms';
import { AuthService } from '../../app/Services/Auth.service';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthResponse } from '../../app/Models/AuthResponse';

describe('LoginComponent Unit', () => {
	let fixture: ComponentFixture<LoginComponent>;
	let loginComponent: LoginComponent;
	let authServiceSpy: jasmine.SpyObj<AuthService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		authServiceSpy = jasmine.createSpyObj('AuthService', ['loginUser']);
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			providers: [
				FormBuilder,
				{ provide: AuthService, useValue: authServiceSpy },
				{ provide: Router, useValue: routerSpy },
			],
		});

		fixture = TestBed.createComponent(LoginComponent);
		loginComponent = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should not submit if form is invalid', () => {
		loginComponent.loginForm.setValue({ username: '', password: '' });
		loginComponent.login();
		expect(authServiceSpy.loginUser).not.toHaveBeenCalled();
	});

	it('should navigate on successful login', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Login successful',
		};

		authServiceSpy.loginUser.and.returnValue(of(mockResponse));

		loginComponent.loginForm.setValue({ username: 'user', password: 'pass' });
		loginComponent.login();

		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
		expect(loginComponent.errorMessage).toBe('');
	});

	it('should show error on failed login', () => {
		authServiceSpy.loginUser.and.returnValue(
			throwError(() => new Error('Invalid credentials'))
		);

		loginComponent.loginForm.setValue({ username: 'user', password: 'wrong' });
		loginComponent.login();

		expect(loginComponent.errorMessage).toBe('Invalid credentials');
		expect(routerSpy.navigate).not.toHaveBeenCalled();
	});

	it('should emit toggleForm event', () => {
		spyOn(loginComponent.toggleForm, 'emit');
		loginComponent.toggleToRegisterForm();
		expect(loginComponent.toggleForm.emit).toHaveBeenCalled();
	});
});