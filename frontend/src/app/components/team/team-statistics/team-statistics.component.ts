import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, inject, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TeamService } from '../../../services/team.service';
import { RunStats, Team, WinsDistribution } from '../../../models/team.model';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { MatIconModule } from '@angular/material/icon';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { AnalyticsCardsComponent } from "../../stats/analytics-cards/analytics-cards.component";
import { AnalyticsCards } from '../../../models/analytics.model';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BackgroundColorService } from '../../../services/background-color.service';
import { MatCheckboxModule } from '@angular/material/checkbox';
import flatpickr from 'flatpickr';

@Component({
	selector: 'app-team-statistics',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		FormsModule,
		BaseChartDirective,
		LoadingModalComponent,
		MatIconModule,
		MatSelectModule,
		AnalyticsCardsComponent,
		MatTooltipModule,
		MatCheckboxModule
	],
	templateUrl: './team-statistics.component.html'
})
export class TeamStatisticsComponent implements OnInit, OnChanges {
	@Input() public baseTeamName!: string;

	private teamService = inject(TeamService);
	public backgroundColorService = inject(BackgroundColorService);

	public allRivals: Team[] = [];
	public displayedTeams: Team[] = [];
	public rivalTeams: string[] = [];
	public selectedRivalsAbbreviation: string[] = [];

	public winDistribution?: WinsDistribution;

	// UI
	public scoredRunsDataSet = true;
	@ViewChild('dateFrom') public dateFrom!: ElementRef;
	@ViewChild('selectRival') private selectRivalInput!: MatSelect;
	private selectedDateFrom = '';
	public availableLeagues = ['AL', 'NL'];
	public availableDivisions = ['EAST', 'CENTRAL', 'WEST'];
	public filteredLeague = '';
	public filteredDivision = '';
	public onlyOver500Teams = false;
	public homeTotalGames = 0;
	public roadTotalGames = 0;
	public homeWinPct = 0.0;
	public roadWinPct = 0.0;

	public loading = false;
	public error = false;
	public errorMessage = '';

	// Datasets Config
	// 1. Wins Per Rival (Pie)
    public winsPerRivalsChartData: ChartData<'pie'> = {
        labels: [],
        datasets: [{
            data: [],
            backgroundColor: ['#10B981', '#EF4444'],
            hoverBackgroundColor: ['#059669', '#DC2626'],
            borderWidth: 0
        }]
    };

    // 2. Run Stats (Bar)
    public runStatsData: RunStats[] = [];
    public runStatsChartData: ChartData<'bar'> = {
        labels: [],
        datasets: [{
            data: [],
            label: 'Runs',
            backgroundColor: '#10B981',
            borderRadius: 8,
            barThickness: 20
        }]
    };

    // 3. Win Distribution (Pie)
    public winDistributionChartData: ChartData<'pie'> = {
        labels: ['Home Wins', 'Road Wins'],
        datasets: [{
            data: [0, 0],
            backgroundColor: ['#6366F1', '#F59E0B'],
            hoverBackgroundColor: ['#4F46E5', '#D97706'],
            borderWidth: 0
        }]
    };

    // 4. Historic Ranking (Line)
    public historicRankingChartData: ChartData<'line'> = {
        labels: [],
        datasets: []
    };

	// Chart Options
	public lineOptions: ChartConfiguration['options'] = {
        responsive: true,
		maintainAspectRatio: false,
        scales: {
            x: {
				grid: { display: false },
				ticks: { color: '#94a3b8' } },
            y: {
                reverse: true,
                grid: { color: 'rgba(255,255,255,0.05)' },
                ticks: { color: '#94a3b8', stepSize: 1 },
                title: { display: true, text: 'Division Rank', color: '#6B7280' }
            }
        },
        plugins: {
            legend: {
				display: true,
				position: 'bottom',
				labels: { usePointStyle: true, color: '#9CA3AF' }
			},
			tooltip: {
            	mode: 'index',
                intersect: false,
                backgroundColor: 'rgba(17, 24, 39, 0.9)',
                titleFont: { size: 14, weight: 'bold' },
                bodySpacing: 5,
                padding: 12,
                cornerRadius: 10,
                displayColors: true
            }
        }
    };

    public winDistributionPieOptions: ChartConfiguration['options'] = {
        responsive: true,
		maintainAspectRatio: false,
        plugins: {
            legend: {
				position: 'bottom',
				labels: { color: '#94a3b8', padding: 20 }
			},
        }
    };

