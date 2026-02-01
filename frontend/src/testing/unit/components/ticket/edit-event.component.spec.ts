import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditEventComponent } from '../../../../app/components/ticket/edit-event/edit-event.component';
import { EventService } from '../../../../app/services/ticket/event.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { MockFactory } from '../../../utils/mock-factory';
import { FormsModule } from '@angular/forms';
import { Pictures } from '../../../../app/models/pictures.model';

describe('Edit Event Component Tests', () => {
    let component: EditEventComponent;
    let fixture: ComponentFixture<EditEventComponent>;
    let eventServiceSpy: jasmine.SpyObj<EventService>;
    let routeQueryParams$: Subject<any>;

	const sectorId = 1;
	const eventId = 2;
	const managerId = 1;

   	const mockSector = MockFactory.buildEventManager(managerId, sectorId, 'North Sector', 50, 100, 100);

	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockEvent = MockFactory.buildEventResponse(
        eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockSector]
    );

    beforeEach(() => {
        eventServiceSpy = jasmine.createSpyObj('EventService', ['getEventById', 'editEvent']);
        routeQueryParams$ = new Subject();

        TestBed.configureTestingModule({
            imports: [EditEventComponent, FormsModule],
            providers: [
                { provide: EventService, useValue: eventServiceSpy },
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: routeQueryParams$.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(EditEventComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should fetch event data on component creation', () => {
        eventServiceSpy.getEventById.and.returnValue(of(mockEvent));

        routeQueryParams$.next({ eventId: eventId });

        expect(eventServiceSpy.getEventById).toHaveBeenCalledWith(eventId);
        expect(component.eventInfo).toEqual(mockEvent);
        expect(component.error).toBeFalse();
    });

    it('should edit event successfully', () => {
		const mockRequest = MockFactory.buildEditEventRequest(eventId, [sectorId], [500]);
		const mockManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 500, 100, 100);
		const mockResponse = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockManager]);

		(component as any).eventId = eventId;
        component.eventInfo = mockEvent
		component.eventInfo.sectors[0].price = 500;

        eventServiceSpy.editEvent.and.returnValue(of(mockResponse));

        component.editEvent();

        expect(eventServiceSpy.editEvent).toHaveBeenCalledWith(jasmine.objectContaining(mockRequest));
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Event Successfully Modified');
    });

    it('should handle error when editEvent fails', () => {
        (component as any).eventId = 100;
        component.eventInfo = mockEvent;
        eventServiceSpy.editEvent.and.returnValue(throwError(() => new Error('Update failed')));

        component.editEvent();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('An error occur while attempting to modified the event');
        expect(component.success).toBeFalse();
    });
});