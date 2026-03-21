import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MatIconModule } from "@angular/material/icon";

@Component({
	selector: 'app-donwload-button',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [CommonModule, MatTooltipModule, MatIconModule],
	templateUrl: './donwload-button.component.html'
})
export class DonwloadButtonComponent {
	@Output() downloadFunc = new EventEmitter<void>;

	public downloadPngFunction() {
		this.downloadFunc.emit();
	}
}