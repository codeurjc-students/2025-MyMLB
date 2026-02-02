import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatchService, ShowMatch } from '../../../app/services/match.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { MockFactory } from '../../utils/mock-factory';

describe('Match Service Tests', () => {
	let service: MatchService;
	let httpMock: HttpTestingController;
	const apiUrl = 'https://localhost:8443/api/v1/matches';

	const match1Id = 1;
	const match2Id = 2;

	const mockHomeTeam = MockFactory.buildTeamSummaryMock('New York Yankees', 'NYY', 'AL', 'EAST');
	const mockAwayTeam = MockFactory.buildTeamSummaryMock('Boston Red Sox', 'BOS', 'AL', 'EAST');
	const mockAuxTeam = MockFactory.buildTeamSummaryMock('Los Angeles Dodgers', 'LAD', 'NL', 'WEST');
	const mockMatch = MockFactory.buildShowMatchMock(match1Id, mockAwayTeam, mockHomeTeam, 2, 3, '2026-05-15', 'SCHEDULED');
	const auxMatch = MockFactory.buildShowMatchMock(match2Id, mockAuxTeam, mockHomeTeam, 2, 3, '2026-06-15', 'SCHEDULED');
	const mockResponse = MockFactory.buildPaginatedResponse(mockMatch);
	const mockHomeMatches: ShowMatch[] = [mockMatch, auxMatch];

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

	it('should return the match of the given id', () => {
		service.getMatchById(match1Id).subscribe((response) => {
			expect(response).toEqual(mockMatch);
		});

		const request = httpMock.expectOne(`${apiUrl}/${match1Id}`);
		expect(request.request.method).toBe('GET');
		request.flush(mockMatch);
	});

	it('should fetch matches of the day paginated', () => {
		service.getMatchesOfTheDay(0, 10).subscribe((data) => {
			expect(data).toEqual(mockResponse);
			expect(data.content.length).toBe(1);
			expect(data.content[0].homeTeam.abbreviation).toBe('NYY');
			expect(data.content[0].status).toBe('SCHEDULED');
		});

		const req = httpMock.expectOne(`${apiUrl}/today?page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should fetch matches of a team by month', () => {
		const teamName = 'New York Yankees';
		const year = 2025;
		const month = 10;

		service.getMatchesOfTeamByMonth(teamName, year, month).subscribe((data) => {
			expect(data).toEqual(mockHomeMatches);
			expect(data.length).toBe(2);
			expect(data[0].homeTeam.name).toBe('New York Yankees');
			expect(data[1].awayTeam.abbreviation).toBe('LAD');
			expect(data[1].status).toBe('SCHEDULED');
		});

		const req = httpMock.expectOne(`${apiUrl}/team/${teamName}?year=${year}&month=${month}`);
		expect(req.request.method).toBe('GET');
		req.flush(mockHomeMatches);
	});
});