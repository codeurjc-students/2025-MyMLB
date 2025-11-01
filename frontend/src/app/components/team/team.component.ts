import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { TeamInfo } from '../../models/team-info.model';
import { SelectedTeamService } from '../../services/selected-team.service';
import { PositionPlayer } from '../../models/position-player.model';
import { Pitcher } from '../../models/pitcher.model';

@Component({
	selector: 'app-team',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './team.component.html',
})
export class TeamComponent implements OnInit {
	public team: TeamInfo | null = null;
	public positionPlayers: PositionPlayer[] = [];
	public pitchers: Pitcher[] = [];

	constructor(private selectedTeamService: SelectedTeamService) {}

	ngOnInit() {
		this.selectedTeamService.selectedTeam$.subscribe(selectedTeam => {
			this.team = selectedTeam;
			this.positionPlayers = selectedTeam?.positionPlayers ?? [];
			this.pitchers = selectedTeam?.pitchers ?? [];
		});
	}
}