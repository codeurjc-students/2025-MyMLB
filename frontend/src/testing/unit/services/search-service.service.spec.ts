import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SearchService } from '../../../app/services/search.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Team } from '../../../app/models/team.model';
import { PositionPlayerGlobal } from '../../../app/models/position-player.model';
import { MockFactory } from '../../utils/mock-factory';

describe('Search Service Tests', () => {
	let service: SearchService;
	let httpMock: HttpTestingController;

	const apiUrl = '/api/v1/searchs';

	const mockTeam = MockFactory.buildTeamMocks(
		'New York Yankees',
		'NYY',
		'American',
		'East',
		162,
		100,
		62,
		0.617,
		0,
		'7-3'
	);

	const mockTeamResponse = MockFactory.buildPaginatedSearchsForTeam(mockTeam);

	const pitchers = MockFactory.buildPitcherGlobalMock(
		'Gerrit Cole',
		'New York Yankees',
		'SP',
		30,
		15,
		5,
		2.5,
		200,
		220,
		40,
		150,
		60,
		1.05,
		0,
		0
	);

	const mockPlayerResponse = MockFactory.buildPaginatedSearchsForPlayer(pitchers);


	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [SearchService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		service = TestBed.inject(SearchService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should search for players with playerType pitcher', () => {
		service.search('player', 'Gerrit Cole', 2, 5, 'pitcher').subscribe((res) => {
			expect(res.content[0].name).toBe('Gerrit Cole');
			expect((res.content[0] as PositionPlayerGlobal).position).toBe('SP');
			expect(res.page.size).toBe(5);
			expect(res.page.number).toBe(2);
		});

		const req = httpMock.expectOne(
			`${apiUrl}/player?query=Gerrit%20Cole&page=2&size=5&playerType=pitcher`
		);
		expect(req.request.method).toBe('GET');
		req.flush(mockPlayerResponse);
	});

	it('should search for teams', () => {
		service.search('team', 'new york yankees').subscribe((res) => {
			expect(res.content[0].name).toBe('New York Yankees');
			expect((res.content[0] as Team).abbreviation).toBe('NYY');
			expect(res.page.number).toBe(0);
			expect(res.page.totalPages).toBe(1);
		});

		const req = httpMock.expectOne(`${apiUrl}/team?query=new%20york%20yankees&page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockTeamResponse);
	});

	it('should search for stadiums with custom pagination', () => {
		service.search('stadium', 'yankee stadium', 1, 20).subscribe((res) => {
			expect(res.page.size).toBe(10);
			expect(res.page.totalPages).toBe(1);
		});

		const req = httpMock.expectOne(
			`${apiUrl}/stadium?query=yankee%20stadium&page=1&size=20`
		);
		expect(req.request.method).toBe('GET');
		req.flush(mockTeamResponse);
	});
});