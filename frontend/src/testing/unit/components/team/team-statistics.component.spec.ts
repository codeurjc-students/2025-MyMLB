import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TeamStatisticsComponent } from '../../../../app/components/team/team-statistics/team-statistics.component';
import { TeamService } from '../../../../app/services/team.service';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { HistoricRanking, RunStats, Team, WinsDistribution } from '../../../../app/models/team.model';
import { SimpleChange } from '@angular/core';

Chart.register(...registerables);

describe('Team Statistics Component Tests', () => {
    let component: TeamStatisticsComponent;
    let fixture: ComponentFixture<TeamStatisticsComponent>;
    let teamServiceSpy: jasmine.SpyObj<TeamService>;

    const mockRivals: Team[] = [
        { name: 'Boston Red Sox', abbreviation: 'BOS', league: 'AL', division: 'EAST', pct: '0.550' } as Team,
        { name: 'Baltimore Orioles', abbreviation: 'BAL', league: 'AL', division: 'EAST', pct: '0.450' } as Team
    ];

    const mockWinDistribution: WinsDistribution = {
        teamName: 'New York Yankees',
        homeWins: 50,
        roadWins: 40,
        homeGames: 81,
        roadGames: 81,
        homeWinPct: 0.617,
        roadWinPct: 0.493
    };

    const mockRunStats: RunStats[] = [
        { teamName: 'New York Yankees', runsScored: 800, runsAllowed: 600 },
        { teamName: 'Boston Red Sox', runsScored: 750, runsAllowed: 700 }
    ];

	const mockHistoricRanking: HistoricRanking[] = [
		{
			teamName: 'New York Yankees',
			matchDate: new Date(2026, 3, 1),
			rank: 1,
			wins: 20,
			losses: 10
		},
		{
			teamName: 'New York Yankees',
			matchDate: new Date(2026, 3, 2),
			rank: 1,
			wins: 21,
			losses: 10
		},
	];

	const mockHistoricRankingResponse: Record<string, HistoricRanking[]> = {
		'New York Yankees': mockHistoricRanking
	};

    beforeEach(() => {
        teamServiceSpy = jasmine.createSpyObj('TeamService', [
            'getRivals',
            'getWinsPerRivals',
            'getWinDistribution',
            'getRunsStatsPerRival',
            'getHistoricRanking'
        ]);
        teamServiceSpy.getRivals.and.returnValue(of(mockRivals));
        teamServiceSpy.getWinsPerRivals.and.returnValue(of([{ rivalTeamName: 'Boston Red Sox', wins: 5, gamesPlayed: 10 }]));
        teamServiceSpy.getWinDistribution.and.returnValue(of(mockWinDistribution));
        teamServiceSpy.getRunsStatsPerRival.and.returnValue(of(mockRunStats));
        teamServiceSpy.getHistoricRanking.and.returnValue(of(mockHistoricRankingResponse));

        TestBed.configureTestingModule({
            imports: [TeamStatisticsComponent],
            providers: [
                provideHttpClient(),
                { provide: TeamService, useValue: teamServiceSpy }
            ],
        });

        fixture = TestBed.createComponent(TeamStatisticsComponent);
        component = fixture.componentInstance;
        component.baseTeamName = 'New York Yankees';
    });

    it('should load all charts data on initialization', () => {
        fixture.detectChanges();

        expect(teamServiceSpy.getRivals).toHaveBeenCalledWith('New York Yankees');
        expect(teamServiceSpy.getWinDistribution).toHaveBeenCalledWith('New York Yankees');
        expect(component.allRivals.length).toBe(2);
        expect(component.homeTotalGames).toBe(81);
    });

    it('should update charts when a rival is selected', () => {
        fixture.detectChanges();
        component.onRivalTeamSelected('Boston Red Sox');

        expect(component.rivalTeams).toContain('Boston Red Sox');
        expect(component.selectedRivalsAbbreviation).toContain('BOS');
        expect(teamServiceSpy.getWinsPerRivals).toHaveBeenCalled();
        expect(teamServiceSpy.getRunsStatsPerRival).toHaveBeenCalled();
    });

	// ????
    it('should reload data when baseTeamName input changes', () => {
        fixture.detectChanges();
        teamServiceSpy.getRivals.calls.reset();

        component.baseTeamName = 'Los Angeles Dodgers';
        component.ngOnChanges({
            baseTeamName: new SimpleChange('New York Yankees', 'Los Angeles Dodgers', false)
        });
        expect(teamServiceSpy.getRivals).toHaveBeenCalledWith('Los Angeles Dodgers');
    });

    it('should toggle from Runs Scored to Runs Allowed', () => {
        fixture.detectChanges();
        expect(component.scoredRunsDataSet).toBeTrue();

        component.toggleRunStatsChartDataset();
        expect(component.scoredRunsDataSet).toBeFalse();
        expect(teamServiceSpy.getRunsStatsPerRival).toHaveBeenCalled();
    });

    it('should apply league and division filters correctly', () => {
        fixture.detectChanges();

        component.onLeagueFilterChange('AL');
        component.onDivisionFilterChange('EAST');

        expect(component.rivalTeams.length).toBe(2);
        expect(component.rivalTeams).toContain('Boston Red Sox');
        expect(component.rivalTeams).toContain('Baltimore Orioles');
    });

    it('should apply over .500 teams filter correctly', () => {
        fixture.detectChanges();

        component.onOver500RivalsFilterChange(true);

        expect(component.rivalTeams).toContain('Boston Red Sox');
        expect(component.rivalTeams).not.toContain('Baltimore Orioles');
    });

    it('should clear all filters and reset charts', () => {
        fixture.detectChanges();
        component.filteredLeague = 'AL';
        component.rivalTeams = ['Boston Red Sox'];

        (component as any).selectRivalInput = { value: 'Boston Red Sox' };

        component.clearAllFilters();

        expect(component.filteredLeague).toBe('');
        expect(component.rivalTeams.length).toBe(0);
        expect(component.onlyOver500Teams).toBeFalse();
    });

    it('should handle errors correctly when a service fails', () => {
		// The winDistribution service was used as a test case, but the driver should behave the same way for the others.
        teamServiceSpy.getWinDistribution.and.returnValue(throwError(() => new Error('API Error')));

        fixture.detectChanges();

        expect(component.error).toBeTrue();
    });

    it('should generate analytics cards with correct calculated values', () => {
        fixture.detectChanges();

        expect(component.homeTotalGames).toBe(81);
        expect(component.homeWinPct * 100).toBe(61.7); // 0.617 * 100 (New York Yankees as baseTeam)
        expect(component.roadWinPct * 100).toBe(49.3); // 0.493 * 100 (New York Yankees as baseTeam)
    });

    it('should remove a specific rival and update charts', () => {
        fixture.detectChanges();
        component.rivalTeams = ['Boston Red Sox', 'Baltimore Orioles'];
        component.selectedRivalsAbbreviation = ['BOS', 'BAL'];
        (component as any).selectRivalInput = { value: null };

        component.onRemoveRival('BOS');

        expect(component.selectedRivalsAbbreviation).not.toContain('BOS');
        expect(component.rivalTeams).not.toContain('Boston Red Sox');
        expect(component.rivalTeams).toContain('Baltimore Orioles');
        expect(teamServiceSpy.getWinsPerRivals).toHaveBeenCalled();
    });
});