	public winsPerRivalsPieOptions: ChartConfiguration['options'] = {
		responsive: true,
		maintainAspectRatio: false,
		layout: {
			padding: 20
		},
		plugins: {
			legend: {
				position: 'bottom',
				labels: { color: '#94a3b8', padding: 20 }
			},
			tooltip: {
				callbacks: {
					label: (context) => {
						const value = context.parsed;
						if (context.label === this.baseTeamName) {
							return `${this.baseTeamName} Wins: ${value}`;
						}
						else {
							let rivalsTooltip = ' ';
							if (this.rivalTeams.length > 3) {
								rivalsTooltip = `${this.rivalTeams.slice(0, 3).join(', ')} and ${this.rivalTeams.length - 3} more`;
							}
							else {
								rivalsTooltip = this.rivalTeams.join(', ');
							}
							return `${rivalsTooltip} Wins: ${value}`;
						}
					}
				}
			}
		}
	};

    public barOptions: ChartConfiguration['options'] = {
        responsive: true,
		maintainAspectRatio: false,
        indexAxis: 'y',
        plugins: { legend: { display: false } },
        scales: {
            x: {
				grid: { display: true },
				ticks: { color: '#94a3b8' } },
            y: {
				grid: { display: false },
				ticks: { color: '#94a3b8' }
			}
        }
    };

	@ViewChild(BaseChartDirective) historicChart?: BaseChartDirective;

	ngOnInit() {
		const from = new Date();
		from.setMonth(from.getMonth() - 1);
		this.selectedDateFrom = from.toISOString().split('T')[0];

		this.loadRivalTeams();
		this.loadWinsPerRivals();
		this.loadWinDistribution();
		this.loadRunsStats();
		this.loadHistoricRanking();
	}

