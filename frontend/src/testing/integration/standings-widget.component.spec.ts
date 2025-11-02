import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StandingsWidgetComponent } from '../../app/components/standings/standings-widget/standings-widget.component';
import { TeamService } from '../../app/services/team.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Team } from '../../app/models/team.model';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Standings Widget Component Integration Test', () => {
	let fixture: ComponentFixture<StandingsWidgetComponent>;
	let component: StandingsWidgetComponent;
	let httpMock: HttpTestingController;

	const apiUrl = 'https://localhost:8443/api/teams/standings';

	const mockResponse = {
		American: {
			East: [
				{ name: 'Yankees', abbreviation: 'NYY', wins: 10, losses: 5 } as Team,
				{ name: 'Red Sox', abbreviation: 'BOS', wins: 8, losses: 7 } as Team,
			],
			West: [{ name: 'Astros', abbreviation: 'HOU', wins: 12, losses: 3 } as Team],
		},
		National: {
			Central: [{ name: 'Cubs', abbreviation: 'CHC', wins: 9, losses: 6 } as Team],
		},
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [StandingsWidgetComponent],
			providers: [
				TeamService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting()
			]
		});

		fixture = TestBed.createComponent(StandingsWidgetComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should load standings from the backend and populate the component', () => {
		fixture.detectChanges();

		const req = httpMock.expectOne(apiUrl);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);

		fixture.detectChanges();

		expect(component.standings.length).toBe(3);
		expect(component.standings[0].league).toBe('American');
		expect(component.standings[0].division).toBe('East');
		expect(component.standings[0].teams.length).toBe(2);
		expect(component.errorMessage).toBe('');
	});

	it('should handle error when backend fails', () => {
		fixture.detectChanges();

		const req = httpMock.expectOne(apiUrl);
		req.error(new ErrorEvent('Network error'));

		fixture.detectChanges();

		expect(component.errorMessage).toBe('Error trying to load the standings');
		expect(component.standings.length).toBe(0);
	});

	it('should navigate correctly with next() and previous()', () => {
		fixture.detectChanges();

		const req = httpMock.expectOne(apiUrl);
		req.flush(mockResponse);
		fixture.detectChanges();

		const total = component.standings.length;
		const initial = component.currentIndex;

		component.next();
		expect(component.currentIndex).toBe((initial + 1) % total);

		component.previous();
		expect(component.currentIndex).toBe((total + initial) % total);
	});
});