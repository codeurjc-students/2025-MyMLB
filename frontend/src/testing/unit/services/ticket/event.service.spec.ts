import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { EventService } from "../../../../app/services/ticket/event.service";
import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { MockFactory } from "../../../utils/mock-factory";
import { Pictures } from "../../../../app/models/pictures.model";

describe('Event Service Tests', () => {
	let service: EventService;
	let httpMock: HttpTestingController;
	const apiUrl = 'https://localhost:8443/api/v1/events';
	const sectorId = 1;
	const eventId = 2;
	const matchId = 1;
	const managerId = 1;

	const mockEventManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 100, 100, 100);
	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockEvent = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockEventManager]);

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [EventService, provideHttpClient(withFetch()), provideHttpClientTesting()]
		});
		service = TestBed.inject(EventService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should return the event of the given id', () => {
		service.getEventById(eventId).subscribe((event) => {
			expect(event).toEqual(mockEvent);
		});

		const request = httpMock.expectOne(`${apiUrl}/${eventId}`);
		expect(request.request.method).toBe('GET');
		request.flush(mockEvent);
	});

	it('should return the event associated to the given match', () => {
		service.getEventByMatchId(matchId).subscribe((event) => {
			expect(event).toEqual(mockEvent);
		});

		const request = httpMock.expectOne(`${apiUrl}/match/${matchId}`);
		expect(request.request.method).toBe('GET');
		request.flush(mockEvent);
	});

	it('should return the available sectors of a given event', () => {
		const mockResponse = MockFactory.buildPaginatedResponse(mockEventManager);
		service.getAvailableSectors(eventId).subscribe((sectors) => {
			expect(sectors.content.length).toBe(mockResponse.content.length);
			expect(sectors.content).toEqual(mockResponse.content);
		});

		const request = httpMock.expectOne(`${apiUrl}/${eventId}/sectors`);
		expect(request.request.method).toBe('GET');
		request.flush(mockResponse);
	});

	it('should return the available seats of a given event and sector', () => {
		const mockSeat = MockFactory.buildMockSeat(5, 'S-1');
		const mockResponse = MockFactory.buildPaginatedResponse(mockSeat);
		service.getAvailableSeats(eventId, sectorId).subscribe((seats) => {
			expect(seats.content.length).toBe(mockResponse.content.length);
			expect(seats.content).toEqual(mockResponse.content);
		});

		const request = httpMock.expectOne(`${apiUrl}/${eventId}/sector/${sectorId}`);
		expect(request.request.method).toBe('GET');
		request.flush(mockResponse);
	});

	it('should create an event', () => {
		const mockSectorRequest = MockFactory.buildSectorCreateRequest('New Sector', 200);
		const mockRequest = MockFactory.buildEventCreateRequest(matchId, [100], [mockSectorRequest]);
		const mockNewManager = MockFactory.buildEventManager(4, 4, 'New Sector', 100, 200, 200);
		const mockResponse = MockFactory.buildEventResponse(10, 'Texas Rangers', 'Detroit Tigers', 'DET', 'Comerica Park', new Date(), mockPicture, [mockNewManager]);

		service.createEvent(mockRequest).subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const request = httpMock.expectOne(apiUrl);
		expect(request.request.method).toBe('POST');
		expect(request.request.body).toBe(mockRequest);
		request.flush(mockResponse);
	});

	it('should edit an event', () => {
		const mockRequest = MockFactory.buildEditEventRequest(eventId, [sectorId], [500]);
		const mockManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 500, 100, 100);
		const mockResponse = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockManager]);

		service.editEvent(mockRequest).subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const request = httpMock.expectOne(apiUrl);
		expect(request.request.method).toBe('PUT');
		expect(request.request.body).toBe(mockRequest);
		request.flush(mockResponse);
	});

	it('should delete an event', () => {
		service.deleteEvent(eventId).subscribe((response) => {
			expect(response).toEqual(mockEvent);
		});

		const request = httpMock.expectOne(`${apiUrl}/${eventId}`);
		expect(request.request.method).toBe('DELETE');
		request.flush(mockEvent);
	});
});