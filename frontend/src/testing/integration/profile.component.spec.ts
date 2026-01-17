import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from '../../app/components/profile/profile.component';
import { AuthService } from '../../app/services/auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserRole } from '../../app/models/auth/user-role.model';
import { Router } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { UserService } from '../../app/services/user.service';
import { Pictures } from '../../app/models/pictures.model';

describe('Profile Component Integration Test', () => {
    let fixture: ComponentFixture<ProfileComponent>;
    let component: ProfileComponent;
    let httpMock: HttpTestingController;
    let routerSpy: jasmine.SpyObj<Router>;

    const authUrl = 'https://localhost:8443/api/v1/auth';
    const usersUrl = 'https://localhost:8443/api/v1/users';

    const mockUser: UserRole = {
        username: 'testUser',
        roles: ['USER']
    };

    const mockPicture: Pictures = {
        url: 'https://test.com/photo.webp',
        publicId: 'v12345'
    };

    const mockProfileResponse = {
        email: 'test@example.com',
        picture: mockPicture
    };

    beforeEach(() => {
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        TestBed.configureTestingModule({
            imports: [ProfileComponent],
            providers: [
                AuthService,
                UserService,
                { provide: Router, useValue: routerSpy },
                provideHttpClient(withFetch()),
                provideHttpClientTesting(),
            ],
        });

        fixture = TestBed.createComponent(ProfileComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        fixture.detectChanges();
        const meReqs = httpMock.match(`${authUrl}/me`);
        meReqs.forEach(req => req.flush(mockUser));

        const profileReqs = httpMock.match(`${usersUrl}/profile`);
        profileReqs.forEach(req => req.flush(mockProfileResponse));
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should load active user and profile data on init', () => {
        expect(component.username).toBe('testUser');
        expect(component.currentEmail).toBe('test@example.com');
        expect(component.pictureSrc).toEqual(mockPicture);
    });

    it('should trigger logout and navigate to root', () => {
        component.activeAction = 'logout';
        component.confirm();

        const logoutReq = httpMock.expectOne(`${authUrl}/logout`);
        expect(logoutReq.request.method).toBe('POST');
        logoutReq.flush({ status: 'SUCCESS', message: 'Logout Successful' });

        expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should trigger account deletion and navigate to root', () => {
        component.activeAction = 'delete';
        component.confirm();

        const deleteReq = httpMock.expectOne(authUrl);
        expect(deleteReq.request.method).toBe('DELETE');
        deleteReq.flush({ status: 'SUCCESS', message: 'Account deleted' });

        expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should update profile information successfully', () => {
        component.emailInput = 'new@test.com';
        component.editProfile();

        const req = httpMock.expectOne(usersUrl);
        expect(req.request.method).toBe('PATCH');
        req.flush({ username: 'testUser', email: 'new@test.com', picture: mockPicture });

        expect(component.sucess).toBeTrue();
        expect(component.currentEmail).toBe('new@test.com');
    });

    it('should upload profile picture successfully', () => {
        const file = new File([''], 'test.webp', { type: 'image/webp' });
        const event = { target: { files: [file] } } as any;

        component.handleProfilePicture(event);

        const req = httpMock.expectOne(`${usersUrl}/picture`);
        expect(req.request.method).toBe('POST');

        const newPic = { url: 'new-url.webp', publicId: 'new-id' };
        req.flush(newPic);

        expect(component.pictureSrc).toEqual(newPic);
        expect(component.sucess).toBeTrue();
    });

    it('should delete the profile picture', () => {
        component.removeProfilePicture();

        const req = httpMock.expectOne(`${usersUrl}/picture`);
        expect(req.request.method).toBe('DELETE');
        req.flush({ status: 'SUCCESS', message: 'Deleted' });

        expect(component.pictureSrc).toBeNull();
    });
});