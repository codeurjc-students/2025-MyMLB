import { RunStats, UpdateTeamRequest, WinsDistribution, WinsPerRival } from './../../../../app/models/team.model';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TeamService } from '../../../../app/services/team.service';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Team } from '../../../../app/models/team.model';
import { MockFactory } from '../../../utils/mock-factory';
import { TeamInfo } from '../../../../app/models/team.model';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { Router } from '@angular/router';
import { AuthResponse } from '../../../../app/models/auth.model';

describe('Team Service Tests', () => {
	let service: TeamService;
	let httpMock: HttpTestingController;
	let mockSelectedTeamService: jasmine.SpyObj<SelectedTeamService>;
  	let mockRouter: jasmine.SpyObj<Router>;

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
		'.617',
		0,
		'7-3'
	);

	const stadium = MockFactory.buildStadiumMock('Yankee Stadium', 2009,  [{ url: '', publicId: '' }]);
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
			0.6,
			{url: '', publicId: ''}
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
			0,
			{url: '', publicId: ''}
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
		mockSelectedTeamService = jasmine.createSpyObj('SelectedTeamService', ['setSelectedTeam']);
   	 	mockRouter = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			providers: [
				TeamService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				{ provide: SelectedTeamService, useValue: mockSelectedTeamService },
				{ provide: Router, useValue: mockRouter }
			],
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

		const req = httpMock.expectOne(`${service['url']}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should fetch all available teams', () => {
		const mockTeamSummary = MockFactory.buildTeamSummaryMock('Team1', 'T1', 'AL', 'WEST');
		const mockResponse = MockFactory.buildPaginatedResponse(mockTeamSummary);

		service.getAvailableTeams(0, 10).subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const req = httpMock.expectOne(`${service['url']}/available?page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should fetch team info by name', () => {
		service.getTeamInfo('Yankees').subscribe((response) => {
			expect(response).toEqual(mockTeamInfo);
		});

		const req = httpMock.expectOne(`${service['url']}/Yankees`);
		expect(req.request.method).toBe('GET');
		req.flush(mockTeamInfo);
	});

	it('should fetch team info, set selected team, and navigate', () => {
		service.selectTeam('Yankees').subscribe((response) => {
			expect(response).toEqual(mockTeamInfo);
			expect(mockSelectedTeamService.setSelectedTeam).toHaveBeenCalledWith(mockTeamInfo);
			expect(mockRouter.navigate).toHaveBeenCalledWith(['team', 'Yankees']);
		});

		const req = httpMock.expectOne(`${service['url']}/Yankees`);
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

		const req = httpMock.expectOne(`${service['url']}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should return correct division rank for a team', () => {
		service.getTeamDivisionRank('BOS').subscribe((rank) => {
			expect(rank).toBe(2);
		});

		const req = httpMock.expectOne(`${service['url']}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	it('should return -1 if team abbreviation is not found', () => {
		service.getTeamDivisionRank('XYZ').subscribe((rank) => {
			expect(rank).toBe(-1);
		});

		const req = httpMock.expectOne(`${service['url']}/standings`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

    it('should fetch rivals of a certain team', () => {
        const mockRivals: Team[] = [
            { name: 'Boston Red Sox', abbreviation: 'BOS', league: 'AL', division: 'East' } as Team,
            { name: 'Baltimore Orioles', abbreviation: 'BAL', league: 'AL', division: 'East' } as Team
        ];

        service.getRivals('New York Yankees').subscribe((response) => {
            expect(response).toEqual(mockRivals);
            expect(response.length).toBe(2);
        });

        const req = httpMock.expectOne(`${service['url']}/New York Yankees/rivals`);
        expect(req.request.method).toBe('GET');
        req.flush(mockRivals);
    });

    it('should fetch wins per rivals', () => {
        const mockWins: WinsPerRival[] = [
            { rivalTeamName: 'Boston Red Sox', wins: 5, gamesPlayed: 10 },
            { rivalTeamName: 'Baltimore Orioles', wins: 7, gamesPlayed: 10 }
        ];
        const rivals = ['Boston Red Sox', 'Ba<ltimore Orioles'];

        service.getWinsPerRivals('New York Yankees', rivals).subscribe((response) => {
            expect(response).toEqual(mockWins);
        });

        const req = httpMock.expectOne(req =>
            req.url === `${service['url']}/New York Yankees/analytics/wins-per-rival` &&
            req.params.getAll('rivalTeamNames')?.length === 2
        );

        expect(req.request.method).toBe('GET');
        expect(req.request.params.get('rivalTeamNames')).toBe('Boston Red Sox');
        req.flush(mockWins);
    });

    it('should fetch runs stats per rival', () => {
        const mockRunStats: RunStats[] = [
            { teamName: 'New York Yankees', runsScored: 50, runsAllowed: 40 },
            { teamName: 'Boston Red Sox', runsScored: 40, runsAllowed: 50 }
        ];
        const teams = ['New York Yankees', 'Boston Red Sox'];

        service.getRunsStatsPerRival(teams).subscribe((response) => {
            expect(response).toEqual(mockRunStats);
        });

        const req = httpMock.expectOne(req =>
            req.url === `${service['url']}/analytics/runs-per-rival` &&
            req.params.getAll('teams')?.length === 2
        );

        expect(req.request.method).toBe('GET');
        req.flush(mockRunStats);
    });

    it('should fetch win distribution only with teams', () => {
        const mockDistribution: WinsDistribution = {
			teamName: 'New York Yankees',
            homeWins: 50,
            roadWins: 40,
            homeGames: 81,
            roadGames: 81,
            homeWinPct: 0.617,
            roadWinPct: 0.493
        };

        service.getWinDistribution('New York Yankees').subscribe((response) => {
            expect(response).toEqual(mockDistribution);
        });

        const req = httpMock.expectOne(`${service['url']}/New York Yankees/analytics/win-distribution`);
        expect(req.request.method).toBe('GET');
        req.flush(mockDistribution);
    });

    it('should fetch historic ranking with teams and dateFrom params', () => {
        const mockHistoricRanking = {
            'New York Yankees': [
                { matchDate: '2023-01-01', rank: 1 },
                { matchDate: '2023-02-01', rank: 2 }
            ]
        };
        const teams = ['New York Yankees'];
        const dateFrom = '2023-01-01';

        service.getHistoricRanking(teams, dateFrom).subscribe((response) => {
            expect(response['New York Yankees'][0].rank).toBe(1);
        });

        const req = httpMock.expectOne(req =>
            req.url === `${service['url']}/analytics/historic-ranking` &&
            req.params.get('dateFrom') === '2023-01-01' &&
            req.params.get('teams') === 'New York Yankees'
        );

        expect(req.request.method).toBe('GET');
        req.flush(mockHistoricRanking);
    });

	it('should update the team successfully', () => {
		const request: UpdateTeamRequest = {
			city: 'New City',
			newInfo: 'New Info'
		};

		const response: AuthResponse = {
			status: 'SUCCESS',
			message: 'Team successfully updated'
		};

		service.updateTeam(teamStats.name, request).subscribe((response) => {
			expect(response.status).toBe('SUCCESS');
		});

		const req = httpMock.expectOne(`${service['url']}/${teamStats.name}`);
		expect(req.request.method).toBe('PATCH');
		req.flush(response);
	});
});