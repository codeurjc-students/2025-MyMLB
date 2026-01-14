import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from '../../../app/components/profile/profile.component';
import { AuthService } from '../../../app/services/auth.service';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { UserRole } from '../../../app/models/auth/user-role.model';
import { UserService } from '../../../app/services/user.service';

describe('Profile Component Tests', () => {
	let component: ProfileComponent;
	let fixture: ComponentFixture<ProfileComponent>;
	let authServiceSpy: jasmine.SpyObj<AuthService>;
	let userServiceSpy: jasmine.SpyObj<UserService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		const authServiceMock = jasmine.createSpyObj('AuthService', [
			'logoutUser',
			'getActiveUser',
			'deleteAccount'
		]);
		const userServiceMock = jasmine.createSpyObj('UserService', [
			'editProfile'
		]);
		const routerMock = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [ProfileComponent],
			providers: [
				{ provide: UserService, useValue: userServiceMock },
				{ provide: AuthService, useValue: authServiceMock },
				{ provide: Router, useValue: routerMock }
			],
		});

		fixture = TestBed.createComponent(ProfileComponent);
		component = fixture.componentInstance;

		authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
		routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
	});

	it('should retrieve the active user on init', () => {
		const mockResponse: UserRole = {
			username: 'testUser',
			roles: ['GUEST', 'USER'],
			email: 'test@gmail.com',
			password: '123'
		};

		authServiceSpy.getActiveUser.and.returnValue(of(mockResponse));

		fixture.detectChanges();

		expect(component.username).toBe('testUser');
		expect(component.errorMessage).toBe('');
	});

	it('should show an error message if getActiveUser fails', () => {
		authServiceSpy.getActiveUser.and.returnValue(throwError(() => new Error('Backend Error')));

		fixture.detectChanges();

		expect(component.errorMessage).toBe('Unexpected error while retrieving the user');
	});

	it('should successfully logout the user when confirm() is called with activeAction = logout', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Logout Successful',
		};

		component.activeAction = 'logout';
		authServiceSpy.logoutUser.and.returnValue(of(mockResponse));

		component.confirm();

		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
		expect(component.activeAction).toBeNull();
	});

	it('should successfully delete the account when confirm() is called with activeAction = delete', () => {
		authServiceSpy.deleteAccount.and.returnValue(of({ status: 'SUCCESS', message: 'Deleted' }));

		component.activeAction = 'delete';
		component.confirm();

		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
		expect(component.activeAction).toBeNull();
	});

	it('should open modal with correct action', () => {
		component.openModal('logout');
		expect(component.activeAction).toBe('logout');

		component.openModal('delete');
		expect(component.activeAction).toBe('delete');
	});

	it('should cancel modal and reset activeAction', () => {
		component.activeAction = 'logout';
		component.cancel();
		expect(component.activeAction).toBeNull();

		component.activeAction = 'delete';
		component.cancel();
		expect(component.activeAction).toBeNull();
	});
});