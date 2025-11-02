import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TeamService } from '../../../app/services/team.service';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Team } from '../../../app/models/team.model';

describe('Team Service Tests', () => {
	let service: TeamService;
	let httpMock: HttpTestingController;

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
			providers: [TeamService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});
		service = TestBed.inject(TeamService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should fetch all the teams grouped by their league and division', () => {
		service.getStandings().subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const req = httpMock.expectOne('https://localhost:8443/api/teams/standings');
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});
});