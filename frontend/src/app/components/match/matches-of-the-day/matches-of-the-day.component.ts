import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ShowMatch } from '../../../models/match.model';
import { CommonModule } from '@angular/common';
import { BackgroundColorService } from '../../../services/background-color.service';
import { MatchService } from '../../../services/match.service';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
	selector: 'app-matches-of-the-day',
	standalone: true,
	imports: [CommonModule, MatTooltipModule, MatIconModule, MatProgressSpinnerModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './matches-of-the-day.component.html'
})
export class MatchesOfTheDayComponent implements OnInit {
	private matchService = inject(MatchService);
	public backgroundService = inject(BackgroundColorService);

	public matches: ShowMatch[] = [];
    public errorMessage = '';
    public currentPage = 0;
    public readonly pageSize = 10;
    public hasMore = true;
	public loading = false;

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

	public refreshTodayGames() {
		this.loading = true;
		this.matchService.refreshMatches('today').subscribe({
			next: (_) => {
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.errorMessage = `An error occur refreshing the matches: ${err.message}`;
			}
		});
	}
}