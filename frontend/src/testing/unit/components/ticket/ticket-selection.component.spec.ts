import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TicketSelectionComponent } from '../../../../app/components/ticket/ticket-selection/ticket-selection.component';
import { EventService } from '../../../../app/services/ticket/event.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { ActivatedRoute } from '@angular/router';
import { MockFactory } from '../../../utils/mock-factory';
import { of, Subject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';

describe('Ticket Selection Component Tests', () => {
    let component: TicketSelectionComponent;
    let fixture: ComponentFixture<TicketSelectionComponent>;
    let eventServiceSpy: jasmine.SpyObj<EventService>;
    let routeQueryParams$: Subject<any>;

    const mockSector = MockFactory.buildEventManager(10, 1, 'North Sector', 50, 100, 100);
    const mockSectorsResponse = MockFactory.buildPaginatedResponse(mockSector);

    const mockEvent = MockFactory.buildEventResponse(
        1, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), { url: 'stadium.jpg', publicId: '1' }, [mockSector]
    );

    const mockSeat = MockFactory.buildMockSeat(1, 'S-1');
    const mockSeatsResponse = MockFactory.buildPaginatedResponse(mockSeat);

    beforeEach(() => {
        eventServiceSpy = jasmine.createSpyObj('EventService', ['getEventByMatchId', 'getAvailableSectors', 'getAvailableSeats']);
        routeQueryParams$ = new Subject();

        TestBed.configureTestingModule({
            imports: [TicketSelectionComponent],
            providers: [
                provideHttpClient(),
                { provide: EventService, useValue: eventServiceSpy },
                { provide: BackgroundColorService, useValue: { getBackgroundColor: () => 'bg-blue' } },
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: routeQueryParams$.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(TicketSelectionComponent);
        component = fixture.componentInstance;
    });

    it('should load event and sectors when matchId is present in queryParams', () => {
        eventServiceSpy.getEventByMatchId.and.returnValue(of(mockEvent as any));
        eventServiceSpy.getAvailableSectors.and.returnValue(of(mockSectorsResponse));

        fixture.detectChanges();
        routeQueryParams$.next({ matchId: '1' });

        expect(eventServiceSpy.getEventByMatchId).toHaveBeenCalledWith(1);
        expect(component.event).toEqual(mockEvent as any);
        expect(component.availableSectors).toEqual(mockSectorsResponse.content);
    });

    it('should load available seats when sector changes', () => {
        component.event = mockEvent as any;
        component.selectedSectorId = 10;
        eventServiceSpy.getAvailableSeats.and.returnValue(of(mockSeatsResponse));

        component.onSectorChange();

        expect(eventServiceSpy.getAvailableSeats).toHaveBeenCalledWith(mockEvent.id, 10);
        expect(component.availableSeats).toEqual(mockSeatsResponse.content);
        expect(component.selectedSeats).toEqual([]);
    });

    it('should calculate total price correctly', () => {
        component.availableSectors = [mockSector];
        component.selectedSectorId = 10;
        component.ticketAmount = 3;

        component.onTicketAmountChange();

        expect(component.totalPrice).toBe(150);
    });

    it('should add and remove seats from selection', () => {
        component.ticketAmount = 2;
        const seat2 = { id: 2, seatNumber: 'S-2' } as any;

        component.addSeats(mockSeat);
        expect(component.selectedSeats.length).toBe(1);
        expect(component.isAlreadySelected(1)).toBeTrue();

        component.addSeats(seat2);
        expect(component.selectedSeats.length).toBe(2);

        component.addSeats({ id: 3 } as any);
        expect(component.selectedSeats.length).toBe(2);

        component.addSeats(mockSeat);
        expect(component.selectedSeats.length).toBe(1);
    });

    it('should toggle stadium map visibility', () => {
        expect(component.isPictureOpen).toBeFalse();
        component.openStadiumMap();
        expect(component.isPictureOpen).toBeTrue();
    });
});