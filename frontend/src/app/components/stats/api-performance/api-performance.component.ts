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
import JSZip from 'jszip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';

@Component({
	selector: 'app-api-performance',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
        CommonModule,
		FormsModule,
        BaseChartDirective,
        MatIconModule,
		MatCheckboxModule,
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

	@ViewChild('perfChart') performanceChart?: BaseChartDirective;
	@ViewChild('errorChart') errorChart?: BaseChartDirective;
	@ViewChild('endpointChart') endpointChart?: BaseChartDirective;

	public selectedChartsToDownload = {
		perfChart: false,
		errorChart: false,
		endpointChart: false
	};

	public allChartsSelected = false;

	public apiAnalytics?: APIAnalytics;
	public selectedDateRange: TimeRange = '1h';
	public totalErrorRate = 0.0;
	public totalRequests = 0;
	public totalSuccesses = 0;
	public totalErrors = 0;
	public successRate = 0.0;

	public performanceChartData: ChartData<'line'> = {
		labels: [],
		datasets: [
			{
				data: [],
				label: 'Latency (ms)',
				borderColor: '#F59E0B',
				backgroundColor: 'rgba(245, 158, 11, 0.1)',
				fill: true,
				tension: 0.4,
				pointBackgroundColor: '#F59E0B'
			},
			{
				data: [],
				label: 'Requests',
				borderColor: '#6366F1',
				backgroundColor: 'rgba(99, 102, 241, 0.1)',
				fill: true,
				tension: 0.4,
				pointBackgroundColor: '#6366F1'
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
		labels: ['Success', 'Errors'],
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
					color: '#6B7280',
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
						family: 'system-ui'
					},
					padding: { bottom: 10 }
				}
			}
		},
		plugins: {
            legend: {
                display: true,
                position: 'bottom',
				align: 'center',
                labels: {
					usePointStyle: true,
					pointStyle: 'circle',
					padding: 20,
					color: '#9CA3AF'
				}
             },
             tooltip: {
                mode: 'index',
                intersect: false,
                backgroundColor: 'rgba(17, 24, 39, 0.9)',
                titleFont: { size: 14, weight: 'bold' },
                bodySpacing: 5,
                padding: 12,
                cornerRadius: 10,
                displayColors: true
            }
        },
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
					color: '#6B7280',
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

		this.performanceChart?.update();
		this.errorChart?.update();
		this.endpointChart?.update();
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
		this.totalSuccesses = data.reduce((acc, d) => acc + d.totalSuccesses, 0);
		this.totalErrors = data.reduce((acc, d) => acc + d.totalErrors, 0);
		this.totalRequests = this.totalSuccesses + this.totalErrors;

		this.totalErrorRate = (this.totalRequests > 0) ? (this.totalErrors / this.totalRequests) * 100 : 0.0;
		this.successRate = (this.totalRequests > 0) ? (this.totalSuccesses / this.totalRequests) * 100 : 0.0;

		this.errorsChartData = {
			...this.errorsChartData,
			datasets: [
				{
					...this.errorsChartData.datasets[0],
					data: [this.totalSuccesses, this.totalErrors]
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

	public toggleSelectedCharts() {
		this.allChartsSelected = !this.allChartsSelected;
		Object.keys(this.selectedChartsToDownload).forEach(key => {
			this.selectedChartsToDownload[key as keyof typeof this.selectedChartsToDownload] = this.allChartsSelected;
		});
	}

	public updateAllSelectedState() {
		const values = Object.values(this.selectedChartsToDownload);
		this.allChartsSelected = values.every(v => v);
	}

	async downloadChartAsPNG() {
		const { isOnlyOne, chartId, count } = this.getSelectedChartsInfo();

		if (count === 0) {
			return;
		}

		const chartsToExport = [
			{ id: 'perfChart', directive: this.performanceChart, name: 'API_Performance' },
			{ id: 'errorChart', directive: this.errorChart, name: 'API_Health_Rate' },
			{ id: 'endpointChart', directive: this.endpointChart, name: 'API_Endpoints' }
		];

		const downloadDate = new Date().toISOString().split('T')[0];

		if (isOnlyOne) {
			const chartToDownload = chartsToExport.find(c => c.id === chartId);
			if (chartToDownload?.directive?.chart) {
				const canvas = chartToDownload.directive.chart.canvas as HTMLCanvasElement;
				this.exportService.downloadPNG(canvas, `${chartToDownload.name}_${downloadDate}`);
				return;
			}
		}

		const zip = new JSZip();
		for (const chart of chartsToExport) {
			if (this.selectedChartsToDownload[chart.id as keyof typeof this.selectedChartsToDownload] && chart.directive?.chart) {
				const base64Data = chart.directive.chart.toBase64Image().split(',')[1];
				zip.file(`${chart.name}.png`, base64Data, { base64: true });
			}
		}

		const zipContent = await zip.generateAsync({ type: 'blob' });
		const link = document.createElement('a');
		link.href = URL.createObjectURL(zipContent);
		link.download = `API_Analytics_${downloadDate}.zip`;
		link.click();
	}

	private getSelectedChartsInfo() {
		const selectedEntries = Object.entries(this.selectedChartsToDownload).filter(([_, isSelected]) => isSelected);

		if (selectedEntries.length === 1) {
			return { isOnlyOne: true, chartId: selectedEntries[0][0], count: 1 };
		}
		return { isOnlyOne: false, chartId: null, count: selectedEntries.length };
	}

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}