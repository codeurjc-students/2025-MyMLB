import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { StandingsResponse, TeamService } from '../../services/team.service';
import { BackgroundColorService } from '../../services/background-color.service';
import { StandingsData } from '../../models/standings-data.model';

@Component({
	selector: 'app-standings',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './standings.component.html',
})
export class StandingsComponent implements OnInit {
	public standings: StandingsData[] = [];
	public errorMessage = '';

	constructor(private teamService: TeamService, private backgroundService: BackgroundColorService) {}

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

	public getBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}

	public selectTeam(teamName: string) {
		this.teamService.selectTeam(teamName).subscribe({
			error: (err) => this.errorMessage = err.message
		});
	}
}