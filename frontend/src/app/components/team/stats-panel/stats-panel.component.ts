import { PositionPlayer } from './../../../models/position-player.model';
import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { TeamInfo } from '../../../models/team-info.model';
import { Pitcher } from '../../../models/pitcher.model';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-stats-panel',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './stats-panel.component.html',
})
export class StatsPanelComponent {
	@Input() teamStats: boolean = true;
	@Input() team: TeamInfo | null = null;
	@Input() teamRank: number = -1;
	@Input() player!: Pitcher | PositionPlayer;
	@Output() close = new EventEmitter<void>();

	closePanel() {
		this.close.emit();
	}

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.closePanel();
		}
	}

	isPitcher(player: Pitcher | PositionPlayer): player is Pitcher {
		return 'era' in player;
	}

	isPositionPlayer(player: Pitcher | PositionPlayer): player is PositionPlayer {
		return 'atBats' in player;
	}
}