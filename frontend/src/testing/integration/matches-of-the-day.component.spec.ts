import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatchesOfTheDayComponent } from '../../app/components/match/matches-of-the-day/matches-of-the-day.component';
import { MatchService, PaginatedMatches } from '../../app/services/match.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Matches Of The Day Component Integration Test', () => {
	let fixture: ComponentFixture<MatchesOfTheDayComponent>;
	let component: MatchesOfTheDayComponent;
	let httpMock: HttpTestingController;

	const baseUrl = 'https://localhost:8443/api/v1/matches/today';

	const generateMockMatches = (count: number): PaginatedMatches => ({
		content: Array.from({ length: count }, (_, i) => ({
			homeTeam: { name: `Team ${i}`, abbreviation: `T${i}`, league: 'AL', division: 'East' },
			awayTeam: {
				name: `Opponent ${i}`,
				abbreviation: `O${i}`,
				league: 'AL',
				division: 'East',
			},
			homeScore: i,
			awayScore: i + 1,
			date: `2025-10-22 19:${String(i).padStart(2, '0')}`,
			status: 'FINISHED',
			stadiumName: 'Yankee Stadium'
		})),
		page: {
			size: 10,
			number: count === 10 ? 0 : 1,
			totalElements: 15,
			totalPages: 2,
		},
	});

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [MatchesOfTheDayComponent],
			providers: [MatchService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		fixture = TestBed.createComponent(MatchesOfTheDayComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should load the first 10 matches scheduled for today', () => {
		fixture.detectChanges();

		const req = httpMock.expectOne(`${baseUrl}?page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(generateMockMatches(10));

		fixture.detectChanges();

		expect(component.pageSize).toBe(10);
		expect(component.currentPage).toBe(0);
		expect(component.hasMore).toBeTrue();
		expect(component.matches.length).toBe(10);
		expect(component.matches[0].homeTeam.abbreviation).toBe('T0');
		expect(component.matches[9].awayTeam.abbreviation).toBe('O9');
	});

	it('should load the remaining matches on next page', () => {
		fixture.detectChanges();

		const firstReq = httpMock.expectOne(`${baseUrl}?page=0&size=10`);
		firstReq.flush(generateMockMatches(10));
		fixture.detectChanges();

		component.loadNextPage();

		const secondReq = httpMock.expectOne(`${baseUrl}?page=1&size=10`);
		expect(secondReq.request.method).toBe('GET');
		secondReq.flush(generateMockMatches(5));
		fixture.detectChanges();

		expect(component.matches.length).toBe(15);
		expect(component.currentPage).toBe(1);
		expect(component.hasMore).toBeFalse();
	});
});