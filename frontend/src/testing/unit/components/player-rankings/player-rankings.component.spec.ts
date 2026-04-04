import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlayerRankingsComponent } from '../../../../app/components/player-rankings/player-rankings/player-rankings.component';
import { PlayerService } from '../../../../app/services/player.service';
import { TeamService } from '../../../../app/services/team.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { ActivatedRoute } from '@angular/router';
import { of, BehaviorSubject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { PlayerRanking } from '../../../../app/models/position-player.model';
import { TeamSummary } from '../../../../app/models/team.model';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

describe('Player Rankings Component Tests', () => {
    let component: PlayerRankingsComponent;
    let fixture: ComponentFixture<PlayerRankingsComponent>;

    let playerServiceSpy: jasmine.SpyObj<PlayerService>;
    let teamServiceSpy: jasmine.SpyObj<TeamService>;
    let backgroundColorServiceSpy: jasmine.SpyObj<BackgroundColorService>;

    const queryParamsSubject = new BehaviorSubject<any>({ playerType: 'position' });

    const mockRankingsResponse: Record<string, PlayerRanking[]> = {
        average: [{ name: 'Aaron Judge', picture: '', stat: 0.311 }],
        homeRuns: [{ name: 'Juan Soto', picture: '', stat: 40 }]
    };

    const mockTeamsResponse: TeamSummary[] = [
        { name: 'New York Yankees', abbreviation: 'NYY', league: 'AL', division: 'EAST' },
        { name: 'Los Angeles Dodgers', abbreviation: 'LAD', league: 'NL', division: 'WEST' }
    ];

    beforeEach(() => {
        playerServiceSpy = jasmine.createSpyObj('PlayerService', ['getPlayerAllStatsRankings', 'refreshPlayerRankings']);
        teamServiceSpy = jasmine.createSpyObj('TeamService', ['getTeamsNamesAndAbbr']);
        backgroundColorServiceSpy = jasmine.createSpyObj('BackgroundColorService', ['getBackgroundColor']); // Asumiendo métodos del servicio

        playerServiceSpy.getPlayerAllStatsRankings.and.returnValue(of(mockRankingsResponse));
        playerServiceSpy.refreshPlayerRankings.and.returnValue(of({ status: 'SUCCESS' } as any));
        teamServiceSpy.getTeamsNamesAndAbbr.and.returnValue(of(mockTeamsResponse));

        TestBed.configureTestingModule({
            imports: [PlayerRankingsComponent],
            providers: [
                provideHttpClient(),
                { provide: PlayerService, useValue: playerServiceSpy },
                { provide: TeamService, useValue: teamServiceSpy },
                { provide: BackgroundColorService, useValue: backgroundColorServiceSpy },
                {
                    provide: ActivatedRoute,
                    useValue: { queryParams: queryParamsSubject.asObservable() }
                }
            ]
        });

        fixture = TestBed.createComponent(PlayerRankingsComponent);
        component = fixture.componentInstance;
		fixture.detectChanges();
    });

    it('should initialize and load data based on queryParams', () => {
        expect(component.playerType).toBe('position');
        expect(playerServiceSpy.getPlayerAllStatsRankings).toHaveBeenCalledWith('position', [], '', '');
        expect(teamServiceSpy.getTeamsNamesAndAbbr).toHaveBeenCalled();
        expect(component.rankings['average']).toBeDefined();
    });

    it('should filter teams based on league and division', () => {
        component.onLeagueFilterChange('AL');

        expect(component.filteredLeague).toBe('AL');
        expect(component.teamsToDisplay.length).toBe(1);
        expect(component.teamsToDisplay[0].name).toBe('New York Yankees');
        expect(playerServiceSpy.getPlayerAllStatsRankings).toHaveBeenCalled();
    });

    it('should handle team filter selection', () => {
        component.onTeamFilterChange('New York Yankees');

        expect(component.filteredTeams).toContain('New York Yankees');
        expect(playerServiceSpy.getPlayerAllStatsRankings).toHaveBeenCalled();
    });

    it('should clear all filters correctly', () => {
        component.filteredLeague = 'AL';
        component.filteredTeams = ['New York Yankees'];

        component.onClearAllFilters();

        expect(component.filteredLeague).toBe('');
        expect(component.filteredTeams.length).toBe(0);
        expect(playerServiceSpy.getPlayerAllStatsRankings).toHaveBeenCalledWith(jasmine.any(String), [], '', '');
    });

    it('should toggle chart state correctly', () => {
        const stat = 'AVG';
        component.toggleChart(stat);
        expect(component.activeCharts.has(stat)).toBeTrue();

        component.toggleChart(stat);
        expect(component.activeCharts.has(stat)).toBeFalse();
    });

    it('should prepare chart data correctly', () => {
        component.rankings = mockRankingsResponse;
        component.numberOfPlayersToShow = 1;

        const chartData = component.getChartData('average');

        expect(chartData.labels).toEqual(['Judge']);
        expect(chartData.datasets[0].data).toEqual([0.311]);
    });

    it('should refresh the dashboard successfully', () => {
        playerServiceSpy.getPlayerAllStatsRankings.calls.reset();

        component.refresh();

        expect(playerServiceSpy.refreshPlayerRankings).toHaveBeenCalled();
        expect(playerServiceSpy.getPlayerAllStatsRankings).toHaveBeenCalled();
    });
});