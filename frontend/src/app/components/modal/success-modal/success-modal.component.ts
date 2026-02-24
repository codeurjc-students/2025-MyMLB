import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter , Input, Output } from '@angular/core';

@Component({
	selector: 'app-success-modal',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './success-modal.component.html',
})
export class SuccessModalComponent {
	@Input() message = '';
	@Output() continue = new EventEmitter<void>();

    handleConfirm() {
        this.continue.emit();
    }
}