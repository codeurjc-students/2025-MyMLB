import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { TeamInfo } from '../../models/team-info.model';
import { SelectedTeamService } from '../../services/selected-team.service';
import { PositionPlayer } from '../../models/position-player.model';
import { Pitcher } from '../../models/pitcher.model';
import { TeamService } from '../../services/team.service';
import { BackgroundColorService } from '../../services/background-color.service';

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
	public championships: number[] = [];
	public teamRank: number = 0;

	constructor(private selectedTeamService: SelectedTeamService, private teamService: TeamService, private backgroundColorService: BackgroundColorService) {}

	ngOnInit() {
		this.selectedTeamService.selectedTeam$.subscribe(selectedTeam => {
			this.team = selectedTeam;
			this.positionPlayers = selectedTeam?.positionPlayers ?? [];
			this.pitchers = selectedTeam?.pitchers ?? [];
			this.championships = selectedTeam?.championships ?? [];

			if (selectedTeam?.teamStats?.abbreviation) {
				this.teamService.getTeamDivisionRank(selectedTeam.teamStats.abbreviation).subscribe((rank) => {
					this.teamRank = rank;
				});
			}
		});
	}

	public backgroundLogo(abbreviation: string | undefined) {
		return this.backgroundColorService.getBackgroundColor(abbreviation);
	}
}