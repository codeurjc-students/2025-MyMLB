import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TeamService } from '../../../../app/services/team.service';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Team } from '../../../../app/models/team.model';
import { MockFactory } from '../../../utils/mock-factory';
import { TeamInfo } from '../../../../app/models/team-info.model';

describe('Team Service Tests', () => {
	let service: TeamService;
	let httpMock: HttpTestingController;
	const apiURL = 'https://localhost:8443/api/teams';

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

	const teamStats = MockFactory.buildTeamMocks(
		'Yankees',
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

	const stadium = MockFactory.buildStadiumMock('Yankee Stadium', 2009);
	const positionPlayers = [
		MockFactory.buildPositionPlayerMock(
			'Aaron Judge',
			'RF',
			500,
			80,
			150,
			30,
			2,
			45,
			110,
			0.3,
			0.4,
			1.0,
			0.6
		),
	];

	const pitchers = [
		MockFactory.buildPitcherMock(
			'Gerrit Cole',
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
		),
	];

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		teamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		stadium,
		positionPlayers,
		pitchers
	);

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

		const req = httpMock.expectOne(`${apiURL}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should fetch team info by name', () => {
		service.getTeamInfo('Yankees').subscribe((response) => {
			expect(response).toEqual(mockTeamInfo);
		});

		const req = httpMock.expectOne(`${apiURL}/Yankees`);
		expect(req.request.method).toBe('GET');
		req.flush(mockTeamInfo);
	});

	it('should return simplified team list sorted by name', () => {
		service.getTeamsNamesAndAbbr().subscribe((teams) => {
			expect(teams.length).toBe(4);
			expect(teams[0].name).toBe('Astros');
			expect(teams[3].name).toBe('Yankees');
			expect(teams[0].abbreviation).toBe('HOU');
			expect(teams[2].league).toBe('American');
			expect(teams[3].division).toBe('East');
		});

		const req = httpMock.expectOne(`${apiURL}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should return correct division rank for a team', () => {
		service.getTeamDivisionRank('BOS').subscribe((rank) => {
			expect(rank).toBe(2);
		});

		const req = httpMock.expectOne(`${apiURL}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should return -1 if team abbreviation is not found', () => {
		service.getTeamDivisionRank('XYZ').subscribe((rank) => {
			expect(rank).toBe(-1);
		});

		const req = httpMock.expectOne(`${apiURL}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});
});