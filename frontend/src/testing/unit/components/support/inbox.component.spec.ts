import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InboxComponent } from '../../../../app/components/support/inbox/inbox.component';
import { SupportService } from '../../../../app/services/support.service';
import { of } from 'rxjs';
import { SupportTicket } from '../../../../app/models/support/support-ticket.model';

describe('Inbox Component Tests', () => {
    let component: InboxComponent;
    let fixture: ComponentFixture<InboxComponent>;
    let supportServiceSpy: jasmine.SpyObj<SupportService>;

    beforeEach(() => {
        const supportMock = jasmine.createSpyObj('SupportService', ['getOpenTickets']);

        TestBed.configureTestingModule({
            imports: [InboxComponent],
            providers: [{ provide: SupportService, useValue: supportMock }]
        });

        fixture = TestBed.createComponent(InboxComponent);
        component = fixture.componentInstance;

        supportServiceSpy = TestBed.inject(SupportService) as jasmine.SpyObj<SupportService>;
    });

    it('should load tickets on init', () => {
        const mockTickets: SupportTicket[] = [
            { id: 1, subject: 'A', status: 'OPEN', creationDate: new Date() }
        ];

        supportServiceSpy.getOpenTickets.and.returnValue(of(mockTickets));

        fixture.detectChanges();

        expect(component.tickets).toEqual(mockTickets);
    });

    it('should load tickets successfully', () => {
        const mockTickets: SupportTicket[] = [
            { id: 1, subject: 'A', status: 'OPEN', creationDate: new Date() },
            { id: 2, subject: 'B', status: 'OPEN', creationDate: new Date() }
        ];

        supportServiceSpy.getOpenTickets.and.returnValue(of(mockTickets));

        component.loadTickets();

        expect(component.tickets.length).toBe(2);
        expect(component.tickets).toEqual(mockTickets);
    });

    it('should set selectedTicketId when opening a ticket', () => {
        component.openTicket(123);

        expect(component.selectedTicketId).toBe(123);
    });

    it('should clear selectedTicketId and reload tickets when closing modal', () => {
        const mockTickets: SupportTicket[] = [
            { id: 1, subject: 'A', status: 'OPEN', creationDate: new Date() }
        ];

        supportServiceSpy.getOpenTickets.and.returnValue(of(mockTickets));

        component.selectedTicketId = 123;

        component.closeModal();

        expect(component.selectedTicketId).toBeNull();
        expect(component.tickets).toEqual(mockTickets);
        expect(supportServiceSpy.getOpenTickets).toHaveBeenCalled();
    });
});