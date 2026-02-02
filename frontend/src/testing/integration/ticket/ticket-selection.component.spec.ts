import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { MockFactory } from '../../utils/mock-factory';
import { TicketSelectionComponent } from '../../../app/components/ticket/ticket-selection/ticket-selection.component';
import { EventService } from '../../../app/services/ticket/event.service';
import { Pictures } from '../../../app/models/pictures.model';

describe('Ticket Selection Component Integration Test', () => {
    let fixture: ComponentFixture<TicketSelectionComponent>;
    let component: TicketSelectionComponent;
    let httpMock: HttpTestingController;
    let routeQueryParams$: Subject<any>;

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
    const mockSectors = MockFactory.buildPaginatedResponse(MockFactory.buildEventManager(1, 10, 'North', 50, 100, 100));
    const mockSeats = MockFactory.buildPaginatedResponse(MockFactory.buildMockSeat(1, 'A-1'));

    beforeEach(() => {
        routeQueryParams$ = new Subject();

        TestBed.configureTestingModule({
            imports: [TicketSelectionComponent],
            providers: [
                EventService,
                provideHttpClient(),
                provideHttpClientTesting(),
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: routeQueryParams$.asObservable() }
                }
            ]
        });
        fixture = TestBed.createComponent(TicketSelectionComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        fixture.detectChanges();
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should load event info and sectors sequentially on init when matchId is present', () => {
        routeQueryParams$.next({ matchId: matchId });

        const eventReq = httpMock.expectOne(`${apiUrl}/match/${matchId}`);
        expect(eventReq.request.method).toBe('GET');
        eventReq.flush(mockEvent);

        const sectorReq = httpMock.expectOne(`${apiUrl}/${eventId}/sectors`);
        expect(sectorReq.request.method).toBe('GET');
        sectorReq.flush(mockSectors);

        expect(component.event).toEqual(mockEvent);
        expect(component.availableSectors.length).toBeGreaterThan(0);
        expect(component.error).toBeFalse();
    });

    it('should load available seats when a sector is selected', () => {
        component.event = mockEvent;
        component.selectedSectorId = sectorId;

        component.onSectorChange();

        const seatsReq = httpMock.expectOne(`${apiUrl}/${eventId}/sector/${sectorId}`);
        expect(seatsReq.request.method).toBe('GET');
        seatsReq.flush(mockSeats);

        expect(component.availableSeats.length).toBe(1);
        expect(component.availableSeats[0].id).toBe(1);
    });
});