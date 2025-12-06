import { Directive, HostListener, Input } from '@angular/core';

@Directive({
	selector: '[appEscapeClose]',
})
export class EscapeCloseDirective {
	@Input('appEscapeClose') closeFunc!: () => void;

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape' && this.closeFunc) {
			this.closeFunc();
		}
	}
}