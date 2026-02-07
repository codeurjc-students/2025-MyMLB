import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { SupportService } from '../../../app/services/support.service';
import { CreateTicketRequest } from '../../../app/models/support/create-ticket-request.model';
import { ReplyRequest } from '../../../app/models/support/reply-request.model';
import { SupportTicket } from '../../../app/models/support/support-ticket.model';
import { SupportMessage } from '../../../app/models/support/support-message.model';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';
import { skip } from 'rxjs';

describe('Support Service Tests', () => {
    let service: SupportService;
    let httpMock: HttpTestingController;

    const userApi = 'https://localhost:8443/api/v1/support';
    const adminApi = 'https://localhost:8443/api/v1/admin/support/tickets';

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [SupportService, provideHttpClient(withFetch()), provideHttpClientTesting()],
        });

        service = TestBed.inject(SupportService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should create a new ticket', () => {
        const request: CreateTicketRequest = {
            email: 'test@test.com',
            subject: 'Issue',
            body: 'Something happened'
        };

        const mockResponse: AuthResponse = {
            status: 'SUCCESS',
            message: 'Ticket created'
        };

        service.createTicket(request).subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(userApi);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(request);
        req.flush(mockResponse);
    });

    it('should fetch open tickets', () => {
        const mockTickets: SupportTicket[] = [
            { id: 1, subject: 'A', status: 'OPEN', creationDate: new Date() },
            { id: 2, subject: 'B', status: 'OPEN', creationDate: new Date() }
        ];

        service.getOpenTickets().subscribe((tickets) => {
            expect(tickets.length).toBe(2);
            expect(tickets).toEqual(mockTickets);
        });

        const req = httpMock.expectOne(adminApi);
        expect(req.request.method).toBe('GET');
        req.flush(mockTickets);
    });

    it('should fetch conversation for a ticket', () => {
        const ticketId = 123;
        const mockMessages: SupportMessage[] = [
            {
                id: 1,
                senderEmail: 'user@test.com',
                body: 'Hello',
                fromUser: 'USER',
                creationDate: new Date()
            }
        ];

        service.getConversation(ticketId).subscribe((messages) => {
            expect(messages).toEqual(mockMessages);
        });

        const req = httpMock.expectOne(`${adminApi}/${ticketId}/conversation`);
        expect(req.request.method).toBe('GET');
        req.flush(mockMessages);
    });

    it('should send a reply to a ticket', () => {
        const ticketId = 123;
        const request: ReplyRequest = {
            adminEmail: 'admin@test.com',
            body: 'We are checking your issue'
        };

        const mockResponse: SupportMessage = {
            id: 1,
            senderEmail: 'admin@test.com',
            body: 'We are checking your issue',
            fromUser: 'ADMIN',
            creationDate: new Date()
        };

        service.reply(ticketId, request).subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${adminApi}/${ticketId}/reply`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(request);
        req.flush(mockResponse);
    });

    it('should close a ticket', () => {
        const ticketId = 123;

        const mockResponse: AuthResponse = {
            status: 'SUCCESS',
            message: 'Ticket closed'
        };

        service.closeTicket(ticketId).subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${adminApi}/${ticketId}/close`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual({});
        req.flush(mockResponse);
    });

    it('should update openTickets$ with the number of open tickets', () => {
        const mockTickets: SupportTicket[] = [
            { id: 1, subject: 'A', status: 'OPEN', creationDate: new Date() },
            { id: 2, subject: 'B', status: 'OPEN', creationDate: new Date() }
        ];

        service.opentTickets$.pipe(skip(1)).subscribe((count) => {
            expect(count).toBe(2);
        });

        service.updateCurrentOpenTickets();

        const req = httpMock.expectOne(adminApi);
        expect(req.request.method).toBe('GET');
        req.flush(mockTickets);
    });

    it('should set openTickets$ to 0 when getOpenTickets fails', () => {
        service.opentTickets$.pipe(skip(1)).subscribe((count) => {
            expect(count).toBe(0);
        });

        service.updateCurrentOpenTickets();

        const req = httpMock.expectOne(adminApi);
        req.error(new ProgressEvent('Network error'));
    });
});