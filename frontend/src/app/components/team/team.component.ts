import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { TeamInfo } from '../../models/team.model';
import { SelectedTeamService } from '../../services/selected-team.service';
import { PositionPlayer } from '../../models/position-player.model';
import { Pitcher } from '../../models/pitcher.model';
import { TeamService } from '../../services/team.service';
import { BackgroundColorService } from '../../services/background-color.service';
import { StatsPanelComponent } from './stats-panel/stats-panel.component';
import { CalendarComponent } from "./calendar/calendar.component";
import { Pictures } from '../../models/pictures.model';
import { MatchService, ShowMatch } from '../../services/match.service';
import { PaginatedSelectorService } from '../../services/utilities/paginated-selector.service';
import { Router } from '@angular/router';

@Component({
	selector: 'app-team',
	standalone: true,
	imports: [CommonModule, StatsPanelComponent, CalendarComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './team.component.html',
})
export class TeamComponent implements OnInit {
	private matchService = inject(MatchService);
	public paginationHandlerService = inject(PaginatedSelectorService);
	private router = inject(Router);

	public team: TeamInfo | null = null;
	public positionPlayers: PositionPlayer[] = [];
	public pitchers: Pitcher[] = [];
	public championships: number[] = [];
	public teamRank: number = 0;
	public homeMatches: ShowMatch[] = [];

	@ViewChild('carousel') carousel!: ElementRef;

	public stadiumImages: Pictures[] = [];
	public currentIndex: number = 0;
	private autoplayInterval: any;
	public isTransitioning: boolean = false;

	public selectedPlayer: Pitcher | PositionPlayer | null = null;

	public showCalendar = false;

	public readonly PAGE_SIZE = 5;

	constructor(
		private selectedTeamService: SelectedTeamService,
		private teamService: TeamService,
		public backgroundService: BackgroundColorService
	) {}

	ngOnInit() {
		this.selectedTeamService.selectedTeam$.subscribe((selectedTeam) => {
			this.team = selectedTeam;
			this.positionPlayers = selectedTeam?.positionPlayers ?? [];
			this.pitchers = selectedTeam?.pitchers ?? [];
			this.championships = selectedTeam?.championships ?? [];
			this.stadiumImages = selectedTeam?.stadium.pictures ?? [];

			this.currentIndex = 0;
			this.restartAutoplay();

			if (selectedTeam?.teamStats?.abbreviation) {
				this.teamService
					.getTeamDivisionRank(selectedTeam.teamStats.abbreviation)
					.subscribe((rank) => {
						this.teamRank = rank;
					});
			}

			if (this.team) {
				this.resetMatches();
			}
		});
	}

	ngOnDestroy() {
		clearInterval(this.autoplayInterval);
	}

	public scrollCarousel(direction: 'left' | 'right') {
		this.restartAutoplay();
		const total = this.stadiumImages.length;
		this.isTransitioning = true;

		// Fade out
		setTimeout(() => {
			this.currentIndex =
				direction === 'left'
					? (this.currentIndex - 1 + total) % total
					: (this.currentIndex + 1) % total;

			// Fade in
			setTimeout(() => {
				this.isTransitioning = false;
			}, 50);
		}, 300);
	}

	public goToSlide(index: number) {
		this.restartAutoplay();
		this.isTransitioning = true;

		setTimeout(() => {
			this.currentIndex = index;

			setTimeout(() => {
				this.isTransitioning = false;
			}, 50);
		}, 300);
	}

	private startAutoplay() {
		this.autoplayInterval = setInterval(() => {
			this.scrollCarousel('right');
		}, 4000);
	}

	private restartAutoplay() {
		clearInterval(this.autoplayInterval);
		this.startAutoplay();
	}

	public selectPlayer(player: Pitcher | PositionPlayer) {
		this.selectedPlayer = player;
	}

	public openCalendar() {
		this.showCalendar = true;
	}

	private resetMatches() {
		this.paginationHandlerService.clearAll();
		this.loadNextMatches();
	}

	public loadNextMatches() {
		const fetch = (page: number, size: number) => {
			return this.matchService.getMatchesOfATeam(this.team?.teamStats.name, 'home', page, size);
		}
		this.paginationHandlerService.loadNextPage(this.PAGE_SIZE, fetch);
	}

	public showLess() {
		this.resetMatches();
	}

	public goToPurchaseTickets(matchId: number) {
		this.router.navigate(['tickets'], {
			queryParams: {
				matchId: matchId
			}
		});
	}
}