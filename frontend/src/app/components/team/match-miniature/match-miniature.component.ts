import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ShowMatch } from '../../../services/match.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { CommonModule } from '@angular/common';
import { CloseButtonComponent } from "../../close-button/close-button.component";

@Component({
	selector: 'app-match-miniature',
	standalone: true,
	imports: [CommonModule, CloseButtonComponent],
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

	public closeMatchMiniature(trigger: boolean) {
		if (trigger) {
			this.isClose = true;
			setTimeout(() => {
				this.close.emit(false);
				this.isClose = false;
			}, 300);
		}
	}
}