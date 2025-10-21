import { Component, OnInit } from '@angular/core';
import { MatchService, ShowMatch, TeamSummary } from '../../../services/match.service';
import { CommonModule } from '@angular/common';
import { BackgroundColorService } from '../../../services/background-color.service';

@Component({
	selector: 'app-matches-of-the-day',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './matches-of-the-day.component.html'
})
export class MatchesOfTheDayComponent implements OnInit {
	public matches: ShowMatch[] = [];
	public errorMessage = '';

	constructor(private matchService: MatchService, private backgroundService: BackgroundColorService) {}

	ngOnInit(): void {
		this.matchService.getMatchesOfTheDay().subscribe({
			next: (response) => this.matches = response,
			error: (_) => this.errorMessage = 'Error trying to show the matches'
		});
	}

	public getBackgroundColor(team: TeamSummary) {
		return this.backgroundService.getBackgroundColor(team);
	}
}