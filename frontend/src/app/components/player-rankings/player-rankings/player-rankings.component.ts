import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { PlayerService } from '../../../services/player.service';
import { PlayerRanking } from '../../../models/position-player.model';
import { TeamService } from '../../../services/team.service';
import { TeamSummary } from '../../../models/team.model';
import { BackgroundColorService } from '../../../services/background-color.service';
import { ActivatedRoute } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../services/auth.service';
import { StatsFormatterService } from '../../../services/utilities/stats-formatter.service';
import { ExportService } from '../../../services/utilities/export.service';

@Component({
	selector: 'app-player-rankings',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		FormsModule,
		MatSelectModule,
		MatFormFieldModule,
		MatProgressSpinnerModule,
		MatIconModule,
		BaseChartDirective,
		MatTooltipModule
	],
	templateUrl: './player-rankings.component.html',
})
export class PlayerRankingsComponent implements OnInit {
	private playerService = inject(PlayerService);
	private teamService = inject(TeamService);
	private authService = inject(AuthService);
	private exportService = inject(ExportService);
	public backgroundService = inject(BackgroundColorService);
	public statFormatter = inject(StatsFormatterService);
	private route = inject(ActivatedRoute);

	public playerType: string = 'position';
	public rankings: Record<string, PlayerRanking[]> = {};
	public availablePlayerTypes = [
		{ label: 'Hitters', value: 'position' },
		{ label: 'Pitchers', value: 'pitcher' },
	];
	public keepOrder = () => 0;
	public availablePositionPlayerStats: Record<string, string> = {
		AVG: 'average',
		AB: 'atBats',
		H: 'hits',
		BB: 'walks',
		HR: 'homeRuns',
		RBI: 'rbis',
		OBP: 'obp',
		OPS: 'ops',
		SLG: 'slugging',
		'2B': 'doubles',
		'3B': 'triples',
	};
	public availablePitcherStats: Record<string, string> = {
		ERA: 'era',
		G: 'games',
		W: 'wins',
		L: 'losses',
		IP: 'inningsPitched',
		SO: 'totalStrikeouts',
		BB: 'walks',
		HA: 'hitsAllowed',
		RA: 'runsAllowed',
		S: 'saves',
		SOPP: 'saveOpportunities',
		WHIP: 'whip',
	};
	public completeStatName: Record<string, string> = {
		AVG: 'Average',
		AB: 'At Bats',
		H: 'Hits',
		BB: 'Base on Balls',
		HR: 'Home Runs',
		RBI: 'Runs Batted In',
		OBP: 'On Base Percentage',
		OPS: 'On Base Plus Slugging',
		SLG: 'Slugging',
		'2B': 'Doubles',
		'3B': 'Triples',
		ERA: 'Earn Runs Allowed',
		G: 'Games',
		W: 'Wins',
		L: 'Losses',
		IP: 'Innings Pitched',
		SO: 'Strike Outs',
		HA: 'Hits Allowed',
		RA: 'Runs Allowed',
		S: 'Saves',
		SOPP: 'Saves Opportunities',
		WHIP: 'Walks Plus Hits Per Innings Pitched',
	};

	public availableLeagues = ['AL', 'NL'];
	public availableDivisions = ['EAST', 'CENTRAL', 'WEST'];
	public allTeams: TeamSummary[] = [];
	public teamsToDisplay: TeamSummary[] = [];

	public isPitcher = false;
	public filteredTeams: string[] = [];
	public filteredLeague = '';
	public filteredDivision = '';
	public numberOfPlayersToShow = 5;

	public currentUser$ = this.authService.currentUser$;

	public loading = false;
	public error = false;
	public errorMessage = '';
	public noTeamsAvailable = '';

	public barChartData: ChartData<'bar'> = {
		labels: [],
		datasets: [
			{
				data: [],
				label: 'Favorites per Team',
				backgroundColor: '#6366F1',
				hoverBackgroundColor: '#4F46E5',
				borderRadius: 6,
			},
		],
	};

