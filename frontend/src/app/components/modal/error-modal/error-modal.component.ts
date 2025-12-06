import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { EventService } from '../../../services/utilities/event.service';

@Component({
	selector: 'app-error-modal',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './error-modal.component.html',
})
export class ErrorModalComponent {
	@Input() title = '';
	@Output() cancel = new EventEmitter<void>();

	public isClosing = false;

	constructor(public eventService: EventService) {}

	handleCancel() {
        this.isClosing = true;
		setTimeout(() => {
			this.cancel.emit();
			this.isClosing = false;
		}, 300);
    }

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.handleCancel();
		}
	}
}