import { ChangeDetectionStrategy, Component, ElementRef, inject, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatIconModule } from '@angular/material/icon';
import flatpickr from 'flatpickr';
import { StatsService } from '../../../services/stats.service';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { ExportService } from '../../../services/utilities/export.service';

@Component({
    selector: 'app-visibility',
    standalone: true,
    imports: [CommonModule, BaseChartDirective, MatIconModule, ErrorModalComponent, LoadingModalComponent],
    changeDetection: ChangeDetectionStrategy.Default,
    templateUrl: './visibility.component.html',
})
export class VisibilityComponent implements OnInit, AfterViewInit {
    private statsService = inject(StatsService);
	private exportService = inject(ExportService);
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
                pointBackgroundColor: '#42A5F5'
            },
            {
                data: [],
                label: 'Registrations',
                borderColor: '#FFA726',
                backgroundColor: 'rgba(255, 167, 38, 0.2)',
                fill: 'origin',
                tension: 0.4,
                borderWidth: 2,
                pointRadius: 2,
                pointBackgroundColor: '#FFA726'
            },
            {
                data: [],
                label: 'Losses',
                borderColor: '#E53935',
                backgroundColor: 'rgba(229, 57, 53, 0.1)',
                fill: 'origin',
                tension: 0.4,
                borderWidth: 2,
                pointRadius: 2,
                pointBackgroundColor: '#E53935'
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
                    font: { size: 11 }
                }
            },
            y: {
                beginAtZero: true,
                border: { display: false },
                grid: { color: 'rgba(255,255,255,0.05)' },
            },
        },
        plugins: {
            legend: {
                display: true,
                position: 'bottom',
                labels: { usePointStyle: true, padding: 20, color: '#9CA3AF' }
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

    public chartType: ChartType = 'line';

    public totalVisualizations = 0;
    public totalRegistrations = 0;
    public totalLosses = 0;
    public growthPercentage = 0.0;
    public usersChurnPercentage = 0.0;

    @ViewChild('dateFrom') dateFrom!: ElementRef;
    @ViewChild('dateTo') dateTo!: ElementRef;

    private selectedDateFrom = '';
    private selectedDateTo = ''

    public error = false;
    public errorMessage = '';
    public loading = false;

    ngOnInit() {
        const from = new Date();
        from.setMonth(from.getMonth() - 1);
        const to = new Date();

        this.selectedDateFrom = from.toISOString().split('T')[0];
        this.selectedDateTo = to.toISOString().split('T')[0];

        this.fillWithMockData();
    }

    ngAfterViewInit() {
        const configBase = {
            disableMobile: false,
            dateFormat: "Y-m-d",
            altInput: true,
            altFormat: "M j, Y",
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
            // this.fetchData();
            this.fillWithMockData();
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
		const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
		const from = new Date(this.selectedDateFrom + 'T00:00:00');
		const to = new Date(this.selectedDateTo + 'T00:00:00');
		return `${from.toLocaleDateString('en-US', options)} - ${to.toLocaleDateString('en-US', options)}`;
	}

    private fillWithMockData() {
        const start = new Date(this.selectedDateFrom);
        const end = new Date(this.selectedDateTo);

        const diffTime = Math.abs(end.getTime() - start.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

        const mockStats = [];

        for (let i = 0; i < diffDays; i++) {
            const currentDate = new Date(start);
            currentDate.setDate(start.getDate() + i);

            const visualizations = Math.floor(Math.random() * 40) + 15;
            const registrations = Math.floor(visualizations * (0.1 + Math.random() * 0.1));
            const losses = Math.floor(Math.random() * (registrations * 0.3));

            mockStats.push({
                date: currentDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
                visualizations,
                registrations,
                losses
            });
        }

        this.chartData.labels = mockStats.map(s => s.date);
        this.chartData.datasets[0].data = mockStats.map(s => s.visualizations);
        this.chartData.datasets[1].data = mockStats.map(s => s.registrations);
        this.chartData.datasets[2].data = mockStats.map(s => s.losses);

        this.totalVisualizations = mockStats.reduce((acc, s) => acc + s.visualizations, 0);
        this.totalRegistrations = mockStats.reduce((acc, s) => acc + s.registrations, 0);
        this.totalLosses = mockStats.reduce((acc, s) => acc + s.losses, 0);

        this.growthPercentage = this.totalVisualizations > 0 ? (this.totalRegistrations / this.totalVisualizations) * 100 : 0;
        this.usersChurnPercentage = this.totalRegistrations > 0 ? (this.totalLosses / this.totalRegistrations) * 100 : 0;

        if (this.chart) {
            this.chart.chart?.update();
        }
    }

    private fetchData() {
        this.loading = true;
        this.error = false;
        this.statsService.getVisibilityStats(this.selectedDateFrom, this.selectedDateTo).subscribe({
            next: (stats) => {
                this.chartData.labels = stats.map(s => s.date);
                this.chartData.datasets[0].data = stats.map(s => s.visualizations);
                this.chartData.datasets[1].data = stats.map(s => s.newUsers);
                this.chartData.datasets[2].data = stats.map(s => s.churnUsers);

                this.totalVisualizations = stats.reduce((acc, stat) => acc + stat.visualizations, 0);
                this.totalRegistrations = stats.reduce((acc, stat) => acc + stat.newUsers, 0);
                this.totalLosses = stats.reduce((acc, stat) => acc + stat.churnUsers, 0);

                this.growthPercentage = this.totalVisualizations > 0 ? (this.totalRegistrations / this.totalVisualizations) * 100 : 0;
                this.usersChurnPercentage = this.totalRegistrations > 0 ? (this.totalLosses / this.totalRegistrations) * 100 : 0;

                this.chart?.chart?.update();
                this.loading = false;
            },
            error: (err) => {
                this.error = true;
                this.errorMessage = `Error loading the chart: ${err.message}`;
                this.loading = false;
            }
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
}