import { PositionPlayer } from './../../../models/position-player.model';
import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { TeamInfo } from '../../../models/team.model';
import { Pitcher } from '../../../models/pitcher.model';
import { CommonModule } from '@angular/common';
import { ValidationService } from '../../../services/utilities/validation.service';
import { EscapeCloseDirective } from "../../../directives/escape-close.directive";

@Component({
	selector: 'app-stats-panel',
	standalone: true,
	imports: [CommonModule, EscapeCloseDirective],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './stats-panel.component.html',
})
export class StatsPanelComponent {
	@Input() teamStats: boolean = true;
	@Input() team: TeamInfo | null = null;
	@Input() teamRank: number = -1;
	@Input() player!: Pitcher | PositionPlayer;
	@Output() close = new EventEmitter<void>();

	constructor(public validationService: ValidationService) {}

	public closePanel = () => {
		this.close.emit();
	}
}