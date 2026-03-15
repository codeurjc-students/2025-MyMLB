import { RegisterComponent } from '../../../app/components/register/register.component';
import { AuthService } from '../../../app/services/auth.service';
import { AuthResponse } from '../../../app/models/auth.model';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { StatsService } from '../../../app/services/stats.service';

describe('Register Component Tests', () => {
	let fixture: ComponentFixture<RegisterComponent>;
	let registerComponent: RegisterComponent;
	let authServiceSpy: jasmine.SpyObj<AuthService>;
	let statsServiceSpy: jasmine.SpyObj<StatsService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		authServiceSpy = jasmine.createSpyObj('AuthService', ['registerUser']);
		statsServiceSpy = jasmine.createSpyObj('StatsService', ['updateNewUsers']);
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			providers: [
				FormBuilder,
				{ provide: AuthService, useValue: authServiceSpy },
				{ provide: StatsService, useValue: statsServiceSpy },
				{ provide: Router, useValue: routerSpy },
			],
		});

		fixture = TestBed.createComponent(RegisterComponent);
		registerComponent = fixture.componentInstance;

		const response: AuthResponse = {
			status: 'SUCCESS',
			message: 'Successfully updated the new users'
		}
		statsServiceSpy.updateNewUsers.and.returnValue(of(response));
		fixture.detectChanges();
	});

	it('should not submit if form is invalid', () => {
		registerComponent.registerForm.setValue({ email: '', username: '', password: '' });
		registerComponent.register();
		expect(authServiceSpy.registerUser).not.toHaveBeenCalled();
	});

	it('should show a success message on successful registration', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'User registered successfully',
		};

		authServiceSpy.registerUser.and.returnValue(of(mockResponse));

		registerComponent.registerForm.setValue({
			email: 'test@example.com',
			username: 'newUser',
			password: 'securePass',
		});

		registerComponent.register();

		expect(registerComponent.showSuccess).toBeTrue();
		expect(registerComponent.successMessage).toBe(mockResponse.message);
		expect(statsServiceSpy.updateNewUsers).toHaveBeenCalled();
		expect(registerComponent.errorMessage).toBe('');
	});

	it('should show error message on failed registration', () => {
		authServiceSpy.registerUser.and.returnValue(
			throwError(() => new Error('User already exists'))
		);

		registerComponent.registerForm.setValue({
			email: 'test@example.com',
			username: 'existingUser',
			password: 'securePass',
		});

		registerComponent.register();

		expect(registerComponent.showSuccess).toBeFalse();
		expect(registerComponent.successMessage).toBe('');
		expect(registerComponent.errorMessage).toBe('User already exists');
	});

	it('should emit toggleForm event', () => {
		spyOn(registerComponent.toggleForm, 'emit');
		registerComponent.toggleToLoginForm();
		expect(registerComponent.toggleForm.emit).toHaveBeenCalled();
	});
});
