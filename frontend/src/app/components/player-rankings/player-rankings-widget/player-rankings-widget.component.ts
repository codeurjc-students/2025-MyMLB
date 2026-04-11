import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PlayerService } from '../../../services/player.service';
import { PlayerRanking } from '../../../models/position-player.model';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { MatTooltip } from "@angular/material/tooltip";
import { AuthService } from '../../../services/auth.service';

@Component({
	selector: 'app-player-rankings-widget',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [CommonModule, FormsModule, MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatIconModule, MatTooltip],
	templateUrl: './player-rankings-widget.component.html'
})
export class PlayerRankingsWidgetComponent implements OnInit {
	private playerService = inject(PlayerService);
	private authService = inject(AuthService);
	private router = inject(Router);
	public Object = Object;

	@Input() playerType!: string;

	public ranking: PlayerRanking[] = [];
	public availablePositionPlayerStats: Record<string, string> = {
		'AB': 'atBats',
		'H': 'hits',
		'BB': 'walks',
		'HR': 'homeRuns',
		'RBI': 'rbis',
		'AVG': 'average',
		'OBP': 'obp',
		'OPS': 'ops',
		'2B': 'doubles',
		'3B': 'triples',
		'SLG': 'slugging'
	};
	public availablePitcherStats: Record<string, string> = {
		'G': 'games',
		'ERA': 'era',
		'W': 'wins',
		'L': 'losses',
		'IP': 'inningsPitched',
		'SO': 'totalStrikeouts',
		'BB': 'walks',
		'HA': 'hitsAllowed',
		'RA': 'runsAllowed',
		'S': 'saves',
		'SOPP': 'saveOpportunities',
		'WHIP': 'whip'
	};
	public isPitcher = false;
	public selectedStat: string = '';
	public currentUser$ = this.authService.currentUser$;

	public loading = false;
	public error = false;
	public errorMessage = '';

	ngOnInit() {
		this.isPitcher = this.playerType === 'pitcher';
		this.selectedStat = (this.isPitcher) ? 'ERA' : 'AVG';
		this.loadRanking();
	}

	private loadRanking() {
		this.loading = true;
		const statDBValue = (this.isPitcher) ? this.availablePitcherStats[this.selectedStat] : this.availablePositionPlayerStats[this.selectedStat];
		this.playerService.getPlayerSingleStatRankings(0, 5, this.playerType, statDBValue).subscribe({
			next: (response) => {
				this.ranking = response.content;
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur fetching the ranking: ${err.message}`;
			}
		});
	}

	public onStatFilterChange(newStat: string) {
		this.selectedStat = newStat;
		this.loadRanking();
	}

	public statFormatter(value: number, stat: string) {
		if (!value && value !== 0) {
			return '-';
		}
		const decimalStats = ['AVG', 'OBP', 'SLG', 'OPS', 'ERA', 'WHIP'];
		if (decimalStats.includes(stat)) {
			const formatted = value.toFixed(3);
			if (['AVG', 'OBP', 'SLG', 'OPS'].includes(stat)) {
				return formatted.startsWith('0') ? formatted.substring(1) : formatted;
			}
			return value.toFixed(2);
		}
		return value.toString();
	}

	public goToFullRankings() {
		this.router.navigate(['player-rankings'], {
			queryParams: {
				playerType: this.playerType
			}
		});
	}

	public refresh() {
		this.loading = true;
		this.playerService.refreshPlayerRankings().subscribe({
			next: (_) => {
				this.loading = false;
				this.ranking = [];
				this.loadRanking();
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `An error occur while refreshing the ranking: ${err.message}`;
			}
		});
	}
}