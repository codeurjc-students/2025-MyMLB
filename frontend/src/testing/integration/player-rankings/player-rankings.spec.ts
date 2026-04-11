import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { PlayerRankingsComponent } from '../../../app/components/player-rankings/player-rankings/player-rankings.component';
import { PlayerService } from '../../../app/services/player.service';
import { TeamService } from '../../../app/services/team.service';
import { BackgroundColorService } from '../../../app/services/background-color.service';
import { AuthService } from '../../../app/services/auth.service';

Chart.register(...registerables);

describe('Player Rankings Component Integration Test', () => {
    let fixture: ComponentFixture<PlayerRankingsComponent>;
    let component: PlayerRankingsComponent;
    let httpMock: HttpTestingController;
	let authServiceMock: any;
    let queryParamsSubject: BehaviorSubject<any>;

    const playersUrl = '/api/v1/players';
    const teamsUrl = '/api/v1/teams';

    const mockStandingsResponse = {
        'AL': { 'EAST': [{ name: 'Baltimore Orioles', abbreviation: 'BAL' }] }
    };

    const mockRankings = { average: [], homeRuns: [] };

    beforeEach(() => {
        queryParamsSubject = new BehaviorSubject<any>({ playerType: 'position' });
		authServiceMock = {
            currentUser$: new BehaviorSubject({ username: 'test-user', roles: ['USER'] }),
        };

        TestBed.configureTestingModule({
            imports: [PlayerRankingsComponent],
            providers: [
                PlayerService,
                TeamService,
                BackgroundColorService,
				{ provide: AuthService, useValue: authServiceMock },
                provideHttpClient(),
                provideHttpClientTesting(),
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: queryParamsSubject.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(PlayerRankingsComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

	/**
	 * Helper method that cleans the default initial requests.
	 *
	 * @param playerType position or pitcher
	 */
    const flushInitialRequests = (playerType = 'position') => {
        const reqRank = httpMock.expectOne(req =>
            req.url === `${playersUrl}/rankings/all` && req.params.get('playerType') === playerType
        );
        reqRank.flush(mockRankings);

        const reqTeams = httpMock.expectOne(req => req.url === `${teamsUrl}/standings`);
        reqTeams.flush(mockStandingsResponse);
    };

    it('should initialize the component', () => {
        fixture.detectChanges();
        flushInitialRequests('position');
        expect(component.allTeams.length).toBe(1);
    });

	it('should reload dashboard when playerType changes in URL', () => {
		fixture.detectChanges();
		flushInitialRequests('position');

		queryParamsSubject.next({ playerType: 'pitcher' });

		const reqRank = httpMock.expectOne(req =>
			req.url === `${playersUrl}/rankings/all` && req.params.get('playerType') === 'pitcher'
		);
		reqRank.flush(mockRankings);

		const reqTeams = httpMock.match(req => req.url === `${teamsUrl}/standings`);
		if (reqTeams.length > 0) {
			reqTeams.forEach(req => req.flush(mockStandingsResponse));
		}

		expect(component.isPitcher).toBeTrue();
		expect(component.playerType).toBe('pitcher');
	});

    it('should reload dashboard when league filter is applied', () => {
        fixture.detectChanges();
        flushInitialRequests('position');

        component.onLeagueFilterChange('AL');

        const req = httpMock.expectOne(req =>
            req.params.get('league') === 'AL' && req.url === `${playersUrl}/rankings/all`
        );
        req.flush(mockRankings);
        expect(component.filteredLeague).toBe('AL');
    });
});