import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateEventComponent } from '../../../../app/components/ticket/create-event/create-event.component';
import { EventService } from '../../../../app/services/ticket/event.service';
import { MatchService } from '../../../../app/services/match.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { MockFactory } from '../../../utils/mock-factory';
import { Pictures } from '../../../../app/models/pictures.model';

describe('Create Event Component Tests', () => {
    let component: CreateEventComponent;
    let fixture: ComponentFixture<CreateEventComponent>;
    let eventServiceSpy: jasmine.SpyObj<EventService>;
    let matchServiceSpy: jasmine.SpyObj<MatchService>;
    let routeQueryParams$: Subject<any>;

	const mockHomeTeam = MockFactory.buildTeamSummaryMock('New York Yankees', 'NYY', 'AL', 'EAST');
	const mockAwayTeam = MockFactory.buildTeamSummaryMock('Boston Red Sox', 'BOS', 'AL', 'EAST');

    beforeEach(() => {
        eventServiceSpy = jasmine.createSpyObj('EventService', ['createEvent']);
        matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchById']);
        routeQueryParams$ = new Subject();

        TestBed.configureTestingModule({
            imports: [CreateEventComponent],
            providers: [
                { provide: EventService, useValue: eventServiceSpy },
                { provide: MatchService, useValue: matchServiceSpy },
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: routeQueryParams$.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(CreateEventComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load match info when component is created', () => {
		const mockMatch = MockFactory.buildShowMatchMock(1, mockAwayTeam, mockHomeTeam, 2, 3, '2026-05-15', 'IN PROGRESS');
        matchServiceSpy.getMatchById.and.returnValue(of(mockMatch));

        routeQueryParams$.next({ matchId: '1' });

        expect(matchServiceSpy.getMatchById).toHaveBeenCalledWith(1);
        expect(component.matchInfo).toEqual(mockMatch);
    });

    it('should manage sector names correctly', () => {
        component.numSectors = 2;

        component.addSectorName('North');
        component.addSectorName('South');
        component.addSectorName('East');

        expect(component.sectorNames.length).toBe(2);
        expect(component.sectorNames).toContain('North');

        component.removeSectorName(0);
        expect(component.sectorNames[0]).toBe('South');
    });

    it('should validate if the event creation is correct', () => {
        component.numSectors = 1;
        expect(component.isValid()).toBeFalse();

        component.addSectorName('Main');
        component.addSectorPrice(50);
        component.addSectorCapacity(100);

        expect(component.isValid()).toBeTrue();
    });

    it('should create event successfully', () => {
		const sectorId = 1;
		const eventId = 2;
		const managerId = 1;

		const mockEventManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 100, 100, 100);
		const mockPicture: Pictures = {
			url: 'https://test-url',
			publicId: 'test'
		}
		const mockEvent = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockEventManager]);

		(component as any).matchId = 1;
        component.numSectors = 1;
        component.addSectorName('Main');
        component.addSectorPrice(50);
        component.addSectorCapacity(100);

        eventServiceSpy.createEvent.and.returnValue(of(mockEvent));

        component.createEvent();

        expect(eventServiceSpy.createEvent).toHaveBeenCalled();
        expect(component.success).toBeTrue();
        expect(component.successMessage).toContain('Successfully Created');
    });

    it('should handle error when creating event', () => {
        (component as any).matchId = 1;
        component.numSectors = 1;
        component.addSectorName('Main');
        component.addSectorPrice(50);
        component.addSectorCapacity(100);

        eventServiceSpy.createEvent.and.returnValue(throwError(() => new Error('API Error')));

        component.createEvent();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('An error occur while attempting to create the event');
    });
});