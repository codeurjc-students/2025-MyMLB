import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TicketPurchaseComponent } from '../../../app/components/ticket/ticket-purchase/ticket-purchase.component';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MockFactory } from '../../utils/mock-factory';
import { TicketService } from '../../../app/services/ticket/ticket.service';
import { provideHttpClient } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { Pictures } from '../../../app/models/pictures.model';

describe('Ticket Purchase Component Integration Test', () => {
	let fixture: ComponentFixture<TicketPurchaseComponent>;
	let component: TicketPurchaseComponent;
	let httpMock: HttpTestingController;

	const apiUrl = 'https://localhost:8443/api/v1/tickets';
	const managerId = 1;
	const ticketId = 2;
	const sectorId = 1;
	const eventId = 2;

	const mockEventManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 100, 100, 100);
	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockEvent = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockEventManager]);
	const mockSeat = MockFactory.buildMockSeat(1, 'S-1');
	const mockRequest = MockFactory.buildPurchaseRequest(managerId, 1, [mockSeat], 'Test Owner', '4539148912345674', '123', '2028-12');
	const mockTicket = MockFactory.buildTicketMock(ticketId, 'Boston Red Sox', 'New York Yankees', 'Yankee Stadium', 100, new Date(), 'New Sector', 'S-1');

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [TicketPurchaseComponent, ReactiveFormsModule],
			providers: [TicketService, provideHttpClient(), provideHttpClientTesting()]
		});
		fixture = TestBed.createComponent(TicketPurchaseComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);

		component.event = mockEvent;
        component.eventManagerId = managerId;
        component.tickets = 1;
        component.seats = [mockSeat];
        component.totalPrice = 100;

		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should purchase the ticket successfully', () => {
		component.paymentForm.patchValue({
			ownerName: 'Test Owner',
			cardNumber: '4539148912345674',
			cvv: '123',
			expirationDate: '2028-12'
		});

		component.purchase();
		const request = httpMock.expectOne(apiUrl);
		expect(request.request.method).toBe('POST');
		expect(request.request.body).toEqual(mockRequest);
		expect(request.request.body.ownerName).toBe('Test Owner');
		request.flush(mockTicket);

		expect(component.showSuccesModal).toBeTrue();
		expect(component.successMessage).toBe('Thank you for your purchase');
	});
});