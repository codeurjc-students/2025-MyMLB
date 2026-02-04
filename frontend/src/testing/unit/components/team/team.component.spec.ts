import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TeamComponent } from '../../../../app/components/team/team.component';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { TeamService } from '../../../../app/services/team.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { MockFactory } from '../../../utils/mock-factory';
import { of, throwError } from 'rxjs';
import { TeamInfo } from '../../../../app/models/team.model';
import { provideHttpClient } from '@angular/common/http';
import { MatchService, ShowMatch } from '../../../../app/services/match.service';
import { EventService } from '../../../../app/services/ticket/event.service';
import { AuthService } from '../../../../app/services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { PaginatedSelectorService } from '../../../../app/services/utilities/paginated-selector.service';
import { Pictures } from '../../../../app/models/pictures.model';

describe('Team Component Tests', () => {
    let component: TeamComponent;
    let fixture: ComponentFixture<TeamComponent>;
    let matchServiceSpy: jasmine.SpyObj<MatchService>;
    let eventServiceSpy: jasmine.SpyObj<EventService>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;
	let routeSpy: any;
    let paginationService: PaginatedSelectorService<ShowMatch>;

    const mockTeamStats = MockFactory.buildTeamMocks(
        'New York Yankees', 'NYY', 'AL', 'EAST', 162, 100, 62, 0.617, 0, '7-3'
    );

    const mockStadium = MockFactory.buildStadiumMock('Yankee Stadium', 2009, [{ url: 'http://fake-url/image.png', publicId: 'image' }]);

    const mockPlayers = [
        MockFactory.buildPositionPlayerMock('Aaron Judge', 'RF', 500, 80, 150, 30, 2, 45, 110, 0.3, 0.4, 1.0, 0.6, {url: '', publicId: ''}),
    ];

    const mockPitchers = [
        MockFactory.buildPitcherMock('Gerrit Cole', 'SP', 30, 15, 5, 2.5, 200, 220, 40, 150, 60, 1.05, 0, 0, {url: '', publicId: ''}),
    ];

    const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
        mockTeamStats, 'New York', 'Founded in 1901', [1903, 1923, 1996, 2009], mockStadium, mockPlayers, mockPitchers
    );

	const sectorId = 1;
	const eventId = 2;
	const matchId = 1;
	const managerId = 1;

	const mockEventManager = MockFactory.buildEventManager(managerId, sectorId, 'Test Sector', 100, 100, 100);
	const mockPicture: Pictures = {
		url: 'https://test-url',
		publicId: 'test'
	}
	const mockEvent = MockFactory.buildEventResponse(eventId, 'Boston Red Sox', 'New York Yankees', 'NYY', 'Yankee Stadium', new Date(), mockPicture, [mockEventManager]);

    beforeEach(() => {
        matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchesOfATeam']);
        eventServiceSpy = jasmine.createSpyObj('EventService', ['getEventByMatchId', 'deleteEvent']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['getActiveUser']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);
        paginationService = new PaginatedSelectorService<ShowMatch>();

		const paramMapSpy = jasmine.createSpyObj('paramMap', ['get']);
		paramMapSpy.get.and.returnValue('New York Yankees');
		routeSpy = {
			snapshot: {
				paramMap: paramMapSpy
			}
		}

        authServiceSpy.getActiveUser.and.returnValue(of({ username: 'testUser', roles: ['ADMIN'] }));
        eventServiceSpy.getEventByMatchId.and.returnValue(of(mockEvent));

        TestBed.configureTestingModule({
            imports: [TeamComponent],
            providers: [
                provideHttpClient(),
                { provide: SelectedTeamService, useValue: { selectedTeam$: of(mockTeamInfo) } },
                { provide: TeamService, useValue: { getTeamDivisionRank: () => of(1) } },
                { provide: BackgroundColorService, useValue: { getBackgroundColor: (abbr: string) => `bg-${abbr?.toLowerCase()}` } },
                { provide: MatchService, useValue: matchServiceSpy },
                { provide: EventService, useValue: eventServiceSpy },
                { provide: AuthService, useValue: authServiceSpy },
                { provide: Router, useValue: routerSpy },
				{ provide: ActivatedRoute, useValue: routeSpy },
                { provide: PaginatedSelectorService, useValue: paginationService }
            ],
        });

        fixture = TestBed.createComponent(TeamComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should initialize team data from SelectedTeamService', () => {
        expect(component.team).toEqual(mockTeamInfo);
        expect(component.positionPlayers).toEqual(mockPlayers);
        expect(component.pitchers).toEqual(mockPitchers);
        expect(component.championships).toEqual([1903, 1923, 1996, 2009]);
        expect(component.teamRank).toBe(1);
    });

    it('should update selectedPlayer when selectPlayer is called', () => {
        component.selectPlayer(mockPlayers[0]);
        expect(component.selectedPlayer).toEqual(mockPlayers[0]);
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
        component.stadiumImages = new Array(5).fill({ url: '', publicId: '' });
        fixture.detectChanges();
        component.goToSlide(3);
        tick(300);
        expect(component.currentIndex).toBe(3);
    }));

    it('should clear interval on destroy', () => {
        const clearSpy = spyOn(window, 'clearInterval');
        component.ngOnDestroy();
        expect(clearSpy).toHaveBeenCalled();
    });

    it('should navigate to purchase tickets', () => {
        component.goToPurchaseTickets(101);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['tickets'], { queryParams: { matchId: 101 } });
    });

    it('should navigate to create event', () => {
        component.createEvent(101);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['create-event'], { queryParams: { matchId: 101 } });
    });

    it('should call deleteEvent and update map on success', () => {
        component.eventMap.set(matchId, { id: 500 } as any);
        eventServiceSpy.deleteEvent.and.returnValue(of(mockEvent));

        component.deleteEvent(matchId);

        expect(eventServiceSpy.deleteEvent).toHaveBeenCalledWith(500);
        expect(component.eventMap.get(matchId)).toBeNull();
        expect(component.success).toBeTrue();
    });

    it('should show error when deleting non-existent event', () => {
        component.deleteEvent(999);
        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('The event does not exists');
    });

    it('should load next matches using matchService', () => {
        const loadSpy = spyOn(paginationService, 'loadNextPage');
        component.team = mockTeamInfo;

        component.loadNextMatches();

        expect(loadSpy).toHaveBeenCalled();
        const fetchFn = loadSpy.calls.mostRecent().args[1];
        matchServiceSpy.getMatchesOfATeam.and.returnValue(of([] as any));

        fetchFn(0, 5);
        expect(matchServiceSpy.getMatchesOfATeam).toHaveBeenCalledWith(mockTeamInfo.teamStats.name, 'home', 0, 5);
    });

    it('should check events when matches are loaded', () => {
        const mockMatch = { id: 202 } as any;
        const eventResponse = { id: 88 } as any;
        eventServiceSpy.getEventByMatchId.and.returnValue(of(eventResponse));

        (paginationService.items$ as any).next([mockMatch]);

        expect(eventServiceSpy.getEventByMatchId).toHaveBeenCalledWith(202);
        expect(component.eventMap.get(202)).toEqual(eventResponse);
    });

    it('should handle error when checking events', () => {
        eventServiceSpy.getEventByMatchId.and.returnValue(throwError(() => new Error()));

        (paginationService.items$ as any).next([{ id: 303 } as any]);

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain('303');
    });
});