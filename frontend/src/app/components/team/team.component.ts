import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TeamInfo } from '../../models/team-info.model';
import { SelectedTeamService } from '../../services/selected-team.service';
import { PositionPlayer } from '../../models/position-player.model';
import { Pitcher } from '../../models/pitcher.model';
import { TeamService } from '../../services/team.service';
import { BackgroundColorService } from '../../services/background-color.service';
import { StatsPanelComponent } from './stats-panel/stats-panel.component';

@Component({
	selector: 'app-team',
	standalone: true,
	imports: [CommonModule, StatsPanelComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './team.component.html',
})
export class TeamComponent implements OnInit {
	public team: TeamInfo | null = null;
	public positionPlayers: PositionPlayer[] = [];
	public pitchers: Pitcher[] = [];
	public championships: number[] = [];
	public teamRank: number = 0;

	@ViewChild('carousel') carousel!: ElementRef;

	public stadiumImages: string[] = [
		'/assets/landing/hero.png',
		'/assets/landing/hero-dark.png',
		'/assets/landing/matches.png',
		'/assets/landing/standings.png',
		'/assets/landing/teams.png',
	];
	public currentIndex: number = 0;
	private autoplayInterval: any;
	public isTransitioning: boolean = false;

	public selectedPlayer: Pitcher | PositionPlayer | null = null;

	constructor(
		private selectedTeamService: SelectedTeamService,
		private teamService: TeamService,
		private backgroundColorService: BackgroundColorService
	) {}

	ngOnInit() {
		this.selectedTeamService.selectedTeam$.subscribe((selectedTeam) => {
			this.team = selectedTeam;
			this.positionPlayers = selectedTeam?.positionPlayers ?? [];
			this.pitchers = selectedTeam?.pitchers ?? [];
			this.championships = selectedTeam?.championships ?? [];

			this.currentIndex = 0;
			this.restartAutoplay();

			if (selectedTeam?.teamStats?.abbreviation) {
				this.teamService
					.getTeamDivisionRank(selectedTeam.teamStats.abbreviation)
					.subscribe((rank) => {
						this.teamRank = rank;
					});
			}
		});
	}

	ngOnDestroy() {
		clearInterval(this.autoplayInterval);
	}

	public backgroundLogo(abbreviation: string | undefined) {
		return this.backgroundColorService.getBackgroundColor(abbreviation);
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
}