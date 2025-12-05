import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { MatchService, ShowMatch } from '../../../services/match.service';
import { TeamInfo } from '../../../models/team.model';
import { BackgroundColorService } from '../../../services/background-color.service';
import { subMonths, addMonths, startOfMonth, endOfMonth, eachDayOfInterval, startOfWeek, endOfWeek } from 'date-fns';
import { CommonModule } from '@angular/common';
import { MatchMiniatureComponent } from "../match-miniature/match-miniature.component";

@Component({
	selector: 'app-calendar',
	standalone: true,
	imports: [CommonModule, MatchMiniatureComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './calendar.component.html'
})
export class CalendarComponent implements OnInit {
	@Input() team!: TeamInfo;
	@Output() close = new EventEmitter<void>();

	public viewDate: Date = new Date();
	public errorMessage = '';

	private matchesByDay = new Map<string, ShowMatch[]>();

	public showMatchInfo = false;
	public selectedMatch!: ShowMatch;
	public isHomeMatch = false;

	constructor(
		private matchService: MatchService,
		public backgroundService: BackgroundColorService
	) {}

	ngOnInit() {
		this.matchService.getHomeMatches(this.team.teamStats.name).subscribe({
			next: (home) => {
				this.indexMatches(home);
			},
			error: () => (this.errorMessage = 'Error trying to load the home matches'),
		});

		this.matchService.getAwayMatches(this.team.teamStats.name).subscribe({
			next: (away) => {
				this.indexMatches(away);
			},
			error: () => (this.errorMessage = 'Error trying to load the away matches'),
		});
	}

	public closeCalendar() {
		this.close.emit();
	}

	private indexMatches(matches: ShowMatch[]) {
		matches.forEach((m) => {
			const key = new Date(m.date).toDateString();
			if (!this.matchesByDay.has(key)) {
				this.matchesByDay.set(key, []);
			}
			this.matchesByDay.get(key)!.push(m);
		});
	}

	public getDaysInMonth(date: Date): Date[] {
		return eachDayOfInterval({ start: startOfMonth(date), end: endOfMonth(date) });
	}

	public getFullMonthGrid(date: Date): Date[] {
		const start = startOfWeek(startOfMonth(date), { weekStartsOn: 0 });
		const end = endOfWeek(endOfMonth(date), { weekStartsOn: 0 });
		return eachDayOfInterval({ start, end });
	}

	public getMatchesOfDay(day: Date): ShowMatch[] {
		return this.matchesByDay.get(day.toDateString()) ?? [];
	}

	public getCellClass(day: Date): string {
		const matches = this.getMatchesOfDay(day);
		const homeMatch = matches.find((m) => m.homeTeam.name === this.team.teamStats.name);
		if (homeMatch) {
			return this.backgroundService.getBackgroundColor(this.team.teamStats.abbreviation);
		}
		return 'bg-white dark:bg-gray-400';
	}

	public previousMonth() {
		this.viewDate = subMonths(this.viewDate, 1);
	}

	public nextMonth() {
		this.viewDate = addMonths(this.viewDate, 1);
	}

	public isOutsideCurrentMonth(day: Date): boolean {
		return day.getMonth() !== this.viewDate.getMonth();
	}

	public isDarkBackground(day: Date): boolean {
		const bgClass = this.getCellClass(day);
		return bgClass.includes('bg-black') || bgClass.includes('bg-gray-900') || bgClass.includes('bg-slate-900');
	}

	public openMatchInfoModal(match: ShowMatch) {
		this.selectedMatch = match;
		this.showMatchInfo = true;

		const homeName = this.selectedMatch?.homeTeam?.name;
		const teamName = this.team?.teamStats?.name;

		this.isHomeMatch = homeName === teamName;
	}

	public getLogoPath(match: ShowMatch): string {
		const team = match.homeTeam.name === this.team.teamStats.name ? match.awayTeam : match.homeTeam;
		return `assets/team-logos-new/${team.league}/${team.division}/${team.abbreviation}.png`;
	}

	public getAbbreviation(match: ShowMatch): string {
		const team = match.homeTeam.name === this.team.teamStats.name ? match.awayTeam : match.homeTeam;
		return team.abbreviation;
	}

	public getLogoBackground(match: ShowMatch): string {
		const abbreviation = this.getAbbreviation(match);
		const darkLogos = ['DET', 'TB', 'KC', 'SD', 'MIA'];
		return darkLogos.includes(abbreviation) ? 'bg-white' : 'bg-transparent';
	}
}