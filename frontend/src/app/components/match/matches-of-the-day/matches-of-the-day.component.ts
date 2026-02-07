import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatchService, ShowMatch } from '../../../services/match.service';
import { CommonModule } from '@angular/common';
import { BackgroundColorService } from '../../../services/background-color.service';

@Component({
	selector: 'app-matches-of-the-day',
	standalone: true,
	imports: [CommonModule, MatTooltipModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './matches-of-the-day.component.html'
})
export class MatchesOfTheDayComponent implements OnInit {
	public matches: ShowMatch[] = [];
    public errorMessage = '';
    public currentPage = 0;
    public readonly pageSize = 10;
    public hasMore = true;

	constructor(private matchService: MatchService, public backgroundService: BackgroundColorService) {}

	ngOnInit(): void {
		this.loadMoreGames(0);
	}

	public loadMoreGames(page: number) {
		this.matchService.getMatchesOfTheDay(page, this.pageSize).subscribe({
			next: (response) => {
				this.matches = [...this.matches, ...response.content];
				this.currentPage = response.page.number;
				this.hasMore = response.page.totalPages > this.currentPage + 1;
			},
			error: (_) => this.errorMessage = 'Error trying to show the matches'
		});
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.loadMoreGames(this.currentPage + 1);
		}
	}
}