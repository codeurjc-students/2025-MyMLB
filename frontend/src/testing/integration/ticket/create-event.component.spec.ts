import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CreateEventComponent } from "../../../app/components/ticket/create-event/create-event.component";
import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { Subject } from "rxjs";
import { MockFactory } from "../../utils/mock-factory";
import { EventService } from "../../../app/services/ticket/event.service";
import { provideHttpClient } from "@angular/common/http";
import { Pictures } from "../../../app/models/pictures.model";
import { ActivatedRoute } from "@angular/router";

describe('Create Event Integration Tests', () => {
	let fixture: ComponentFixture<CreateEventComponent>;
	let component: CreateEventComponent;
	let httpMock: HttpTestingController;
	let apiUrl = 'https://localhost:8443/api/v1/events';
	let matchUrl = 'https://localhost:8443/api/v1/matches';
	let routeQueryParams$: Subject<any>;

	const matchId = 1;

	const mockSectorRequest = MockFactory.buildSectorCreateRequest('New Sector', 200);
	const mockRequest = MockFactory.buildEventCreateRequest(matchId, [100], [mockSectorRequest]);
	const mockNewManager = MockFactory.buildEventManager(4, 4, 'New Sector', 100, 200, 200);
	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockResponse = MockFactory.buildEventResponse(10, 'Texas Rangers', 'Detroit Tigers', 'DET', 'Comerica Park', new Date(), mockPicture, [mockNewManager]);

	beforeEach(() => {
		routeQueryParams$ = new Subject();

		TestBed.configureTestingModule({
			imports: [CreateEventComponent],
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
		fixture = TestBed.createComponent(CreateEventComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);

		fixture.detectChanges();
	});

	it('should create the event', () => {
        routeQueryParams$.next({ matchId: matchId });
        const matchReq = httpMock.expectOne(`${matchUrl}/${matchId}`);
        matchReq.flush({});

        component.numSectors = 1;
        component.sectorNames = ['New Sector'];
        component.sectorPrices = [100];
        component.sectorCapacities = [200];

        component.createEvent();

        const request = httpMock.expectOne(apiUrl);
        expect(request.request.method).toBe('POST');
        expect(request.request.body).toEqual(mockRequest);

        request.flush(mockResponse);

        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Event Successfully Created');
    });
});