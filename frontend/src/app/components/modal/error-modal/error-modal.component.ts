import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EscapeCloseDirective } from '../../../directives/escape-close.directive';

@Component({
	selector: 'app-error-modal',
	standalone: true,
	imports: [CommonModule, EscapeCloseDirective],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './error-modal.component.html',
})
export class ErrorModalComponent implements OnInit {
	@Input() title = '';
	@Output() cancel = new EventEmitter<void>();

	public isClosing = false;

	ngOnInit(): void {
		this.handleCancel = this.handleCancel.bind(this);
	}

	handleCancel() {
        this.isClosing = true;
		setTimeout(() => {
			this.cancel.emit();
			this.isClosing = false;
		}, 300);
    }
}