	public barChartOptions: ChartConfiguration['options'] = {
		responsive: true,
		maintainAspectRatio: false,
		scales: {
			x: {
				grid: { display: false },
				ticks: { color: '#9CA3AF', font: { size: 12 } },
				title: {
					display: true,
					text: 'Players',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'system-ui',
					},
					padding: { top: 10 },
				},
			},
			y: {
				beginAtZero: true,
				ticks: {
					stepSize: 1,
					color: '#9CA3AF',
				},
				grid: { color: 'rgba(255,255,255,0.05)' },
				title: {
					display: true,
					text: 'Value',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'sytem-ui',
					},
					padding: { bottom: 10 },
				},
			},
		},
		plugins: {
			legend: { display: false },
			tooltip: {
				backgroundColor: 'rgba(17, 24, 39, 0.9)',
				padding: 12,
				cornerRadius: 8,
			},
		},
	};

	public barChartType: ChartType = 'bar';

	@ViewChild(BaseChartDirective) chart?: BaseChartDirective;

	public activeCharts = new Set<string>();
	public selectedRankingForChart: { key: string, value: string } | null = null;
	public currentChartData: ChartData<'bar'> = { labels: [], datasets: [] };

	ngOnInit() {
		this.route.queryParams.subscribe((param) => {
			const receivedParam = param['playerType'];
			if (receivedParam) {
				this.playerType = receivedParam;
			}
			this.isPitcher = this.playerType === 'pitcher';
			this.loadDashboard();
			this.loadTeams();
		});
	}

	private loadDashboard() {
		this.loading = true;
		this.playerService.getPlayerAllStatsRankings(this.playerType, this.filteredTeams, this.filteredLeague, this.filteredDivision).subscribe({
			next: (response) => {
				this.rankings = response;
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur fetching the ranking: ${err.message}`;
			},
		});
	}

	private loadTeams() {
		this.teamService.getTeamsNamesAndAbbr().subscribe({
			next: (response) => {
				this.allTeams = response;
				this.adjustTeamsToDisplay();
			},
			error: (_) => {
				this.noTeamsAvailable = 'No teams available to show';
			},
		});
	}

	private adjustTeamsToDisplay() {
		let aux = [...this.allTeams];
		if (this.filteredLeague) {
			aux = aux.filter((team) => team.league === this.filteredLeague);
		}
		if (this.filteredDivision) {
			aux = aux.filter((team) => team.division === this.filteredDivision);
		}
		this.teamsToDisplay = aux;
	}

	public onNumberOfPlayersChange(value: number) {
		this.numberOfPlayersToShow = value;
	}

	public onTemplateChange(newVal: string) {
		this.playerType = newVal;
		this.isPitcher = newVal === 'pitcher';
		this.loadDashboard();
	}

	public onTeamFilterChange(newTeam: string) {
		this.filteredTeams.push(newTeam);
		this.loadDashboard();
	}

	public onLeagueFilterChange(newLeague: string) {
		this.filteredLeague = newLeague;
		this.loadDashboard();
		this.adjustTeamsToDisplay();
	}

	public onDivisionFilterChange(newDivision: string) {
		this.filteredDivision = newDivision;
		this.loadDashboard();
		this.adjustTeamsToDisplay();
	}

	public onClearTeamFilter() {
		this.filteredTeams = [];
		this.loadDashboard();
	}

	public onClearLeagueFilter() {
		this.filteredLeague = '';
		this.loadDashboard();
		this.adjustTeamsToDisplay();
	}

	public onClearDivisionFilter() {
		this.filteredDivision = '';
		this.loadDashboard();
		this.adjustTeamsToDisplay();
	}

	public onClearAllFilters() {
		this.filteredTeams = [];
		this.filteredLeague = '';
		this.filteredDivision = '';

		this.loadDashboard();
		this.adjustTeamsToDisplay();
	}

	public openChart(key: string, value: string) {
		this.selectedRankingForChart = { key, value };
		this.updateCurrentChartData(value);
		document.body.style.overflow = 'hidden'; // Disable background scroll
	}

	public updateCurrentChartData(statKey: string) {
		const players = this.rankings[statKey] || [];
		const playersToDisplay = players.slice(0, this.numberOfPlayersToShow);

		this.currentChartData = {
			labels: playersToDisplay.map(player => player.name.split(' ').pop()),
			datasets: [{
				data: playersToDisplay.map(player => player.stat),
				label: this.selectedRankingForChart?.key || '',
				backgroundColor: '#6366F1',
				hoverBackgroundColor: '#4F46E5',
				borderRadius: 6,
				barThickness: 30
			}]
		};
	}

	public closeChart() {
		this.selectedRankingForChart = null;
		document.body.style.overflow = 'auto'; // Enable Scroll
	}

	public toggleChart(statKey: string) {
		if (this.activeCharts.has(statKey)) {
			this.activeCharts.delete(statKey);
		}
		else {
			this.activeCharts.add(statKey);
		}
	}

	public getCurrentDate() {
		const today = new Date();
		return today.toISOString().split('T')[0];
	}

	public refresh() {
		this.loading = true;
		this.playerService.refreshPlayerRankings().subscribe({
			next: (_) => {
				this.loading = false;
				this.rankings = {};
				this.loadDashboard();
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur updating the dashboard: ${err.message}`;
			}
		});
	}

	public downloadChartAsPNG() {
		const canvas = this.chart?.chart?.canvas;
		if (canvas) {
			const date = new Date().toISOString().split('T')[0];
			const chartName = this.completeStatName[this.selectedRankingForChart!.key];
			const fileName = `${chartName}_Ranking_${date}`;
			this.exportService.downloadPNG(canvas, fileName);
		}
	}
}