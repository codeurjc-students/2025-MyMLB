import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SupportTicketModalComponent } from '../../../app/components/support/support-ticket-modal/support-ticket-modal.component';
import { SupportService } from '../../../app/services/support.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { SupportMessage } from '../../../app/models/support/support-message.model';

describe('Support Ticket Modal Component Integration Test', () => {
    let fixture: ComponentFixture<SupportTicketModalComponent>;
    let component: SupportTicketModalComponent;
    let httpMock: HttpTestingController;

    const adminApi = 'https://localhost:8443/api/v1/admin/support/tickets';

    const mockMessages: SupportMessage[] = [
        {
            id: 1,
            senderEmail: 'user@test.com',
            body: 'Hello',
            fromUser: 'USER',
            creationDate: new Date()
        }
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [SupportTicketModalComponent],
            providers: [
                SupportService,
                provideHttpClient(withFetch()),
                provideHttpClientTesting(),
            ],
        });

        fixture = TestBed.createComponent(SupportTicketModalComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        component.ticketId = 123;

        fixture.detectChanges();

        const req = httpMock.expectOne(`${adminApi}/123/conversation`);
        expect(req.request.method).toBe('GET');
        req.flush(mockMessages);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should load conversation on init', () => {
        expect(component.messages).toEqual(mockMessages);
        expect(component.error).toBeFalse();
    });

    it('should send reply successfully and reload conversation', () => {
        component.replyBody = 'Admin reply';

        component.sendReply();

        const replyReq = httpMock.expectOne(`${adminApi}/123/reply`);
        expect(replyReq.request.method).toBe('POST');
        expect(replyReq.request.body).toEqual({
            adminEmail: 'mlbportal29@gmail.com',
            body: 'Admin reply'
        });

        replyReq.flush({});

        expect(component.loading).toBeFalse();
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Response Sent Correctly');
        expect(component.replyBody).toBe('');

        const reloadReq = httpMock.expectOne(`${adminApi}/123/conversation`);
        expect(reloadReq.request.method).toBe('GET');
        reloadReq.flush(mockMessages);
    });

    it('should close ticket successfully', () => {
        spyOn(component['supportService'], 'updateCurrentOpenTickets');

        component.closeTicket();

        const req = httpMock.expectOne(`${adminApi}/123/close`);
        expect(req.request.method).toBe('POST');
        req.flush({});

        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Ticket Successfully Closed');
        expect(component.ticketClosed).toBeTrue();
        expect(component['supportService'].updateCurrentOpenTickets).toHaveBeenCalled();
    });
});