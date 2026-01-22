import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Support } from '../../../../app/components/support/support.component';
import { UserService } from '../../../../app/services/user.service';
import { SupportService } from '../../../../app/services/support.service';
import { of, throwError } from 'rxjs';
import { FormBuilder } from '@angular/forms';

describe('Support Component Tests', () => {
    let component: Support;
    let fixture: ComponentFixture<Support>;

    let userServiceSpy: jasmine.SpyObj<UserService>;
    let supportServiceSpy: jasmine.SpyObj<SupportService>;

    beforeEach(() => {
        const userServiceMock = jasmine.createSpyObj('UserService', ['getUserProfile']);
        const supportServiceMock = jasmine.createSpyObj('SupportService', [
            'createTicket',
            'updateCurrentOpenTickets'
        ]);

        TestBed.configureTestingModule({
            imports: [Support],
            providers: [
                FormBuilder,
                { provide: UserService, useValue: userServiceMock },
                { provide: SupportService, useValue: supportServiceMock }
            ]
        });

        fixture = TestBed.createComponent(Support);
        component = fixture.componentInstance;

        userServiceSpy = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
        supportServiceSpy = TestBed.inject(SupportService) as jasmine.SpyObj<SupportService>;

        userServiceSpy.getUserProfile.and.returnValue(
            of({ email: 'test@test.com', picture: null, enableNotifications: false })
        );
    });

    it('should load user email on init and set it in the form', () => {
        fixture.detectChanges();

        expect(component.userEmail).toBe('test@test.com');
        expect(component.supportForm.get('email')?.value).toBe('test@test.com');
    });

    it('should emit closeModal after animation delay', fakeAsync(() => {
        spyOn(component.closeModal, 'emit');

        component.handleCancel();
        expect(component.isClosing).toBeTrue();

        tick(300);

        expect(component.closeModal.emit).toHaveBeenCalled();
        expect(component.isClosing).toBeFalse();
    }));

    it('should return form controls via getters', () => {
        expect(component.getEmailFromForm()).toBe(component.supportForm.get('email'));
        expect(component.getSubjectFromForm()).toBe(component.supportForm.get('subject'));
        expect(component.getBodyFromForm()).toBe(component.supportForm.get('body'));
    });

    it('should build a CreateTicketRequest object', () => {
        component.supportForm.setValue({
            email: 'user@test.com',
            subject: 'Help',
            body: 'Something happened'
        });

        const request = (component as any)['buildRequest']();

        expect(request).toEqual({
            email: 'user@test.com',
            subject: 'Help',
            body: 'Something happened'
        });
    });

    it('should NOT call createTicket if form is invalid', () => {
        component.supportForm.setValue({
            email: '',
            subject: '',
            body: ''
        });

        component.createTicket();

        expect(supportServiceSpy.createTicket).not.toHaveBeenCalled();
    });

    it('should create ticket successfully', () => {
        component.supportForm.setValue({
            email: 'user@test.com',
            subject: 'Issue',
            body: 'Details'
        });

        supportServiceSpy.createTicket.and.returnValue(of({ status: 'SUCCESS', message: 'OK' }));

        component.createTicket();

        expect(supportServiceSpy.createTicket).toHaveBeenCalled();
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Message sent successfully');
        expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalled();
    });

    it('should set error when createTicket fails', () => {
        component.supportForm.setValue({
            email: 'user@test.com',
            subject: 'Issue',
            body: 'Details'
        });

        supportServiceSpy.createTicket.and.returnValue(throwError(() => new Error()));

        component.createTicket();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('Something wrong happened. Please, try again');
    });
});