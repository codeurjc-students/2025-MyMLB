import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { AnalyticsCards } from '../../../models/analytics.model';
import { MatIconModule } from '@angular/material/icon';

@Component({
	selector: 'app-analytics-cards',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [CommonModule, MatIconModule,],
	templateUrl: './analytics-cards.component.html'
})
export class AnalyticsCardsComponent {
	@Input() data!: AnalyticsCards[];
}