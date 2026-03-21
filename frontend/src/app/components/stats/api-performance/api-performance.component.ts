import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, inject, OnInit, Output, ViewChild } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { LoadingModalComponent } from '../../modal/loading-modal/loading-modal.component';
import { BackToDashboardButtonComponent } from '../back-to-dashboard-button/back-to-dashboard-button.component';
import { DonwloadButtonComponent } from '../donwload-button/donwload-button.component';
import { AnalyticsService } from '../../../services/analytics.service';
import { ExportService } from '../../../services/utilities/export.service';
import { APIAnalytics, TimeRange } from '../../../models/analytics.model';
import { ChartConfiguration, ChartData } from 'chart.js';
import { take } from 'rxjs';

@Component({
	selector: 'app-api-performance',
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
	templateUrl: './api-performance.component.html'
})
export class ApiPerformanceComponent implements OnInit {
	private analyticsService = inject(AnalyticsService);
	private exportService = inject(ExportService);

	@Output() backToStatsDashboard = new EventEmitter<void>();
	@ViewChild(BaseChartDirective) chart?: BaseChartDirective;

	public apiAnalytics?: APIAnalytics;
	public selectedDateRange: TimeRange = '1h';
	public totalErrorRate = 0.0;

	public performanceChartData: ChartData<'line'> = {
		labels: [],
		datasets: [
			{
				data: [],
				label: 'Latency (ms)',
				borderColor: '#F59E0B',
				backgroundColor: 'rgba(245, 158, 11, 0.1)',
				fill: true,
				tension: 0.4
			},
			{
				data: [],
				label: 'Requests',
				borderColor: '#6366F1',
				backgroundColor: 'rgba(99, 102, 241, 0.1)',
				fill: true,
				tension: 0.4
			}
		]
	};

	public endpointsChartData: ChartData<'bar'> = {
		labels: [],
		datasets: [{
			data: [],
			label: 'Hits',
			backgroundColor: '#10B981',
			borderRadius: 8,
			barThickness: 20
		}]
	};

	public errorsChartData: ChartData<'pie'> = {
		labels: ['Success (2xx/3xx)', 'Errors (4xx/5xx)'],
		datasets: [{
			data: [0, 0],
			backgroundColor: ['#10B981', '#EF4444'],
			hoverBackgroundColor: ['#059669', '#DC2626'],
            borderWidth: 0
		}]
	};

	public lineOptions: ChartConfiguration['options'] = {
        responsive: true, maintainAspectRatio: false,
        scales: {
			x: {
				display: true,
				grid: { display: false },
				ticks: { color: '#94a3b8', maxRotation: 45, minRotation: 45 },
				title: {
					display: true,
					text: 'Timeline (HH:mm)',
					color: '##6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'system-ui'
					}
				}
			},
			y: {
				grid: { color: 'rgba(255,255,255,0.05)' },
				ticks: { color: '#94a3b8' },
				title: {
					display: true,
					text: 'Value (ms / count)',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'sytem-ui'
					},
					padding: { bottom: 10 }
				}
			}
		}
    };

    public pieOptions: ChartConfiguration['options'] = {
        responsive: true,
		maintainAspectRatio: false,
        plugins: {
			legend: { position: 'bottom',
				labels: { color: '#94a3b8', padding: 20 }
			}
		}
    };

    public barOptions: ChartConfiguration['options'] = {
        responsive: true,
		maintainAspectRatio: false,
		indexAxis: 'y',
        scales: {
			x: {
				grid: { display: false },
				ticks: { color: '#94a3b8' },
				title: {
					display: true,
					text: 'Total Requests (Hits)',
					color: '##6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'system-ui'
					}
				}
			},
			y: {
				grid: { display: false },
				ticks: { color: '#94a3b8' },
				title: {
					display: true,
					text: 'Endpoints',
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
        plugins: { legend: { display: false } }
    };

	public error = false;
	public loading = false;
	public errorMessage = '';

	ngOnInit() {
		this.fetchData();
	}

	public onDateRangeChange(range: string) {
		const formattedRange = range as TimeRange;
		this.selectedDateRange = formattedRange;
		this.fetchData();
	}

	private fetchData() {
		this.loading = true;
		this.analyticsService.getAPIPerformanceHistory(this.selectedDateRange).pipe(take(1)).subscribe({
			next: (data : APIAnalytics[]) => {
				if (data && data.length > 0) {
					this.updateAllCharts(data);
				}
				this.loading = false;
			},
			error: (err) => {
				this.error = true;
				this.errorMessage = `Error fecthing the data: ${err.message}`;
				this.loading = false;
			}
		});
	}

	private updateAllCharts(data: APIAnalytics[]) {
		this.updatePerformanceChart(data);
		this.updateErrorsChart(data);
		this.updateEndpointsChart(data);

		this.chart?.update();
	}

	private updatePerformanceChart(data: APIAnalytics[]) {
		this.performanceChartData = {
			...this.performanceChartData,
			labels: data.map(d => this.timeFormatter(d.timeStamp)),
			datasets: [
				{
					...this.performanceChartData.datasets[0],
					data: data.map(d => d.averageResponseTime)
				},
				{
					...this.performanceChartData.datasets[1],
					data: data.map(d => d.totalRequests)
				}
			]
		};
	}

	private updateErrorsChart(data: APIAnalytics[]) {
		const success = data.reduce((acc, d) => acc + d.totalSuccesses, 0);
		const errors = data.reduce((acc, d) => acc + d.totalErrors, 0);
		const totalRequests = success + errors;

		this.totalErrorRate = (totalRequests > 0) ? (errors / totalRequests) * 100 : 0.0;

		this.errorsChartData = {
			...this.errorsChartData,
			datasets: [
				{
					...this.errorsChartData.datasets[0],
					data: [success, errors]
				}
			]
		};
	}

	private updateEndpointsChart(data: APIAnalytics[]) {
		const agg = new Map<string, number>();
		data.forEach(d =>
			d.mostDemandedEndpoints.forEach(end =>
				agg.set(end.uri, (agg.get(end.uri) || 0) + end.count)
			)
		);

		const top = Array.from(agg.entries())
			.sort((e1, e2) => e2[1] - e1[1])
			.slice(0, 5);

		this.endpointsChartData = {
			...this.endpointsChartData,
			labels: top.map(t => t[0]),
			datasets: [
				{
					...this.endpointsChartData.datasets[0],
					data: top.map(t => t[1])
				}
			]
		};
	}

	private timeFormatter(dateString: string) {
		const formattedDate = new Date(dateString);

		if (isNaN(formattedDate.getTime())) {
			return '--:--';
		}

		return (this.selectedDateRange === '1h')
			? formattedDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
			: formattedDate.toLocaleDateString([], { day: '2-digit', month: 'short', hour: '2-digit' });
	}

	public downloadChartAsPNG() {
		const canvas = this.chart?.chart?.canvas;
		if (canvas) {
			const date = new Date().toISOString().split('T')[0];
			const fileName = `API_Performance_Analytics_${date}`;
			this.exportService.downloadPNG(canvas, fileName);
		}
	}

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}