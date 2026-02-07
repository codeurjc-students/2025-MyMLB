import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { StandingsResponse, TeamService } from '../../services/team.service';
import { BackgroundColorService } from '../../services/background-color.service';
import { StandingsData } from '../../models/standings-data.model';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
	selector: 'app-standings',
	standalone: true,
	imports: [CommonModule, MatTooltip],
	templateUrl: './standings.component.html',
})
export class StandingsComponent implements OnInit {
	public standings: StandingsData[] = [];
	public errorMessage = '';

	public headers = [
		{ stat: 'G', description: 'Total games played' },
		{ stat: 'W', description: 'Games won' },
		{ stat: 'L', description: 'Lost games' },
		{ stat: 'PCT', description: 'Winning Percentage: Wins divided by total games played' },
		{ stat: 'GB', description: 'Games Behind: Game difference compared to the division leader' },
		{ stat: 'L10', description: 'Last 10 Games: Performance of the team the last 10 games in the following format: Wins - Losses' }
	];

	constructor(private teamService: TeamService, public backgroundService: BackgroundColorService) {}

	ngOnInit() {
		this.teamService.getStandings().subscribe({
			next: (response: StandingsResponse) => {
				for (const league of Object.keys(response)) {
					const divisions = response[league];
					for (const division of Object.keys(divisions)) {
						this.standings.push({
							league,
							division,
							teams: divisions[division]
						});
					}
				}
			},
			error: (_) => this.errorMessage = 'Error trying to load the standings'
		});
	}

	public selectTeam(teamName: string) {
		this.teamService.selectTeam(teamName).subscribe({
			error: (err) => this.errorMessage = err.message
		});
	}
}