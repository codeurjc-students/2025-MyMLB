import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DropdownMenuComponent } from '../../../../app/components/navbar/dropdown-menu/dropdown-menu.component';
import { TeamService } from '../../../../app/services/team.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { Router } from '@angular/router';
import { MockFactory } from '../../../utils/mock-factory';
import { of, throwError } from 'rxjs';
import { SimpliefiedTeam } from '../../../../app/services/team.service';
import { TeamInfo } from '../../../../app/models/team-info.model';

describe('Dropdown Menu Component Tests', () => {
	let component: DropdownMenuComponent;
	let fixture: ComponentFixture<DropdownMenuComponent>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;
	let selectedTeamServiceSpy: jasmine.SpyObj<SelectedTeamService>;
	let routerSpy: jasmine.SpyObj<Router>;

	const mockTeams: SimpliefiedTeam[] = [
		{ name: 'Yankees', abbreviation: 'NYY', league: 'AL', division: 'East' },
		{ name: 'Red Sox', abbreviation: 'BOS', league: 'AL', division: 'East' },
	];

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		MockFactory.buildTeamMocks('Yankees', 'NYY', 'AL', 'East', 162, 100, 62, 0.617, 0, '7-3'),
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		MockFactory.buildStadiumMock('Yankee Stadium', 2009),
		[
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
		],
		[
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
		]
	);

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [DropdownMenuComponent],
			providers: [
				{
					provide: TeamService,
					useValue: jasmine.createSpyObj('TeamService', [
						'getTeamsNamesAndAbbr',
						'getTeamInfo',
					]),
				},
				{
					provide: BackgroundColorService,
					useValue: jasmine.createSpyObj('BackgroundColorService', [
						'getBackgroundColor',
					]),
				},
				{
					provide: SelectedTeamService,
					useValue: jasmine.createSpyObj('SelectedTeamService', ['setSelectedTeam']),
				},
				{ provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
			],
		});

		fixture = TestBed.createComponent(DropdownMenuComponent);
		component = fixture.componentInstance;

		teamServiceSpy = TestBed.inject(TeamService) as jasmine.SpyObj<TeamService>;
		backgroundServiceSpy = TestBed.inject(
			BackgroundColorService
		) as jasmine.SpyObj<BackgroundColorService>;
		selectedTeamServiceSpy = TestBed.inject(
			SelectedTeamService
		) as jasmine.SpyObj<SelectedTeamService>;
		routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
	});

	it('should load teams on init', () => {
		teamServiceSpy.getTeamsNamesAndAbbr.and.returnValue(of(mockTeams));
		component.ngOnInit();
		expect(component.teams).toEqual(mockTeams);
		expect(component.errorMessage).toBe('');
	});

	it('should set errorMessage if getTeamsNamesAndAbbr fails', () => {
		teamServiceSpy.getTeamsNamesAndAbbr.and.returnValue(throwError(() => new Error('fail')));
		component.ngOnInit();
		expect(component.errorMessage).toBe('Error loading the team info');
	});

	it('should select team and navigate on success', () => {
		teamServiceSpy.getTeamInfo.and.returnValue(of(mockTeamInfo));
		component.selectTeam('Yankees');
		expect(selectedTeamServiceSpy.setSelectedTeam).toHaveBeenCalledWith(mockTeamInfo);
		expect(routerSpy.navigate).toHaveBeenCalledWith(['team', 'Yankees']);
	});

	it('should set errorMessage on selectTeam failure', () => {
		teamServiceSpy.getTeamInfo.and.returnValue(throwError(() => new Error('Team Not Found')));
		component.selectTeam('UnknownTeam');
		expect(component.errorMessage).toBe('Team Not Found');
	});

	it('should return background color from service', () => {
		backgroundServiceSpy.getBackgroundColor.and.returnValue('bg-blue-900');
		const result = component.loadBackgroundColor('NYY');
		expect(result).toBe('bg-blue-900');
	});
});