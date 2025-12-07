import { Pictures } from './../../../../app/models/pictures.model';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditPlayerComponent } from '../../../../app/components/admin/edit-player/edit-player.component';
import { PlayerService } from '../../../../app/services/player.service';
import { TeamService } from '../../../../app/services/team.service';
import { EntityFormMapperService } from '../../../../app/services/utilities/entity-form-mapper.service';
import { PaginatedSelectorService } from '../../../../app/services/utilities/paginated-selector.service';
import { of, throwError } from 'rxjs';
import { PitcherGlobal } from './../../../../app/models/pitcher.model';
import { TeamSummary } from '../../../../app/models/team.model';
import { PositionPlayerGlobal } from '../../../../app/models/position-player.model';
import { MockFactory } from '../../../utils/mock-factory';

describe('Edit Player Component Tests', () => {
	let component: EditPlayerComponent;
	let fixture: ComponentFixture<EditPlayerComponent>;
	let playerServiceSpy: jasmine.SpyObj<PlayerService>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let selectorService: PaginatedSelectorService<TeamSummary>;

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
		{url: '', publicId: ''}
	);

	beforeEach(() => {
		playerServiceSpy = jasmine.createSpyObj('PlayerService', [
			'updatePositionPlayer',
			'updatePitcher',
			'updatePicture',
			'deletePlayer',
		]);
		teamServiceSpy = jasmine.createSpyObj('TeamService', ['getAvailableTeams']);
		selectorService = new PaginatedSelectorService<TeamSummary>();

		TestBed.configureTestingModule({
			imports: [EditPlayerComponent],
			providers: [
				EntityFormMapperService,
				{ provide: PlayerService, useValue: playerServiceSpy },
				{ provide: TeamService, useValue: teamServiceSpy },
				{ provide: PaginatedSelectorService, useValue: selectorService },
			],
		});

		fixture = TestBed.createComponent(EditPlayerComponent);
		component = fixture.componentInstance;
		component.player = mockPositionPlayer;
		fixture.detectChanges();
	});

	it('should hydrate form on init', () => {
		spyOn(component, 'hydrateForm');
		component.ngOnInit();
		expect(component.hydrateForm).toHaveBeenCalled();
	});

	it('should show teams modal and reset selector', () => {
		spyOn(selectorService, 'reset');
		spyOn(selectorService, 'loadPage');
		component.showTeamsModal();
		expect(component.selectTeamButtonClicked).toBeTrue();
		expect(selectorService.reset).toHaveBeenCalled();
		expect(selectorService.loadPage).toHaveBeenCalled();
	});

	it('should select a team and update formInputs', () => {
		const team: TeamSummary = MockFactory.buildTeamSummaryMock('Los Angeles Dodgers', 'LAD', 'NL', 'WEST');
		spyOn(selectorService, 'select').and.returnValue(team.name);
		component.selectTeam(team);
		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Team Selected');
		expect(component.formInputs.teamName).toBe('Los Angeles Dodgers');
	});

	it('should upload picture successfully', () => {
		const mockFile = new File([''], 'pic.webp', { type: 'image/webp' });
		const mockResponse: Pictures = { url: 'http://some-url', publicId: 'some-id' };
		playerServiceSpy.updatePicture.and.returnValue(of(mockResponse));
		component.uploadPicture('stadium', mockFile);
		expect(playerServiceSpy.updatePicture).toHaveBeenCalled();
	});

	it('should set error when uploading non-webp picture', () => {
		const mockFile = new File([''], 'pic.png', { type: 'image/png' });
		component.uploadPicture('stadium', mockFile);
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('Only .webp images are allowed');
	});

	it('should delete player successfully', () => {
		playerServiceSpy.deletePlayer.and.returnValue(of({}));
		component.deletePlayer();
		expect(playerServiceSpy.deletePlayer).toHaveBeenCalledWith('Aaron Judge');
		expect(component.success).toBeTrue();
	});

	it('should handle delete player error', () => {
		playerServiceSpy.deletePlayer.and.returnValue(throwError(() => new Error('fail')));
		component.deletePlayer();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occurred during the deletion process');
	});

	it('should emit backToMenu event', () => {
		spyOn(component.backToMenu, 'emit');
		component.goToEditMenu();
		expect(component.backToMenu.emit).toHaveBeenCalled();
	});
});