	ngAfterViewInit() {
		const configBase = {
			disableMobile: false,
			dateFormat: 'Y-m-d',
			altInput: true,
			altFormat: 'M j, Y',
		};

		flatpickr(this.dateFrom.nativeElement, {
			...configBase,
			defaultDate: this.selectedDateFrom,
			maxDate: new Date(),
			onChange: (selectedDates) => {
				if (selectedDates?.length) {
					this.selectedDateFrom = selectedDates[0].toISOString().split('T')[0];
					this.loadHistoricRanking();
				}
			},
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['baseTeamName'] && !changes['baseTeamName'].firstChange) {
			this.reloadComponent();
		}
	}

	private reloadComponent() {
        this.rivalTeams = [];
        this.selectedRivalsAbbreviation = [];

        this.loadRivalTeams();
        this.loadWinsPerRivals();
        this.loadWinDistribution();
        this.loadRunsStats();
        this.loadHistoricRanking();

        this.filteredLeague = '';
        this.filteredDivision = '';
        this.onlyOver500Teams = false;
    }

	// ----------------- Win Distribution Cards -------------------
	public getWinDsitributionCardsContent(): AnalyticsCards[] {
		return [
			{
				iconName: 'grid_view',
				iconStyles: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
				label: 'Total Games',
				textStyles: 'from-amber-600 to-orange-500',
				value: this.homeTotalGames + this.roadTotalGames,
				isRate: false
			},
			{
				iconName: 'stadium',
				iconStyles: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400',
				label: 'Home Games',
				textStyles: 'from-blue-600 to-indigo-500',
				value: this.homeTotalGames,
				isRate: false
			},
			{
				iconName: 'commute',
				iconStyles: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400',
				label: 'Road Games',
				textStyles: 'from-emerald-600 to-teal-500',
				value: this.roadTotalGames,
				isRate: false
			},
				{
				iconName: 'leaderboard',
				iconStyles: 'bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400',
				label: 'Home Wins PCT',
				textStyles: 'from-indigo-600 to-purple-500',
				value: this.homeWinPct * 100,
				isRate: true
			},
			{
				iconName: 'auto_graph',
				iconStyles: 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-600 dark:text-cyan-400',
				label: 'Road Wins PCT',
				textStyles: 'from-cyan-500 to-emerald-500',
				value: this.roadWinPct * 100,
				isRate: true
			},
		];
	}

	// ------------------------------ Loaders -----------------------------------
	private loadRivalTeams() {
		this.loading = true;
		this.teamService.getRivals(this.baseTeamName).subscribe({
			next: (response) => {
				this.allRivals = response;
				this.displayedTeams = [...response];
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur loading the teams: ${err.message}`;
			}
		});
	}

	private loadWinsPerRivals() {
		if (this.rivalTeams.length === 0) {
			return;
		}
		this.loading = true;
		this.teamService.getWinsPerRivals(this.baseTeamName, this.rivalTeams).subscribe({
			next: (response) => {
				if (response.length === 0) {
					this.winsPerRivalsChartData.datasets = [];
				}
				else {
					let winsCounter = 0;
					let rivalWinsCounter = 0;
					response.forEach(data => {
						winsCounter += data.wins;
						rivalWinsCounter += (data.gamesPlayed - data.wins);
					});
					const rivalsLabel = (this.rivalTeams.length === 1) ? this.rivalTeams[0] : 'Rivals';
					this.winsPerRivalsChartData = {
						labels: [this.baseTeamName, rivalsLabel],
						datasets: [{
							...this.winsPerRivalsChartData.datasets[0],
							data: [winsCounter, rivalWinsCounter]
						}]
					}
				}
			},
			error: (err) => this.handleErrors(err, 'wins per rival')
		});
	}

	private loadWinDistribution() {
		this.loading = true;
		this.teamService.getWinDistribution(this.baseTeamName).subscribe({
			next: (response) => {
				this.winDistribution = response;
				this.winDistributionChartData = {
					...this.winDistributionChartData,
					datasets: [{
						...this.winDistributionChartData.datasets[0],
						data: [response.homeWins, response.roadWins]
					}]
				}
				this.homeTotalGames = response.homeGames;
				this.roadTotalGames = response.roadGames;
				this.homeWinPct = response.homeWinPct;
				this.roadWinPct = response.roadWinPct;
				this.loading = false;
			},
			error: (err) => this.handleErrors(err, 'wins distribution')
		});
	}

	private loadRunsStats() {
		this.loading = true;
		const teams = [this.baseTeamName, ...this.rivalTeams];
		this.teamService.getRunsStatsPerRival(teams).subscribe({
			next: (response) => {
				this.runStatsData = response;
				this.runStatsChartData = {
					labels: this.runStatsData.map(data => data.teamName),
					datasets: [{
						...this.runStatsChartData.datasets[0],
						label: this.scoredRunsDataSet ? 'Runs Scored' : 'Runs Allowed',
						backgroundColor: this.scoredRunsDataSet ? '#10B981' : '#6366F1',
						data: this.runStatsData.map(data => this.scoredRunsDataSet ? data.runsScored : data.runsAllowed)
					}]
				}
				this.loading = false;
			},
			error: (err) => this.handleErrors(err, 'runs stats')
		});
	}

	private loadHistoricRanking() {
		this.loading = true;
		const teamsToDisplay = [this.baseTeamName, ...this.rivalTeams];
		this.teamService.getHistoricRanking(teamsToDisplay, this.selectedDateFrom).subscribe({
			next: (response) => {
				const teams = Object.keys(response);
				if (teams.length === 0) {
					this.loading = false;
					return;
				}
				const labels = response[teams[0]].map(data => new Date(data.matchDate).toLocaleDateString());
                const colors = ['#F59E0B', '#6366F1', '#10B981', '#EC4899', '#8B5CF6', '#06B6D4'];
				this.historicRankingChartData = {
                    labels,
                    datasets: teams.map((name, i) => ({
                        label: name,
                        data: response[name].map(res => res.rank),
                        borderColor: colors[i % colors.length],
                        backgroundColor: `${colors[i % colors.length]}1A`,
                        fill: true,
                        tension: 0.4,
                        pointBackgroundColor: colors[i % colors.length]
                    }))
                };
				this.historicChart?.update();
				this.loading = false;
			},
			error: (err) => this.handleErrors(err, 'historic ranking')
		});
	}

	public onRivalTeamSelected(rival: string) {
		if (!this.rivalTeams.includes(rival)) {
			this.rivalTeams.push(rival);
			const abbreviation = this.allRivals.find(team => team.name === rival)!.abbreviation;
			this.selectedRivalsAbbreviation.push(abbreviation);
			this.updateCharts();
		}
	}

	private updateCharts() {
		this.displayedTeams = this.allRivals.filter(rival => !this.rivalTeams.includes(rival.name));
		if (this.rivalTeams.length === 0) {
			this.resetCharts();
		}
		else {
			this.loadWinsPerRivals();
			this.loadRunsStats();
			this.loadHistoricRanking();
		}
	}

	private resetCharts() {
		this.winsPerRivalsChartData = {
			labels: [],
			datasets: [{
				...this.winsPerRivalsChartData.datasets[0],
				data: []
			}]
		};

		this.runStatsChartData = {
			labels: [this.baseTeamName],
			datasets: [this.runStatsChartData.datasets[0]]
		}

		const baseTeamDataset = this.historicRankingChartData.datasets.find(dataset => dataset.label === this.baseTeamName);

		if (baseTeamDataset) {
			this.historicRankingChartData = {
				labels: this.historicRankingChartData.labels,
				datasets: [{
					...baseTeamDataset,
                    borderColor: '#F59E0B',
                    backgroundColor: `#F59E0B1A`,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#F59E0B'
				}]
			}
		}
	}

