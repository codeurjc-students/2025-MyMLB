import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostListener, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchService } from '../../../services/search.service';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { Team, TeamInfo } from '../../../models/team.model';
import { Stadium } from '../../../models/stadium.model';
import { PositionPlayerGlobal } from '../../../models/position-player.model';
import { PitcherGlobal } from '../../../models/pitcher.model';
import { BackgroundColorService } from '../../../services/background-color.service';
import { EditStadiumComponent } from "../edit-stadium/edit-stadium.component";
import { EditTeamComponent } from "../edit-team/edit-team.component";
import { EditPlayerComponent } from "../edit-player/edit-player.component";
import { ValidationService } from '../../../services/utilities/validation.service';

@Component({
	selector: 'app-edit-menu',
	standalone: true,
	imports: [CommonModule, FormsModule, ErrorModalComponent, EditStadiumComponent, EditTeamComponent, EditPlayerComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './edit-menu.component.html',
})
export class EditMenuComponent implements OnInit {
	public searchQuery: string = '';
	public searchType: 'player' | 'team' | 'stadium' = 'team';
	public playerType: 'position' | 'pitcher' | null = null;
	public searchResults: (TeamInfo | Stadium | PositionPlayerGlobal | PitcherGlobal)[] = [];
	public error: boolean = false;
	public errorMessage = '';
	public currentPage = 0;
    public readonly pageSize = 10;
    public hasMore = true;
	public lastSearch = '';
	public noResultsMessage = '';
	public isSearchTypeOpen = false;
	public isPlayerTypeOpen = false;
	public currentView: 'team' | 'stadium' | 'player' | null = null;
	public selectedResult!: Team | Stadium | PositionPlayerGlobal | PitcherGlobal;

	constructor(private searchService: SearchService, public backgroundService: BackgroundColorService, public validationService: ValidationService) {}

	ngOnInit(): void {}

	public performSearch(page: number = 0): void {
		if (!this.searchQuery.trim()) {
			this.error = true;
			this.errorMessage = 'Please enter the team, stadium or player you want to edit';
			return;
		}

		if (this.searchType === 'player' && !this.playerType) {
			this.error = true;
			this.errorMessage = 'Please select the type of player you want to edit';
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
					if (results.content.length === 0) {
						this.noResultsMessage = 'No results were found';
					}
					else {
						this.noResultsMessage = '';
					}
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

	@HostListener('document:keydown', ['$event'])
	public handleEnter(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			this.performSearch();
		}
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.performSearch(this.currentPage + 1);
		}
	}

	public edit(result: any): void {
		if (this.validationService.isTeamInfo(result)) {
			this.currentView = 'team';
		}
		else if (this.validationService.isStadium(result)) {
			this.currentView = 'stadium';
		}
		else if (this.validationService.isPositionPlayer(result) || this.validationService.isPitcher(result)) {
			this.currentView = 'player';
		}
		this.selectedResult = result;
	}

	public toggleSearchType(state: boolean) {
		this.isSearchTypeOpen = state;
	}

	public togglePlayerType(state: boolean) {
		this.isPlayerTypeOpen = state;
	}
}