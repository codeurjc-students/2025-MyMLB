import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SearchService } from '../../../app/services/search.service';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { TeamInfo } from '../../../app/models/team.model';
import { PositionPlayerGlobal } from '../../../app/models/position-player.model';
import { MockFactory } from '../../utils/mock-factory';
import { PaginatedSearchs } from '../../../app/models/pagination.model';
import { Stadium } from '../../../app/models/stadium.model';
import { PitcherGlobal } from '../../../app/models/pitcher.model';

describe('Search Service Tests', () => {
	let service: SearchService;
	let httpMock: HttpTestingController;

	const apiUrl = '/api/v1/searchs';

	const teamStats = MockFactory.buildTeamMocks(
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

	const stadium = MockFactory.buildStadiumCompleteMock('Yankee Stadium', 2009, 'New York Yankees', []);

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		teamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		stadium,
		[],
		[]
	);

	const mockTeamResponse: PaginatedSearchs = {
		content: [mockTeamInfo],
		page: {
			size: 10,
			number: 0,
			totalElements: 1,
			totalPages: 1,
		},
	};

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

	const mockPlayerResponse: PaginatedSearchs = {
		content: [pitchers],
		page: {
			size: 5,
			number: 2,
			totalElements: 1,
			totalPages: 1,
		},
	};

	const mockStadiumResponse: PaginatedSearchs = {
		content: [stadium],
		page: {
			size: 20,
			number: 1,
			totalElements: 1,
			totalPages: 1,
		},
	};

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
			expect((res.content[0] as PitcherGlobal).name).toBe('Gerrit Cole');
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
			expect((res.content[0] as TeamInfo).teamStats.name).toBe('New York Yankees');
			expect((res.content[0] as TeamInfo).teamStats.abbreviation).toBe('NYY');
			expect(res.page.number).toBe(0);
			expect(res.page.totalPages).toBe(1);
		});

		const req = httpMock.expectOne(`${apiUrl}/team?query=new%20york%20yankees&page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockTeamResponse);
	});

	it('should search for stadiums', () => {
		service.search('stadium', 'yankee stadium', 1, 20).subscribe((res) => {
			expect((res.content[0] as Stadium).name).toBe('Yankee Stadium');
			expect(res.page.size).toBe(20);
			expect(res.page.number).toBe(1);
			expect(res.page.totalPages).toBe(1);
		});

		const req = httpMock.expectOne(`${apiUrl}/stadium?query=yankee%20stadium&page=1&size=20`);
		expect(req.request.method).toBe('GET');
		req.flush(mockStadiumResponse);
	});
});