	// ------------------ Filters ---------------------------------

	public onLeagueFilterChange(newLeague: string) {
		this.filteredLeague = newLeague;
		this.applyMixedFilters();
	}

	public onDivisionFilterChange(newDivision: string) {
		this.filteredDivision = newDivision;
		this.applyMixedFilters();
	}

	public onOver500RivalsFilterChange(newState: boolean) {
		this.onlyOver500Teams = newState;
		this.applyMixedFilters();
	}

	private applyMixedFilters() {
		const filteredRivalsBySelectors = this.allRivals.filter(rival => {
			const matchesLeague = !this.filteredLeague || rival.league === this.filteredLeague;
			const matchesDivision = !this.filteredDivision || rival.division === this.filteredDivision;
			const matchesOver500 = !this.onlyOver500Teams || (rival.pct && parseFloat(rival.pct) >= 0.500);

			return matchesLeague && matchesDivision && matchesOver500;
		});
		const hasActiveFilters = this.filteredLeague !== '' || this.filteredDivision !== '' || this.onlyOver500Teams;

		if (hasActiveFilters) { // Rivals based on the active filters
			this.rivalTeams = filteredRivalsBySelectors.map(rival => rival.name);
			this.selectedRivalsAbbreviation = filteredRivalsBySelectors.map(rival => rival.abbreviation);

			const currentSelectValue = this.selectRivalInput?.value;
			if (currentSelectValue && !this.rivalTeams.includes(currentSelectValue)) {
				this.selectRivalInput.value = null;
			}
		}
		else { // Rival teams manually selected
			const rivalManuallySelected = this.selectRivalInput?.value;
			if (rivalManuallySelected) {
				const team = this.allRivals.find(t => t.name === rivalManuallySelected);
				this.rivalTeams = [rivalManuallySelected];
				this.selectedRivalsAbbreviation = team ? [team.abbreviation] : [];
			}
			else {
				this.rivalTeams = [];
				this.selectedRivalsAbbreviation = [];
			}
		}
		this.updateCharts();
	}

	public setDefaultDateFilter() {
		const from = new Date();
		from.setMonth(from.getMonth() - 1);
		const formatted = from.toISOString().split('T')[0];
		this.selectedDateFrom = formatted;

		const fpFrom = (this.dateFrom.nativeElement as any)._flatpickr;
		if (fpFrom) {
			fpFrom.setDate(formatted);
		}
		this.loadHistoricRanking();
	}

	public onClearLeagueFilter() {
		this.filteredLeague = '';
		this.applyMixedFilters();
	}

	public onClearDivisionFilter() {
		this.filteredDivision = '';
		this.applyMixedFilters();
	}

	public onRemoveRival(abbr: string) {
		const team = this.allRivals.find(rival => rival.abbreviation === abbr)!;
		const rivalName = team.name;
		this.selectedRivalsAbbreviation = this.selectedRivalsAbbreviation.filter(a => a !== abbr);
		this.rivalTeams = this.rivalTeams.filter(name => name !== rivalName);
		if (this.selectRivalInput.value === rivalName) {
			this.selectRivalInput.value = null;
		}
		if (this.rivalTeams.length === 0) {
			this.clearAllFilters();
		}
		else {
			this.updateCharts();
		}
	}

	public clearAllFilters() {
		this.filteredLeague = '';
		this.filteredDivision = '';
		this.onlyOver500Teams = false;
		this.rivalTeams = [];
		this.selectRivalInput.value = null;
		this.selectedRivalsAbbreviation = [];
		this.applyMixedFilters();
		this.setDefaultDateFilter();
	}

	// ----------------------- Additional Methods -------------------
	public toggleRunStatsChartDataset() {
        this.scoredRunsDataSet = !this.scoredRunsDataSet;
        this.loadRunsStats();
    }

	private handleErrors(err: any, chartOfError: string) {
		this.loading = false;
        this.error = true;
        this.errorMessage = `An error occurred loading the ${chartOfError}: ${err.message}`;
	}

	public isWinsPerRivalsChartEmpty() {
		return this.rivalTeams.length === 0 || this.winsPerRivalsChartData.datasets.length === 0;
	}

	public getTeam(abbreviation: string) {
		return this.allRivals.find(team => team.abbreviation === abbreviation);
	}
}