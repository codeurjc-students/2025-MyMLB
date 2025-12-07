import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { EscapeCloseDirective } from "../../directives/escape-close.directive";

@Component({
    selector: 'app-remove-confirmation-modal',
    standalone: true,
    imports: [CommonModule, EscapeCloseDirective],
    templateUrl: './remove-confirmation-modal.component.html',
})
export class RemoveConfirmationModalComponent {
    @Input() title = '';
    @Input() contentMessage = '';
    @Input() buttonContent = 'Confirm';
	@Input() isDelete!: boolean;

    @Output() confirm = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

	public isClosing = false;

    public handleCancel = () => {
        this.isClosing = true;
		setTimeout(() => {
			this.cancel.emit();
			this.isClosing = false;
		}, 300);
    }

    handleConfirm() {
        this.confirm.emit();
    }
}