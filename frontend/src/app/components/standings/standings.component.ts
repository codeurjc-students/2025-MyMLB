import { Component, OnInit } from '@angular/core';
import { TeamService, StandingsResponse } from '../../services/team.service';
import { StandingsData } from '../../models/standings-data.model';
import { CommonModule } from '@angular/common';
import { Team } from '../../models/team-model';

@Component({
    selector: 'app-standings',
    standalone: true,
	imports: [CommonModule],
    templateUrl: './standings.component.html',
})
export class StandingsComponent implements OnInit {
    standings: StandingsData[] = [];
    currentIndex = 0;
    errorMessage = '';

    constructor(private teamService: TeamService) {}

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
            error: (_) => {
                this.errorMessage = 'Error trying to load the standings';
            }
        });
    }

    public previous() {
        this.currentIndex = (this.currentIndex - 1 + this.standings.length) % this.standings.length;
    }

    public next() {
        this.currentIndex = (this.currentIndex + 1) % this.standings.length;
    }

	public getBackgroundColor(team: Team) {
		switch(team.abbreviation) {
			case 'TOR':return 'bg-sky-500';
			case 'LAA':
			case 'CIN':
			case 'ARI':
			case 'PHI':
			case 'WSH':
				 return 'bg-red-500';
			case 'TB':
			case 'KC':
				return 'bg-sky-300';
			case 'BAL':
			case 'HOU':
			case 'DET':
				return 'bg-orange-500';

			case 'CLE':
			case 'SEA':
			case 'TEX':
			case 'NYM':
			case 'ATL':
			case 'MIL':
			case 'MIN':
			case 'NYY':
			case 'BOS':
				return 'bg-blue-900';

			case 'MIA': return 'bg-sky-400';

			case 'CHC': return 'bg-blue-700';

			case 'ATH': return 'bg-green-900';

			case 'PIT':
			case 'CWS':
			case 'SF':
				return 'bg-black';

			case 'LAD': return 'bg-blue-600';

			case 'SD': return 'bg-yellow-500';

			case 'COL': return 'bg-purple-700';

			default:
				return 'bg-blue-100';
		}
	}
}