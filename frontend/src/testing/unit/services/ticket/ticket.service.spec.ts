import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { TicketService } from "../../../../app/services/ticket/ticket.service";
import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { MockFactory } from "../../../utils/mock-factory";

describe('Ticket Service Tests', () => {
	let service: TicketService;
	let httpMock: HttpTestingController
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
		const mockRequest = MockFactory.buildPurchaseRequest(managerId, 1, [mockSeat], 'Tes Owner', '4539148912345674', '123', '2028-12');
		const mockTicket = MockFactory.buildTicketMock(ticketId, 'Boston Red Sox', 'New York Yankees', 'Yankee Stadium', 100, new Date(), 'New Sector', 'S-1');
		const mockResponse = MockFactory.buildPaginatedResponse(mockTicket);

		service.purchaseTicket(mockRequest).subscribe((response) => {
			expect(response.content.length).toEqual(mockResponse.content.length);
			expect(response).toEqual(mockResponse);
		});

		const request = httpMock.expectOne(service['apiUrl']);
		expect(request.request.method).toBe('POST');
		expect(request.request.body).toBe(mockRequest);
		expect(request.request.withCredentials).toBeTrue;
		request.flush(mockResponse);
	});

	it('should download the pdf of a ticket', () => {
		const mockResponse = new Blob(['fakePdf'], { type: 'application/pdf' });
		service.downloadPdf(ticketId).subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const url = `${service['apiUrl']}/${ticketId}/download`;
		const request = httpMock.expectOne(url);
		expect(request.request.method).toBe('GET');
		expect(request.request.withCredentials).toBeTrue();
		request.flush(mockResponse);
	});
});