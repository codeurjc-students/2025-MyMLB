import { ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ShowMatch } from '../../../services/match.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { CommonModule } from '@angular/common';
import { EscapeCloseDirective } from '../../../directives/escape-close.directive';
import { Router } from '@angular/router';

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

	private router = inject(Router);

	public isClose = false;

	constructor(public backgroundService: BackgroundColorService) {}

	public closeMatchMiniature = () => {
		this.isClose = true;
		setTimeout(() => {
			this.close.emit(false);
			this.isClose = false;
		}, 300);
	};

	public goToPurchaseTicket() {
		this.router.navigate(['tickets'], {
			queryParams: {
				matchId: this.match.id
			}
		});
	}
}