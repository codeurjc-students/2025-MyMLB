import { Component, Input, Output, EventEmitter, HostListener } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-remove-confirmation-modal',
    standalone: true,
    imports: [],
    templateUrl: './remove-confirmation-modal.component.html',
})
export class RemoveConfirmationModalComponent {
    @Input() title = '';
    @Input() contentMessage = '';
    @Input() buttonContent = 'Confirm';

    safeSvg: SafeHtml = '';
    @Input() set svg(value: string) {
        this.safeSvg = this.sanitizer.bypassSecurityTrustHtml(value);
    }

    @Output() confirm = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

    constructor(private sanitizer: DomSanitizer) {}

    handleCancel() {
        this.cancel.emit();
    }

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.handleCancel();
		}
	}

    handleConfirm() {
        this.confirm.emit();
    }
}