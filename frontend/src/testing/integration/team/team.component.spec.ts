import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TeamComponent } from '../../../app/components/team/team.component';
import { SelectedTeamService } from '../../../app/services/selected-team.service';
import { TeamService } from '../../../app/services/team.service';
import { BackgroundColorService } from '../../../app/services/background-color.service';
import { MockFactory } from '../../utils/mock-factory';
import { of } from 'rxjs';
import { TeamInfo } from '../../../app/models/team.model';
import { provideHttpClient } from '@angular/common/http';
import { EventService } from '../../../app/services/ticket/event.service';
import { Router } from '@angular/router';
import { MatchService } from '../../../app/services/match.service';
import { AuthService } from '../../../app/services/auth.service';

describe('Team Component Integration Tests', () => {
	let component: TeamComponent;
	let fixture: ComponentFixture<TeamComponent>;

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		MockFactory.buildTeamMocks('Yankees', 'NYY', 'AL', 'East', 162, 100, 62, 0.617, 0, '7-3'),
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		MockFactory.buildStadiumMock('Yankee Stadium', 2009,  [{ url: '', publicId: '' }]),
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
				0.6,
				{url: '', publicId: ''}
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
				0,
				{url: '', publicId: ''}
			),
		]
	);

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [TeamComponent],
			providers: [
				provideHttpClient(),
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
				{
					provide: AuthService,
					useValue: {
						getActiveUser: () => of({ roles: ['ADMIN'] })
					},
				},
				{
					provide: EventService,
					useValue: {
						getEventByMatchId: () => of(null),
						deleteEvent: () => of({})
					},
				},
				{
					provide: MatchService,
					useValue: {
						getMatchesOfATeam: () => of([])
					},
				},
			],
		});

		fixture = TestBed.createComponent(TeamComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
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

	it('should navigate to create event page with matchId', () => {
        const routerSpy = spyOn(TestBed.inject(Router), 'navigate');
        const matchId = 123;

        component.createEvent(matchId);

        expect(routerSpy).toHaveBeenCalledWith(['create-event'], {
            queryParams: { matchId: matchId }
        });
    });

    it('should navigate to edit event page with eventId from the map', () => {
        const routerSpy = spyOn(TestBed.inject(Router), 'navigate');
        const matchId = 1;
        component.eventMap.set(matchId, { id: 500 } as any);

        component.editEvent(matchId);

        expect(routerSpy).toHaveBeenCalledWith(['edit-event'], {
            queryParams: { eventId: 500 }
        });
    });
});