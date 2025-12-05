import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatchService, PaginatedMatches, ShowMatch } from '../../../app/services/match.service';
import { provideHttpClient, withFetch } from '@angular/common/http';

describe('Match Service Tests', () => {
	let service: MatchService;
	let httpMock: HttpTestingController;
	const apiUrl = 'https://localhost:8443/api/v1/matches';

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
				status: 'FINISHED',
			},
		],
		page: {
			size: 10,
			number: 0,
			totalElements: 1,
			totalPages: 1,
		},
	};

	const mockHomeMatches: ShowMatch[] = [
		{
			homeTeam: {
				name: 'New York Yankees',
				abbreviation: 'NYY',
				league: 'AL',
				division: 'EAST',
			},
			awayTeam: {
				name: 'Boston Red Sox',
				abbreviation: 'BOS',
				league: 'AL',
				division: 'EAST',
			},
			homeScore: 5,
			awayScore: 3,
			date: '2025-10-22 19:05',
			status: 'FINISHED',
		},

		{
			homeTeam: {
				name: 'New York Yankees',
				abbreviation: 'NYY',
				league: 'AL',
				division: 'East',
			},
			awayTeam: {
				name: 'Los Angeles Dodgers',
				abbreviation: 'LAD',
				league: 'NL',
				division: 'WEST',
			},
			homeScore: 0,
			awayScore: 0,
			date: '2025-11-22 19:05',
			status: 'SCHEDULED',
		},
	];

	const mockAwayMatches: ShowMatch[] = [
		{
			homeTeam: {
				name: 'New York Yankees',
				abbreviation: 'NYY',
				league: 'AL',
				division: 'EAST',
			},
			awayTeam: {
				name: 'Boston Red Sox',
				abbreviation: 'BOS',
				league: 'AL',
				division: 'EAST',
			},
			homeScore: 5,
			awayScore: 3,
			date: '2025-10-22 19:05',
			status: 'FINISHED',
		}
	];

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
		service.getMatchesOfTheDay(0, 10).subscribe((data) => {
			expect(data).toEqual(mockResponse);
			expect(data.content.length).toBe(1);
			expect(data.content[0].homeTeam.abbreviation).toBe('NYY');
			expect(data.content[0].status).toBe('FINISHED');
		});

		const req = httpMock.expectOne(`${apiUrl}/today?page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should fetch the home matches of a certain team', () => {
		service.getHomeMatches('New York Yankees').subscribe((response) => {
			expect(response.length).toBe(2);
			expect(response[0].awayTeam.name).toBe('Boston Red Sox');
			expect(response[1].awayTeam.name).toBe('Los Angeles Dodgers');
		});

		const req = httpMock.expectOne(`${apiUrl}/home/New York Yankees`);
		expect(req.request.method).toBe('GET');
		req.flush(mockHomeMatches);
	});

	it('should fetch the away matches of a certain team', () => {
		service.getAwayMatches('Boston Red Sox').subscribe((response) => {
			expect(response.length).toBe(1);
			expect(response[0].homeTeam.name).toBe('New York Yankees');
		});

		const req = httpMock.expectOne(`${apiUrl}/away/Boston Red Sox`);
		expect(req.request.method).toBe('GET');
		req.flush(mockAwayMatches);
	});
});