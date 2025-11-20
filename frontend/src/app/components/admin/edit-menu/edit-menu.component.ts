import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchService } from '../../../services/search.service';
import { RemoveConfirmationModalComponent } from "../../remove-confirmation-modal/remove-confirmation-modal.component";
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { Team } from '../../../models/team.model';
import { Stadium } from '../../../models/stadium.model';
import { PositionPlayerGlobal } from '../../../models/position-player.model';
import { PitcherGlobal } from '../../../models/pitcher.model';
import { BackgroundColorService } from '../../../services/background-color.service';
import { PaginatedSearchs } from '../../../models/pagination.model';

@Component({
	selector: 'app-edit-menu',
	standalone: true,
	imports: [CommonModule, FormsModule, ErrorModalComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './edit-menu.component.html',
})
export class EditMenuComponent implements OnInit {
	public searchQuery: string = '';
	public searchType: 'player' | 'team' | 'stadium' = 'team';
	public playerType: 'position' | 'pitcher' | null = null;
	public searchResults: (Team | Stadium | PositionPlayerGlobal | PitcherGlobal)[] = [];
	public error: boolean = false;
	public errorMessage = '';
	public currentPage = 0;
    public readonly pageSize = 10;
    public hasMore = true;
	public lastSearch = '';

	constructor(private searchService: SearchService, private backgroundService: BackgroundColorService) {}

	ngOnInit(): void {}

	public performSearch(page: number = 0): void {
		if (!this.searchQuery.trim()) return;

		if (this.searchType === 'player' && !this.playerType) {
			this.error = true;
			this.errorMessage = 'Please select the type of player';
			return;
		}

		const currentType = `${this.searchType}-${this.playerType ?? ''}`;
		const isNewSearch = page === 0 || currentType !== this.lastSearch;

		if (isNewSearch) {
			this.searchResults = [];
			this.currentPage = 0;
			this.hasMore = true;
			this.lastSearch = currentType;
		}

		this.searchService
			.search(this.searchType, this.searchQuery, page, this.pageSize, this.playerType ?? undefined)
			.subscribe({
				next: (results) => {
					if (page === 0) {
						this.searchResults = results.content;
					}
					else {
						this.searchResults = [...this.searchResults, ...results.content];
					}
					this.currentPage = results.page.number;
					this.hasMore = results.page.totalPages > this.currentPage + 1;
				},
				error: (_) => {
					this.error = true;
					this.errorMessage = 'An error occur during the search';
				}
			});
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.performSearch(this.currentPage + 1);
		}
	}

	public edit(result: any): void {
		console.log('Edit:', result);
	}

	public isPitcher(obj: any): obj is PitcherGlobal {
		return 'era' in obj && 'inningsPitched' in obj;
	}

	public isPositionPlayer(obj: any): obj is PositionPlayerGlobal {
		return 'atBats' in obj && 'homeRuns' in obj;
	}

	public isTeam(obj: any): obj is Team {
		return 'division' in obj && 'league' in obj;
	}

	public isStadium(obj: any): obj is Stadium {
		return 'openingDate' in obj && 'pictures' in obj;
	}

	public getBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}
}