import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
	selector: 'app-action-buttons',
	standalone: true,
	imports: [CommonModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './action-buttons.component.html'
})
export class ActionButtonsComponent {
	@Output() confirm = new EventEmitter<void>();
	@Output() goBack = new EventEmitter<void>();

	public confirmButton() {
		this.confirm.emit();
	}

	public goBackButton() {
		this.goBack.emit();
	}
}