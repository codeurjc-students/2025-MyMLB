import { TestBed } from '@angular/core/testing';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { Team } from '../../../../app/models/team.model';
import { MockFactory } from '../../../utils/mock-factory';
import { StadiumSummary } from '../../../../app/models/stadium-summary.model';
import { PositionPlayer } from '../../../../app/models/position-player.model';
import { Pitcher } from '../../../../app/models/pitcher.model';
import { TeamInfo } from '../../../../app/models/team-info.model';

describe('Selected Team Service Tests', () => {
	let service: SelectedTeamService;

	const teamStats: Team = MockFactory.buildTeamMocks(
		'New York Yankees',
		'NYY',
		'AL',
		'EAST',
		162,
		100,
		62,
		0.617,
		0,
		'7-3'
	);

	const stadium: StadiumSummary = MockFactory.buildStadiumMock('Yankee Stadium', 2009);

	const positionPlayers: PositionPlayer[] = [
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

	const pitchers: Pitcher[] = [
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

	const mockTeam: TeamInfo = MockFactory.buildTeamInfoMock(
		teamStats,
		'New York',
		'Founded in 1901...',
		[1903, 1923, 1996, 2009],
		stadium,
		positionPlayers,
		pitchers
	);

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(SelectedTeamService);
	});

	it('should emit selected team when setSelectedTeam is called', (done) => {
		service.setSelectedTeam(mockTeam);

		service.selectedTeam$.subscribe((team) => {
			expect(team).toEqual(mockTeam);
			done();
		});
	});

	it('should emit null when clearSelectedTeam is called', (done) => {
		service.setSelectedTeam(mockTeam);
		service.clearSelectedTeam();

		service.selectedTeam$.subscribe((team) => {
			expect(team).toBeNull();
			done();
		});
	});
});