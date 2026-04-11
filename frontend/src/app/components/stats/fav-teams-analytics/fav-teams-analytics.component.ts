import { ChangeDetectionStrategy, Component, EventEmitter, inject, OnInit, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatIconModule } from '@angular/material/icon';
import { AnalyticsService } from '../../../services/analytics.service';
import { ExportService } from '../../../services/utilities/export.service';
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { BackToDashboardButtonComponent } from "../back-to-dashboard-button/back-to-dashboard-button.component";
import { DonwloadButtonComponent } from '../donwload-button/donwload-button.component';

@Component({
	selector: 'app-fav-teams-analytics',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		BaseChartDirective,
		MatIconModule,
		ErrorModalComponent,
		LoadingModalComponent,
		BackToDashboardButtonComponent,
		DonwloadButtonComponent
	],
	templateUrl: './fav-teams-analytics.component.html'
})
export class FavTeamsAnalyticsComponent implements OnInit {
	private analyticsService = inject(AnalyticsService);
	private exportService = inject(ExportService);

	@Output() backToStatsDashboard = new EventEmitter<void>();

	@ViewChild(BaseChartDirective) chart?: BaseChartDirective;

	public barChartData: ChartData<'bar'> = {
		labels: [],
		datasets: [
			{
				data: [],
				label: 'Favorites per Team',
				backgroundColor: '#6366F1',
				hoverBackgroundColor: '#4F46E5',
				borderRadius: 6
			}
		]
	};

	public barChartOptions: ChartConfiguration['options'] = {
		responsive: true,
		maintainAspectRatio: false,
		scales: {
			x: {
				grid: { display: false },
				ticks: { color: '#9CA3AF', font: { size: 12 } },
				title: {
					display: true,
					text: 'Teams',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'system-ui'
					},
					padding: { top: 10 }
				}
			},
			y: {
				beginAtZero: true,
				ticks: {
					stepSize: 1,
					color: '#9CA3AF'
				},
				grid: { color: 'rgba(255,255,255,0.05)' },
				title: {
					display: true,
					text: 'Fans',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'sytem-ui'
					},
					padding: { bottom: 10 }
				}
			}
		},
		plugins: {
			legend: { display: false },
			tooltip: {
				backgroundColor: 'rgba(17, 24, 39, 0.9)',
				padding: 12,
				cornerRadius: 8
			}
		}
	};

	public barChartType: ChartType = 'bar';

	public error = false;
	public errorMessage = '';
	public loading = false;

	ngOnInit() {
		this.fetchData();
	}

	private fetchData() {
		this.loading = true;
		this.analyticsService.getFavTeamsAnalytics().subscribe({
			next: (data: Record<string, number>) => {
				this.barChartData.labels = Object.keys(data);
            	this.barChartData.datasets[0].data = Object.values(data);

				this.chart?.update();
				this.loading = false;
			},
			error: (err) => {
				this.loading = false;
				this.error = true;
				this.errorMessage = `Error fetching the data: ${err.error.message}`;
			}
		});
	}

	public downloadChartAsPNG() {
		const canvas = this.chart?.chart?.canvas;
		if (canvas) {
			const date = new Date().toISOString().split('T')[0];
			const fileName = `Favorite_Teams_Analytics_${date}`;
			this.exportService.downloadPNG(canvas, fileName);
		}
	}

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}