import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { TicketPurchaseComponent } from '../../../../app/components/ticket/ticket-purchase/ticket-purchase.component';
import { TicketService } from '../../../../app/services/ticket/ticket.service';
import { MockFactory } from '../../../utils/mock-factory';

describe('Ticket Purchase Component Tests', () => {
    let component: TicketPurchaseComponent;
    let fixture: ComponentFixture<TicketPurchaseComponent>;
    let ticketServiceSpy: jasmine.SpyObj<TicketService>;

	const mockSector = MockFactory.buildEventManager(10, 1, 'North Sector', 50, 100, 100);

    const mockEvent = MockFactory.buildEventResponse(
        1, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), { url: 'stadium.jpg', publicId: '1' }, [mockSector]
    );

    const mockSeat = MockFactory.buildMockSeat(1, 'S-1');
    const mockSeatsResponse = MockFactory.buildPaginatedResponse(mockSeat);

    beforeEach(() => {
        ticketServiceSpy = jasmine.createSpyObj('TicketService', ['purchaseTicket']);

        TestBed.configureTestingModule({
            imports: [TicketPurchaseComponent, ReactiveFormsModule],
            providers: [
                { provide: TicketService, useValue: ticketServiceSpy }
            ]
        });

        fixture = TestBed.createComponent(TicketPurchaseComponent);
        component = fixture.componentInstance;

        component.event = mockEvent as any;
        component.eventManagerId = 10;
        component.tickets = 2;
        component.seats = mockSeatsResponse.content;
        component.totalPrice = 100;

        fixture.detectChanges();
    });

    it('should initialize the form', () => {
        const form = component.paymentForm;
        expect(form).toBeDefined();
        expect(form.get('ownerName')?.value).toBe('');
        expect(form.valid).toBeFalse();
    });

    it('should call purchaseTicket and show success modal', () => {
		const mockTicket = MockFactory.buildTicketMock(1, 'Boston Red Sox', 'New York Yankees', 'Yankee Stadium', 100, new Date(), 'New Sector', 'S-1');
		const mockResponse = MockFactory.buildPaginatedResponse(mockTicket);

        component.paymentForm.patchValue({
            ownerName: 'TestUser',
            cardNumber: '4539148912345674',
            cvv: '123',
            expirationDate: '2025-12-31'
        });

        ticketServiceSpy.purchaseTicket.and.returnValue(of(mockResponse));

        component.purchase();

        expect(ticketServiceSpy.purchaseTicket).toHaveBeenCalled();
        expect(component.showSuccesModal).toBeTrue();
        expect(component.successMessage).toBe('Thank you for your purchase!');
    });

    it('should handle error during purchase', () => {
         component.paymentForm.patchValue({
            ownerName: 'TestUser',
            cardNumber: '4539148912345674',
            cvv: '123',
            expirationDate: '2025-12-31'
        });

		const mockErrorResponse = {
			error: { message: 'The card has expired' }
		};

        ticketServiceSpy.purchaseTicket.and.returnValue(throwError(() => mockErrorResponse));

        component.purchase();

        expect(component.loading).toBeFalse();
        expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('The card has expired');
    });

    it('should not call ticketService if form is invalid', () => {
        component.paymentForm.patchValue({ ownerName: '' });

        component.purchase();

        expect(ticketServiceSpy.purchaseTicket).not.toHaveBeenCalled();
    });

    it('should emit goBack when previousPage is called', () => {
        spyOn(component.goBack, 'emit');

        component.previousPage();

        expect(component.goBack.emit).toHaveBeenCalled();
    });

    it('should set successfullPurchase to true when opening success page', () => {
        component.showSuccesModal = true;

        component.openSuccessPage();

        expect(component.showSuccesModal).toBeFalse();
        expect(component.successfullPurchase).toBeTrue();
    });
});