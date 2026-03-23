import {
	ChangeDetectionStrategy,
	Component,
	ElementRef,
	inject,
	OnInit,
	ViewChild,
	AfterViewInit,
	Output,
	EventEmitter,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatIconModule } from '@angular/material/icon';
import flatpickr from 'flatpickr';
import { AnalyticsService } from '../../../services/analytics.service';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { LoadingModalComponent } from '../../modal/loading-modal/loading-modal.component';
import { ExportService } from '../../../services/utilities/export.service';
import { DonwloadButtonComponent } from '../donwload-button/donwload-button.component';
import { BackToDashboardButtonComponent } from '../back-to-dashboard-button/back-to-dashboard-button.component';
import { AnalyticsCards } from '../../../models/analytics.model';
import { AnalyticsCardsComponent } from "../analytics-cards/analytics-cards.component";

@Component({
	selector: 'app-visibility',
	standalone: true,
	imports: [
    CommonModule,
    BaseChartDirective,
    MatIconModule,
    ErrorModalComponent,
    LoadingModalComponent,
    DonwloadButtonComponent,
    BackToDashboardButtonComponent,
    AnalyticsCardsComponent
],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './visibility.component.html',
})
export class VisibilityComponent implements OnInit, AfterViewInit {
	private analyticsService = inject(AnalyticsService);
	private exportService = inject(ExportService);

	@Output() backToStatsDashboard = new EventEmitter<void>();
	@ViewChild(BaseChartDirective) chart?: BaseChartDirective;

	public chartData: ChartData<'line'> = {
		datasets: [
			{
				data: [],
				label: 'Visualizations',
				borderColor: '#42A5F5',
				backgroundColor: 'rgba(66, 165, 245, 0.2)',
				fill: 'origin',
				tension: 0.4,
				borderWidth: 2,
				pointRadius: 2,
				pointBackgroundColor: '#42A5F5',
			},
			{
				data: [],
				label: 'New Users',
				borderColor: '#10B981',
				backgroundColor: 'rgba(16, 185, 129, 0.1)',
				fill: 'origin',
				tension: 0.4,
				borderWidth: 2,
				pointRadius: 2,
				pointBackgroundColor: '#10B981',
			},
			{
				data: [],
				label: 'Deleted Users',
				borderColor: '#E53935',
				backgroundColor: 'rgba(229, 57, 53, 0.1)',
				fill: 'origin',
				tension: 0.4,
				borderWidth: 2,
				pointRadius: 2,
				pointBackgroundColor: '#E53935',
			},
		],
		labels: [],
	};

