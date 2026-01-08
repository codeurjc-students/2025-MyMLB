import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
	selector: 'app-profile-buttons',
	imports: [],
	templateUrl: './profile-buttons.component.html'
})
export class ProfileButtons {
	@Input() content! : string;
	@Output() confirm = new EventEmitter<void>();

	public handleConfirm() {
		this.confirm.emit();
	}
}