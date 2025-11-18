import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatchService, PaginatedMatches } from '../../../app/services/match.service';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Match Service Tests', () => {
	let service: MatchService;
	let httpMock: HttpTestingController;

	beforeEach(() => {
			TestBed.configureTestingModule({
				providers: [MatchService, provideHttpClient(withFetch()), provideHttpClientTesting()],
			});
			service = TestBed.inject(MatchService);
			httpMock = TestBed.inject(HttpTestingController);
		});

	afterEach(() => {
		httpMock.verify();
	});

	it('should fetch matches of the day paginated', () => {
		const mockResponse: PaginatedMatches = {
			content: [
				{
					homeTeam: {
						name: 'Yankees',
						abbreviation: 'NYY',
						league: 'AL',
						division: 'East',
					},
					awayTeam: {
						name: 'Red Sox',
						abbreviation: 'BOS',
						league: 'AL',
						division: 'East',
					},
					homeScore: 5,
					awayScore: 3,
					date: '2025-10-22 19:05',
					status: 'FINISHED'
				},
			],
			page: {
				size: 10,
				number: 0,
				totalElements: 1,
				totalPages: 1,
			},
		};

		service.getMatchesOfTheDay(0, 10).subscribe((data) => {
			expect(data).toEqual(mockResponse);
			expect(data.content.length).toBe(1);
			expect(data.content[0].homeTeam.abbreviation).toBe('NYY');
			expect(data.content[0].status).toBe('FINISHED');
		});

		const req = httpMock.expectOne('https://localhost:8443/api/matches/today?page=0&size=10');
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});
});