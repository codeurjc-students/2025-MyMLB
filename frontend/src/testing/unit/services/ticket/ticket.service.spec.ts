import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { TicketService } from "../../../../app/services/ticket/ticket.service";
import { TestBed, tick } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { MockFactory } from "../../../utils/mock-factory";

describe('Ticket Service Tests', () => {
	let service: TicketService;
	let httpMock: HttpTestingController
	const apiUrl = 'https://localhost:8443/api/v1/tickets';
	const managerId = 1;
	const ticketId = 2;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [TicketService, provideHttpClient(withFetch()), provideHttpClientTesting()]
		});
		service = TestBed.inject(TicketService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should purchase a ticket', () => {
		const mockSeat = MockFactory.buildMockSeat(1, 'S-1');
		const mockRequest = MockFactory.buildPurchaseRequest(managerId, 1, [mockSeat], 'Tes Owner', '4539148912345674', '123', new Date(2028, 12, 1));
		const mockTicket = MockFactory.buildTicketMock(ticketId, 'Boston Red Sox', 'New York Yankees', 'Yankee Stadium', 100, new Date(), 'New Sector', 'S-1');
		const mockResponse = MockFactory.buildPaginatedResponse(mockTicket);

		service.purchaseTicket(mockRequest).subscribe((response) => {
			expect(response.content.length).toEqual(mockResponse.content.length);
			expect(response).toEqual(mockResponse);
		});

		const request = httpMock.expectOne(apiUrl);
		expect(request.request.method).toBe('POST');
		expect(request.request.body).toBe(mockRequest);
		expect(request.request.withCredentials).toBeTrue;
		request.flush(mockResponse);
	});
});