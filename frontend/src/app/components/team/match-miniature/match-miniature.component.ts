import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { ShowMatch } from '../../../services/match.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-match-miniature',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './match-miniature.component.html',
})
export class MatchMiniatureComponent {
	@Input() match!: ShowMatch;
	@Output() close = new EventEmitter<void>();

	constructor(private bakcgroundColor: BackgroundColorService) {}

	public closeMatchMiniature() {
		this.close.emit();
	}

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.closeMatchMiniature();
		}
	}

	public getBackgroundColor(abbreviation: string | undefined) {
		return this.bakcgroundColor.getBackgroundColor(abbreviation);
	}
}