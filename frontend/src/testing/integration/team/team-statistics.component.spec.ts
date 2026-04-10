import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TeamStatisticsComponent } from '../../../app/components/team/team-statistics/team-statistics.component';
import { TeamService } from '../../../app/services/team.service';
import { Chart, registerables } from 'chart.js';
import { Team, WinsDistribution } from './../../../app/models/team.model';

Chart.register(...registerables);

describe('Team Statistics Component Integration Test', () => {
    let fixture: ComponentFixture<TeamStatisticsComponent>;
    let component: TeamStatisticsComponent;
    let httpMock: HttpTestingController;

    const baseApiUrl = '/api/v1/teams';

    const mockRivals: Team[] = [
        { name: 'Boston Red Sox', abbreviation: 'BOS', league: 'AL', division: 'EAST', pct: '0.550' } as Team,
        { name: 'Baltimore Orioles', abbreviation: 'BAL', league: 'AL', division: 'EAST', pct: '0.450' } as Team
    ];

    const mockWinDistribution: WinsDistribution = {
        teamName: 'New York Yankees',
        homeWins: 10,
        roadWins: 5,
        homeGames: 15,
        roadGames: 15,
        homeWinPct: 0.666,
        roadWinPct: 0.333
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [TeamStatisticsComponent],
            providers: [
                TeamService,
                provideHttpClient(),
                provideHttpClientTesting(),
            ]
        });

        fixture = TestBed.createComponent(TeamStatisticsComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);

        component.baseTeamName = 'New York Yankees';
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should retrieve new stats when a rival is manually selected', () => {
        fixture.detectChanges();
        completeInitialRequests();

        component.onRivalTeamSelected('Boston Red Sox');

        const winsReq = httpMock.expectOne(req =>
            req.url.includes('/wins-per-rival') &&
            req.params.getAll('rivalTeamNames')!.includes('Boston Red Sox')
        );
        winsReq.flush([{ wins: 2, gamesPlayed: 5 }]);

        const runsReq = httpMock.expectOne(req => req.url.includes('/runs-per-rival'));
        runsReq.flush([]);

        const histReq = httpMock.expectOne(req => req.url.includes('/historic-ranking'));
        histReq.flush({});

        expect(component.rivalTeams).toContain('Boston Red Sox');
    });

    function completeInitialRequests() {
        httpMock.expectOne(`${baseApiUrl}/New York Yankees/rivals`).flush(mockRivals);

        httpMock.expectOne(`${baseApiUrl}/New York Yankees/analytics/win-distribution`).flush(mockWinDistribution);

        httpMock.expectOne(req =>
            req.url.includes('/runs-per-rival') &&
            req.params.get('teams') === 'New York Yankees'
        ).flush([]);

        httpMock.expectOne(req =>
            req.url.includes('/historic-ranking') &&
            req.params.get('teams') === 'New York Yankees'
        ).flush({});
    }
});