import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
	selector: 'app-back-to-dashboard-button',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [CommonModule, MatIconModule],
	templateUrl: './back-to-dashboard-button.component.html'
})
export class BackToDashboardButtonComponent {
	@Output() backToStatsDashboard = new EventEmitter<void>();

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}