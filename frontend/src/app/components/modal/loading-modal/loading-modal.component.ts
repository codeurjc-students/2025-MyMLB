import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
	selector: 'app-loading-modal',
	standalone: true,
	imports: [CommonModule, MatProgressSpinnerModule],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './loading-modal.component.html'
})
export class LoadingModalComponent {
	@Input() message = '';
}