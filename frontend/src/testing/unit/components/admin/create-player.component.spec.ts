import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreatePlayerComponent } from '../../../../app/components/admin/create-player/create-player.component';
import { PlayerService } from '../../../../app/services/player.service';
import { TeamService } from '../../../../app/services/team.service';
import { PaginatedSelectorService } from '../../../../app/services/utilities/paginated-selector.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TeamSummary } from '../../../../app/models/team.model';
import { PositionPlayerGlobal } from '../../../../app/models/position-player.model';
import { MockFactory } from '../../../utils/mock-factory';
import { PitcherGlobal } from '../../../../app/models/pitcher.model';

describe('Create Player Component Tests', () => {
	let component: CreatePlayerComponent;
	let fixture: ComponentFixture<CreatePlayerComponent>;
	let playerServiceSpy: jasmine.SpyObj<PlayerService>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let routerSpy: jasmine.SpyObj<Router>;
	let selectorService: PaginatedSelectorService<TeamSummary>;

	beforeEach(() => {
		playerServiceSpy = jasmine.createSpyObj('PlayerService', [
			'createPitcher',
			'createPositionPlayer',
		]);
		teamServiceSpy = jasmine.createSpyObj('TeamService', ['getAvailableTeams']);
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);
		selectorService = new PaginatedSelectorService<TeamSummary>();

		TestBed.configureTestingModule({
			imports: [CreatePlayerComponent],
			providers: [
				{ provide: PlayerService, useValue: playerServiceSpy },
				{ provide: TeamService, useValue: teamServiceSpy },
				{ provide: PaginatedSelectorService, useValue: selectorService },
				{ provide: Router, useValue: routerSpy },
			],
		});

		fixture = TestBed.createComponent(CreatePlayerComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should build request correctly', () => {
		component.nameInput = 'Gleyber Torres';
		component.playerNumberInput = 25;
		component.teamNameInput = 'Detroit Tigers';
		component.positionInput = '2B';
		const request = (component as any).buildRequest();
		expect(request).toEqual({
			name: 'Gleyber Torres',
			playerNumber: 25,
			teamName: 'Detroit Tigers',
			position: '2B',
		});
	});

	it('should create pitcher when position is SP', () => {
		const mockPitcher: PitcherGlobal = MockFactory.buildPitcherGlobalMock(
			'Gerrit Cole',
			'Yankees',
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
			{ url: '', publicId: '' }
		);
		component.positionInput = 'SP';
		playerServiceSpy.createPitcher.and.returnValue(of(mockPitcher));
		component.createPlayer();
		expect(playerServiceSpy.createPitcher).toHaveBeenCalled();
		expect(component.success).toBeTrue();
		expect(component.successMessage).toContain('successfully created');
	});

	it('should create position player when position is SS', () => {
		component.positionInput = 'SS';
		const mockPositionPlayer: PositionPlayerGlobal = MockFactory.buildPositionPlayerGlobalMock(
			'Aaron Judge',
			'New York Yankees',
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
			{ url: '', publicId: '' }
		);
		playerServiceSpy.createPositionPlayer.and.returnValue(of(mockPositionPlayer));
		component.createPlayer();
		expect(playerServiceSpy.createPositionPlayer).toHaveBeenCalled();
		expect(component.success).toBeTrue();
		expect(component.successMessage).toContain('successfully created');
	});

	it('should handle error in createPlayerByType', () => {
		component.positionInput = 'SP';
		playerServiceSpy.createPitcher.and.returnValue(
			throwError(() => ({ error: { message: 'Service failed' } }))
		);
		component.createPlayer();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('Service failed');
	});

	it('should show teams modal and reset selector', () => {
		spyOn(selectorService, 'reset');
		spyOn(selectorService, 'loadPage');
		component.showTeamsModal();
		expect(component.selectTeamButtonClicked).toBeTrue();
		expect(selectorService.reset).toHaveBeenCalled();
		expect(selectorService.loadPage).toHaveBeenCalled();
	});

	it('should close team modal on Escape key', () => {
		component.selectTeamButtonClicked = true;
		component.handleEscape(new KeyboardEvent('keydown', { key: 'Escape' }));
		expect(component.isClose).toBeTrue();
	});

	it('should select team and update teamNameInput', () => {
		const team: TeamSummary = MockFactory.buildTeamSummaryMock(
			'Los Angeles Dodgers',
			'LAD',
			'NL',
			'WEST'
		);
		spyOn(selectorService, 'select').and.returnValue(team.name);
		component.selectTeam(team);
		expect(component.selectedTeam).toBeTrue();
		expect(component.successMessage).toBe('Team Selected');
		expect(component.teamNameInput).toBe('Los Angeles Dodgers');
	});

	it('should load next page of teams', () => {
		spyOn(selectorService, 'loadNextPage');
		component.loadNextPage();
		expect(selectorService.loadNextPage).toHaveBeenCalled();
	});

	it('should navigate to home on returnToHome', () => {
		component.returnToHome();
		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
	});
});