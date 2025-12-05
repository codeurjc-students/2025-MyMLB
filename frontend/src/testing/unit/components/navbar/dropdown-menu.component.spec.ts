import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DropdownMenuComponent } from '../../../../app/components/navbar/dropdown-menu/dropdown-menu.component';
import { TeamService } from '../../../../app/services/team.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TeamSummary } from '../../../../app/models/team.model';

describe('Dropdown Menu Component Tests', () => {
	let component: DropdownMenuComponent;
	let fixture: ComponentFixture<DropdownMenuComponent>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;
	let selectedTeamServiceSpy: jasmine.SpyObj<SelectedTeamService>;
	let routerSpy: jasmine.SpyObj<Router>;

	const mockTeams: TeamSummary[] = [
		{ name: 'Yankees', abbreviation: 'NYY', league: 'AL', division: 'East' },
		{ name: 'Red Sox', abbreviation: 'BOS', league: 'AL', division: 'East' },
	];

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [DropdownMenuComponent],
			providers: [
				{
					provide: TeamService,
					useValue: jasmine.createSpyObj('TeamService', [
						'getTeamsNamesAndAbbr'
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
});