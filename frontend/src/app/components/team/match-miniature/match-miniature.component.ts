import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ShowMatch } from '../../../services/match.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { CommonModule } from '@angular/common';
import { EscapeCloseDirective } from '../../../directives/escape-close.directive';

@Component({
	selector: 'app-match-miniature',
	standalone: true,
	imports: [CommonModule, EscapeCloseDirective],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './match-miniature.component.html',
})
export class MatchMiniatureComponent {
	@Input() match!: ShowMatch;
	@Input() showMatchMiniature!: boolean;
	@Input() homeMatch!: boolean;
	@Output() close = new EventEmitter<boolean>();

	public isClose = false;

	constructor(public backgroundService: BackgroundColorService) {}

	public closeMatchMiniature = () => {
		this.isClose = true;
		setTimeout(() => {
			this.close.emit(false);
			this.isClose = false;
		}, 300);
	};
}