	public chartOptions: ChartConfiguration['options'] = {
		responsive: true,
		maintainAspectRatio: false,
		scales: {
			x: {
				grid: { display: false },
				ticks: {
					autoSkip: true,
					maxTicksLimit: 7,
					font: { size: 11 },
				},
				title: {
					display: true,
					text: 'Date',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'system-ui',
					},
					padding: { top: 10 },
				},
			},
			y: {
				beginAtZero: true,
				border: { display: false },
				grid: { color: 'rgba(255,255,255,0.05)' },
				title: {
					display: true,
					text: 'Users',
					color: '#6B7280',
					font: {
						size: 14,
						weight: 'bold',
						family: 'sytem-ui',
					},
					padding: { bottom: 10 },
				},
			},
		},
		plugins: {
			legend: {
				display: true,
				position: 'bottom',
				labels: { usePointStyle: true, padding: 20, color: '#9CA3AF' },
			},
			tooltip: {
				mode: 'index',
				intersect: false,
				backgroundColor: 'rgba(17, 24, 39, 0.9)',
				titleFont: { size: 14, weight: 'bold' },
				bodySpacing: 5,
				padding: 12,
				cornerRadius: 10,
				displayColors: true,
			},
		},
	};

	public chartType: ChartType = 'line';

	public totalVisualizations = 0;
	public totalRegistrations = 0;
	public totalLosses = 0;
	public growthPercentage = 0.0;
	public deletedUsersPercentage = 0.0;

	@ViewChild('dateFrom') dateFrom!: ElementRef;
	@ViewChild('dateTo') dateTo!: ElementRef;

	private selectedDateFrom = '';
	private selectedDateTo = '';

	public error = false;
	public errorMessage = '';
	public loading = false;

	ngOnInit() {
		const from = new Date();
		from.setMonth(from.getMonth() - 1);
		const to = new Date();

		this.selectedDateFrom = from.toISOString().split('T')[0];
		this.selectedDateTo = to.toISOString().split('T')[0];

		this.fetchData();
	}

	get analyticsCardsContent(): AnalyticsCards[] {
		return [
			{
				iconName: 'visibility',
				iconStyles: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400',
				label: 'Total Views',
				textStyles: 'from-blue-600 to-indigo-500',
				value: this.totalVisualizations,
				isRate: false,
			},
			{
				iconName: 'person_add',
				iconStyles: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400',
				label: 'New Users',
				textStyles: 'from-emerald-600 to-teal-500',
				value: this.totalRegistrations,
				isRate: false,
			},
			{
				iconName: 'person_remove',
				iconStyles: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
				label: 'Churn Users',
				textStyles: 'from-red-600 to-rose-500',
				value: this.totalLosses,
				isRate: false,
			},
			{
				iconName: 'trending_up',
				iconStyles: 'bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400',
				label: 'Success Rate',
				textStyles: 'from-indigo-600 to-purple-500',
				value: this.growthPercentage,
				isRate: true,
			},
			{
				iconName: 'trending_down',
				iconStyles: 'bg-rose-100 dark:bg-rose-900/30 text-rose-600 dark:text-rose-400',
				label: 'Churn Rate',
				textStyles: 'from-rose-600 to-orange-500',
				value: this.deletedUsersPercentage,
				isRate: true,
			},
		];
	}

	ngAfterViewInit() {
		const configBase = {
			disableMobile: false,
			dateFormat: 'Y-m-d',
			altInput: true,
			altFormat: 'M j, Y',
		};

		const fpFrom = flatpickr(this.dateFrom.nativeElement, {
			...configBase,
			defaultDate: this.selectedDateFrom,
			maxDate: this.selectedDateTo,
			onChange: (selectedDates) => {
				if (selectedDates?.length) {
					this.selectedDateFrom = selectedDates[0].toISOString().split('T')[0];
					fpTo.set('minDate', selectedDates[0]);
					this.onDateFilterChange();
				}
			},
		});

		const fpTo = flatpickr(this.dateTo.nativeElement, {
			...configBase,
			defaultDate: this.selectedDateTo,
			minDate: this.selectedDateFrom,
			onChange: (selectedDates) => {
				if (selectedDates?.length) {
					this.selectedDateTo = selectedDates[0].toISOString().split('T')[0];
					fpFrom.set('maxDate', selectedDates[0]);
					this.onDateFilterChange();
				}
			},
		});
	}

	public onDateFilterChange() {
		this.loading = true;
		setTimeout(() => {
			this.fetchData();
			this.loading = false;
		}, 400);
	}

	public resetDateFilter() {
		const from = new Date();
		from.setMonth(from.getMonth() - 1);
		const to = new Date();

		this.selectedDateFrom = from.toISOString().split('T')[0];
		this.selectedDateTo = to.toISOString().split('T')[0];

		const fpFrom = (this.dateFrom.nativeElement as any)._flatpickr;
		const fpTo = (this.dateTo.nativeElement as any)._flatpickr;

		if (fpFrom && fpTo) {
			fpFrom.setDate(from, false);
			fpTo.setDate(to, false);

			fpFrom.set('maxDate', to);
			fpTo.set('minDate', from);
		}
		this.onDateFilterChange();
	}

	public getDateRange(): string {
		if (!this.selectedDateFrom || !this.selectedDateTo) {
			return 'Select a range';
		}
		const options: Intl.DateTimeFormatOptions = {
			month: 'short',
			day: 'numeric',
			year: 'numeric',
		};
		const from = new Date(this.selectedDateFrom + 'T00:00:00');
		const to = new Date(this.selectedDateTo + 'T00:00:00');
		return `${from.toLocaleDateString('en-US', options)} - ${to.toLocaleDateString('en-US', options)}`;
	}

	private fetchData() {
		this.loading = true;
		this.error = false;
		this.analyticsService
			.getVisibilityStats(this.selectedDateFrom, this.selectedDateTo)
			.subscribe({
				next: (stats) => {
					this.chartData.labels = stats.map((s) => s.date);
					this.chartData.datasets[0].data = stats.map((s) => s.visualizations);
					this.chartData.datasets[1].data = stats.map((s) => s.newUsers);
					this.chartData.datasets[2].data = stats.map((s) => s.deletedUsers);

					this.totalVisualizations = stats.reduce(
						(acc, stat) => acc + stat.visualizations,
						0,
					);
					this.totalRegistrations = stats.reduce((acc, stat) => acc + stat.newUsers, 0);
					this.totalLosses = stats.reduce((acc, stat) => acc + stat.deletedUsers, 0);

					this.growthPercentage =
						this.totalVisualizations > 0
							? (this.totalRegistrations / this.totalVisualizations) * 100
							: 0;
					this.deletedUsersPercentage =
						this.totalRegistrations > 0
							? (this.totalLosses / this.totalRegistrations) * 100
							: 0;

					this.chart?.update();
					this.analyticsCardsContent;
					this.loading = false;
				},
				error: (err) => {
					this.error = true;
					this.errorMessage = `Error loading the chart: ${err.message}`;
					this.loading = false;
				},
			});
	}

	public downloadChartAsPNG() {
		const canvas = this.chart?.chart?.canvas;
		if (canvas) {
			const downloadDate = this.getDateRange().replace(/ /g, '_').replace(/, /g, '');
			const fileName = `Application_Visibility_Analytics_${downloadDate}`;
			this.exportService.downloadPNG(canvas, fileName);
		}
	}

	public goBackToDashboard() {
		this.backToStatsDashboard.emit();
	}
}
