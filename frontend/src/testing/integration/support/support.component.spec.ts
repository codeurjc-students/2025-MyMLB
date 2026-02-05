import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Support } from '../../../app/components/support/support.component';
import { UserService } from '../../../app/services/user.service';
import { SupportService } from '../../../app/services/support.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Support Component Integration Test', () => {
    let fixture: ComponentFixture<Support>;
    let component: Support;
    let httpMock: HttpTestingController;

    const userUrl = 'https://localhost:8443/api/v1/users/profile';
    const supportUrl = 'https://localhost:8443/api/v1/support';

    const mockProfileResponse = {
        email: 'test@example.com',
        picture: null,
        enableNotifications: false
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [Support],
            providers: [
                UserService,
                SupportService,
                provideHttpClient(withFetch()),
                provideHttpClientTesting(),
            ],
        });

        fixture = TestBed.createComponent(Support);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        fixture.detectChanges();

        const profileReq = httpMock.expectOne(userUrl);
        expect(profileReq.request.method).toBe('GET');
        profileReq.flush(mockProfileResponse);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should create ticket successfully and update open tickets', () => {
        component.supportForm.setValue({
            email: 'test@example.com',
            subject: 'Test Subject',
            body: 'Test Body'
        });

        component.createTicket();

        const req = httpMock.expectOne(supportUrl);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual({
            email: 'test@example.com',
            subject: 'Test Subject',
            body: 'Test Body'
        });

        req.flush({ status: 'SUCCESS', message: 'Created' });

        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Message sent successfully');
    });
});