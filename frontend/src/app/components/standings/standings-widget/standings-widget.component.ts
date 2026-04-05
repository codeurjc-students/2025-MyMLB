import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TeamService, StandingsResponse } from '../../../services/team.service';
import { StandingsData } from '../../../models/standings-data.model';
import { CommonModule } from '@angular/common';
import { BackgroundColorService } from '../../../services/background-color.service';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
    selector: 'app-standings',
    standalone: true,
	imports: [CommonModule, MatTooltipModule, MatIconModule, MatProgressSpinnerModule],
	changeDetection: ChangeDetectionStrategy.Default,
    templateUrl: './standings-widget.component.html',
})
export class StandingsWidgetComponent implements OnInit {
	private teamService = inject(TeamService);
	public backgroundService = inject(BackgroundColorService);

    public standings: StandingsData[] = [];
    public currentIndex = 0;
    public errorMessage = '';
	public loading = false;

    ngOnInit() {
        this.loadStandings();
    }

	private loadStandings() {
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

	public selectTeam(teamName: string) {
		this.teamService.selectTeam(teamName).subscribe({
			error: (err) => this.errorMessage = err.message
		});
	}

	public isGoodPct(pct: string) {
		return this.teamService.isGoodPct(pct);
	}

	public refreshStandings() {
		this.loading = true;
		this.teamService.refreshStandings().subscribe({
			next: (_) => {
				this.loading = false;
				this.standings = [];
				this.loadStandings();
			},
			error: (err) => {
				this.loading = false;
				this.errorMessage = `An error occurr refreshing the standings: ${err.message}`;
			}
		});
	}

	public getCurrentDate() {
		const today = new Date();
		return today.toISOString().split('T')[0];
	}
}