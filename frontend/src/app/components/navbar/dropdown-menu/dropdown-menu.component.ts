import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { TeamService } from '../../../services/team.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { TeamInfo, TeamSummary } from '../../../models/team.model';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-dropdown-menu',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './dropdown-menu.component.html',
})
export class DropdownMenuComponent implements OnInit {
	private teamService = inject(TeamService);
	public backgroundService = inject(BackgroundColorService);
	public errorMessage = '';
	public teams: TeamSummary[] = [];
	public teamInfoResponse: TeamInfo | null = null;

	ngOnInit() {
		this.teamService.getTeamsNamesAndAbbr().subscribe({
			next: (response) => (this.teams = response),
			error: (_) => (this.errorMessage = 'Error loading the team info'),
		});
	}

	public selectTeam(teamName: string) {
		this.teamService.selectTeam(teamName).subscribe({
			error: (err) => this.errorMessage = err.message
		});
	}
}