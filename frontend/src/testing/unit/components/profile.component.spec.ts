import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ProfileComponent } from "../../../app/components/profile/profile.component";
import { AuthService } from "../../../app/services/auth.service";
import { AuthResponse } from "../../../app/models/auth/auth-response.model";
import { of, throwError } from "rxjs";
import { Router } from "@angular/router";
import { UserRole } from "../../../app/models/auth/user-role.model";

describe('Profile Component Tests', () => {
	let component: ProfileComponent;
	let fixture: ComponentFixture<ProfileComponent>;
	let authServiceSpy: jasmine.SpyObj<AuthService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		const authServiceMock = jasmine.createSpyObj('AuthService', ['logoutUser', 'getActiveUser']);
		const routerMock = jasmine.createSpyObj('Router', ['navigate']);
		TestBed.configureTestingModule({
			providers: [
				{ provide: AuthService, useValue: authServiceMock },
				{ provide: Router, useValue: routerMock }
			]
		});

		fixture = TestBed.createComponent(ProfileComponent);
		component = fixture.componentInstance;
		authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
		routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
	});

	it('should retrieve the actives user on init', () => {
		const mockResponse: UserRole = {
			username: 'testUser',
			roles: ['GUEST', 'USER']
		}
		authServiceSpy.getActiveUser.and.returnValue(of(mockResponse));
		fixture.detectChanges();

		expect(mockResponse.username).toBe('testUser');
		expect(mockResponse.roles).toContain('USER');
		expect(component.errorMessage).toBe('');
	});

	it('should show an error message if any error occurs while retrieving the active user', () => {
		authServiceSpy.getActiveUser.and.returnValue(throwError(() => new Error('Backend Error')));
		fixture.detectChanges();

		expect(component.errorMessage).toBe('Unexpected error while retrieving the user');
	});

	it('should succesfully logout the user', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Logout Successful'
		}
		authServiceSpy.logoutUser.and.returnValue(of(mockResponse));
		component.confirmLogout();

		expect(component.errorMessage).toBe('');
		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
	});
});