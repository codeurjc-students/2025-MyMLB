import { SelectedTeamService } from './../../../services/selected-team.service';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { SimplifiedTeam, TeamService } from '../../../services/team.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { Router } from '@angular/router';
import { TeamInfo } from '../../../models/team-info.model';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-dropdown-menu',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './dropdown-menu.component.html',
})
export class DropdownMenuComponent implements OnInit {
	public errorMessage = '';
	public teams: SimplifiedTeam[] = [];
	public teamInfoResponse: TeamInfo | null = null;

	constructor(
		private teamService: TeamService,
		private backgroundService: BackgroundColorService,
		private selectedTeamService: SelectedTeamService,
		private router: Router
	) {}

	ngOnInit() {
		this.teamService.getTeamsNamesAndAbbr().subscribe({
			next: (response) => (this.teams = response),
			error: (_) => (this.errorMessage = 'Error loading the team info'),
		});
	}

	public selectTeam(teamName: string) {
		this.teamService.getTeamInfo(teamName).subscribe({
			next: (response) => {
				this.selectedTeamService.setSelectedTeam(response);
				this.router.navigate(['team', teamName]);
			},
			error: (err) => {
				this.errorMessage = err.message;
			},
		});
	}

	public loadBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}
}