import { ComponentFixture, TestBed } from "@angular/core/testing";
import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { provideHttpClient } from "@angular/common/http";
import { ActivatedRoute } from "@angular/router";
import { Subject } from "rxjs";
import { MockFactory } from "../../utils/mock-factory";
import { Pictures } from "../../../app/models/pictures.model";
import { EditEventComponent } from "../../../app/components/ticket/edit-event/edit-event.component";

describe('Edit Event Integration Test', () => {
    let fixture: ComponentFixture<EditEventComponent>;
    let component: EditEventComponent;
    let httpMock: HttpTestingController;
    let routeQueryParams$: Subject<any>;

    const apiUrl = 'https://localhost:8443/api/v1/events';
    const sectorId = 1;
	const eventId = 2;
	const managerId = 1;

	const mockEventManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 100, 100, 100);
	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockEvent = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockEventManager]);

    const mockRequest = MockFactory.buildEditEventRequest(eventId, [sectorId], [500]);

	beforeEach(() => {
        routeQueryParams$ = new Subject();

        TestBed.configureTestingModule({
            imports: [EditEventComponent],
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: routeQueryParams$.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(EditEventComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        fixture.detectChanges();
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should edit the event successfully', () => {
        routeQueryParams$.next({ eventId: eventId });

        const getReq = httpMock.expectOne(`${apiUrl}/${eventId}`);
        expect(getReq.request.method).toBe('GET');
        getReq.flush(mockEvent);

		if (component.eventInfo && component.eventInfo.sectors.length > 0) {
            component.eventInfo.sectors[0].price = 500;
        }
        component.editEvent();

        const editReq = httpMock.expectOne(apiUrl);
        expect(editReq.request.method).toBe('PUT');

        expect(editReq.request.body).toEqual(mockRequest);

        editReq.flush({ message: 'Success' });

        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Event Successfully Modified');
    });
});