import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { TeamService, StandingsResponse } from '../../../services/team.service';
import { StandingsData } from '../../../models/standings-data.model';
import { CommonModule } from '@angular/common';
import { BackgroundColorService } from '../../../services/background-color.service';

@Component({
    selector: 'app-standings',
    standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
    templateUrl: './standings-widget.component.html',
})
export class StandingsWidgetComponent implements OnInit {
    standings: StandingsData[] = [];
    currentIndex = 0;
    errorMessage = '';

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

	public getBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}
}