import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TeamComponent } from '../../app/components/team/team.component';
import { SelectedTeamService } from '../../app/services/selected-team.service';
import { TeamService } from '../../app/services/team.service';
import { BackgroundColorService } from '../../app/services/background-color.service';
import { MockFactory } from '../utils/mock-factory';
import { of } from 'rxjs';
import { TeamInfo } from '../../app/models/team-info.model';

describe('Team Component Integration Tests', () => {
	let component: TeamComponent;
	let fixture: ComponentFixture<TeamComponent>;

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
			imports: [TeamComponent],
			providers: [
				{
					provide: SelectedTeamService,
					useValue: {
						selectedTeam$: of(mockTeamInfo),
					},
				},
				{
					provide: TeamService,
					useValue: {
						getTeamDivisionRank: () => of(1),
					},
				},
				{
					provide: BackgroundColorService,
					useValue: {
						getBackgroundColor: (abbr: string) => `bg-${abbr?.toLowerCase()}`,
					},
				},
			],
		});

		fixture = TestBed.createComponent(TeamComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should return background class from service', () => {
		const result = component.backgroundLogo('NYY');
		expect(result).toBe('bg-nyy');
	});

	it('should select a player', () => {
		const player = component.positionPlayers[0];
		component.selectPlayer(player);
		expect(component.selectedPlayer).toEqual(player);
	});

	it('should scroll carousel to the right', fakeAsync(() => {
		const initialIndex = component.currentIndex;
		component.scrollCarousel('right');
		tick(300);
		expect(component.currentIndex).toBe((initialIndex + 1) % component.stadiumImages.length);
	}));

	it('should scroll carousel to the left', fakeAsync(() => {
		component.currentIndex = 1;
		component.scrollCarousel('left');
		tick(300);
		expect(component.currentIndex).toBe(0);
	}));

	it('should go to specific slide index', fakeAsync(() => {
		component.goToSlide(3);
		tick(300);
		expect(component.currentIndex).toBe(3);
	}));

	it('should clear interval on destroy', () => {
		const clearSpy = spyOn(window, 'clearInterval');
		component.ngOnDestroy();
		expect(clearSpy).toHaveBeenCalled();
	});
});