import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from '../../../app/components/profile/profile.component';
import { AuthService } from '../../../app/services/auth.service';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { UserRole } from '../../../app/models/auth/user-role.model';
import { UserService } from '../../../app/services/user.service';
import { Pictures } from '../../../app/models/pictures.model';
import { User } from '../../../app/models/user.model';

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
            'editProfile',
            'getUserProfile',
            'editProfilePicture',
            'setProfilePicture',
            'deleteProfilePicture'
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
        userServiceSpy = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
        routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

        const mockUser: UserRole = { username: 'testUser', roles: ['USER'] };
        authServiceSpy.getActiveUser.and.returnValue(of(mockUser));
        userServiceSpy.getUserProfile.and.returnValue(of({ email: 'test@test.com', picture: null }));
    });

    it('should retrieve the active user on init', () => {
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
        const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Logout Successful' };
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

    it('should retrieve profile data successfully', () => {
        const mockPicture: Pictures = { publicId: 'test-123', url: 'http://test-123.png' };
        userServiceSpy.getUserProfile.and.returnValue(of({ email: 'newEmail@gmail.com', picture: mockPicture }));
        component['retrieveProfileData']();
        expect(component.currentEmail).toBe('newEmail@gmail.com');
        expect(component.pictureSrc).toEqual(mockPicture);
    });

    it('should set error when retrieveProfileData fails', () => {
        userServiceSpy.getUserProfile.and.returnValue(throwError(() => new Error()));
        component['retrieveProfileData']();
        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('Unexpected error while retrieving the profile data');
    });

    it('should update profile successfully', () => {
		const mockUserResponse: User = {
			email: 'newEmail@gmail.com',
			username: 'username'
		};
        component.emailInput = 'newEmail@gmail.com';
        userServiceSpy.editProfile.and.returnValue(of(mockUserResponse));
        component.editProfile();
        expect(component.sucess).toBeTrue();
        expect(component.currentEmail).toBe('newEmail@gmail.com');
        expect(component.emailInput).toBe('');
    });

    it('should handle profile picture upload', () => {
        const mockFile = new File([''], 'test.webp', { type: 'image/webp' });
        const mockEvent = { target: { files: [mockFile] } } as any;
        const mockPicture: Pictures = { publicId: 'test-123', url: 'http://test-123.png' };
        userServiceSpy.editProfilePicture.and.returnValue(of(mockPicture));
        component.handleProfilePicture(mockEvent);
        expect(userServiceSpy.editProfilePicture).toHaveBeenCalledWith(mockFile);
    });

    it('should not upload picture if type is not webp', () => {
        const mockFile = new File([''], 'test.png', { type: 'image/png' });
        const mockEvent = { target: { files: [mockFile], value: 'test' } } as any;
        component.handleProfilePicture(mockEvent);
        expect(component.error).toBeTrue();
        expect(mockEvent.target.value).toBe('');
    });

    it('should remove profile picture successfully', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'anyMessage'
		};
        userServiceSpy.deleteProfilePicture.and.returnValue(of(mockResponse));
        component.removeProfilePicture();
        expect(component.pictureSrc).toBeNull();
        expect(userServiceSpy.setProfilePicture).toHaveBeenCalledWith('');
        expect(component.sucess).toBeTrue();
    });

    it('should return default avatar if pictureSrc is null', () => {
        component.pictureSrc = null;
        expect(component.getPictureUrl()).toBe('assets/account-avatar.png');
    });

    it('should return picture url if pictureSrc exists', () => {
        component.pictureSrc = { publicId: 'test-123', url: 'http://test-123.png' };
        expect(component.getPictureUrl()).toBe('http://test-123.png');
    });
});