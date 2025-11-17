import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';

@Component({
	selector: 'app-close-button',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './close-button.component.html'
})
export class CloseButtonComponent {
	@Output() close = new EventEmitter<boolean>();

	public closeModal() {
		this.close.emit(true);
	}

	@HostListener('document:keydown', ['$event'])
	public handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.closeModal();
		}
	}
}