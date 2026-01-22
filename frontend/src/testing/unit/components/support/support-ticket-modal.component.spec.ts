import { AuthResponse } from './../../../../app/models/auth/auth-response.model';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SupportTicketModalComponent } from './../../../../app/components/support/support-ticket-modal/support-ticket-modal.component';
import { SupportService } from '../../../../app/services/support.service';
import { of, throwError } from 'rxjs';
import { SupportMessage } from '../../../../app/models/support/support-message.model';

describe('Support Ticket Modal Component Tests', () => {
    let component: SupportTicketModalComponent;
    let fixture: ComponentFixture<SupportTicketModalComponent>;
    let supportServiceSpy: jasmine.SpyObj<SupportService>;

    beforeEach(() => {
        const supportMock = jasmine.createSpyObj('SupportService', [
            'getConversation',
            'reply',
            'closeTicket',
            'updateCurrentOpenTickets'
        ]);

        TestBed.configureTestingModule({
            imports: [SupportTicketModalComponent],
            providers: [{ provide: SupportService, useValue: supportMock }]
        });

        fixture = TestBed.createComponent(SupportTicketModalComponent);
        component = fixture.componentInstance;

        supportServiceSpy = TestBed.inject(SupportService) as jasmine.SpyObj<SupportService>;

        component.ticketId = '123';
    });

    it('should load conversation on init', () => {
        const mockMessages = [
            {
                id: '1',
                senderEmail: 'user@test.com',
                body: 'Hello',
                fromUser: 'USER',
                creationDate: new Date()
            }
        ];

        supportServiceSpy.getConversation.and.returnValue(of(mockMessages));

        fixture.detectChanges();

        expect(component.messages).toEqual(mockMessages);
        expect(component.error).toBeFalse();
    });

    it('should set error when loadConversation fails', () => {
        supportServiceSpy.getConversation.and.returnValue(
            throwError(() => new Error('Backend error'))
        );

        component.loadConversation();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('Unexpected error ocurred loading the conversation');
    });

    it('should emit close after animation delay', fakeAsync(() => {
        spyOn(component.close, 'emit');

        component.handleCancel();
        expect(component.isClosing).toBeTrue();

        tick(300);

        expect(component.close.emit).toHaveBeenCalled();
        expect(component.isClosing).toBeFalse();
    }));


    it('should send reply successfully', () => {
		const response: SupportMessage = {
			id: '1234',
			senderEmail: 'test@gmail.com',
			body: 'body',
			fromUser: 'testUser',
			creationDate: new Date()
		}
        component.replyBody = 'Test reply';

        supportServiceSpy.reply.and.returnValue(of(response));
        supportServiceSpy.getConversation.and.returnValue(of([]));

        component.sendReply();

        expect(component.loading).toBeFalse();
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Response Sent Correctly');
        expect(component.replyBody).toBe('');
        expect(supportServiceSpy.reply).toHaveBeenCalled();
        expect(supportServiceSpy.getConversation).toHaveBeenCalled();
    });

    it('should set error when sendReply fails', () => {
        component.replyBody = 'Test reply';

        supportServiceSpy.reply.and.returnValue(
            throwError(() => new Error('Error sending reply'))
        );

        component.sendReply();

        expect(component.loading).toBeFalse();
        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('An error occurred while sending the response.');
    });

    it('should close ticket successfully', () => {
		const response: AuthResponse = {
			status: 'SUCCESS',
			message: 'Ticket Successfully Closed'
		};
        supportServiceSpy.closeTicket.and.returnValue(of(response));

        component.closeTicket();

        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Ticket Successfully Closed');
        expect(component.ticketClosed).toBeTrue();
        expect(supportServiceSpy.updateCurrentOpenTickets).toHaveBeenCalled();
    });

    it('should set error when closeTicket fails', () => {
        supportServiceSpy.closeTicket.and.returnValue(
            throwError(() => new Error('Error closing ticket'))
        );

        component.closeTicket();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('Unexpected error ocurred closing this ticket.');
    });

    it('should emit close event when closeModal is called', () => {
        spyOn(component.close, 'emit');

        component.closeModal();

        expect(component.close.emit).toHaveBeenCalled();
    });
});