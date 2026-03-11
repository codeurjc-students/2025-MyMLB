import { ChangeDetectionStrategy, Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatIconModule } from '@angular/material/icon';
import { StatsService } from '../../../services/stats.service';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';

@Component({
	selector: 'app-visibility',
	standalone: true,
	imports: [CommonModule, BaseChartDirective, MatIconModule, ErrorModalComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './visibility.component.html',
})
export class VisibilityComponent implements OnInit {
	private statsService = inject(StatsService);
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
				grid: { color: 'rgba(0,0,0,0.05)' },
			},
		},
		plugins: {
			legend: {
				display: true,
				position: 'bottom',
				labels: { usePointStyle: true, padding: 20 }
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

	public error = false;
	public errorMessage = '';

	ngOnInit() {
		this.fillWithMockData();
		//this.fetchData();
	}

	private fetchData() {
		const today = new Date().toISOString().split('T')[0];
		const previousMonth = new Date();
		previousMonth.setDate(previousMonth.getDate() - 30);
		const dateFrom = previousMonth.toISOString().split('T')[0];

		this.statsService.getVisibilityStats(dateFrom, today).subscribe({
			next: (stats) => {
				this.chartData.labels = stats.map((stat) => stat.date);
				this.chartData.datasets[0].data = stats.map((stat) => stat.visualizations);
				this.chartData.datasets[1].data = stats.map((stat) => stat.registrations);
				this.chartData.datasets[2].data = stats.map((stat) => stat.losses);

				this.totalVisualizations = stats.reduce((acc, stat) => acc + stat.visualizations,0);
				this.totalRegistrations = stats.reduce((acc, stat) => acc + stat.registrations, 0);
				this.totalLosses = stats.reduce((acc, stat) => acc + stat.losses, 0);

				if (this.totalVisualizations === 0) {
					this.growthPercentage = 0.0;
				}
				else {
					this.growthPercentage = (this.totalRegistrations / this.totalVisualizations) * 100;
				}

				if (this.totalRegistrations === 0) {
					this.usersChurnPercentage = 0.0;
				}
				else {
					this.usersChurnPercentage = (this.totalLosses / this.totalRegistrations) * 100;
				}

				this.chart?.update();
			},
			error: (error) => {
				this.error = true;
				this.errorMessage = `An error occur fetching the stats:${error.message}`;
			},
		});
	}

    private fillWithMockData() {
        const dataPoints = 30;
        const mockStats = [];

        for (let i = 0; i < dataPoints; i++) {
            const date = new Date();
            date.setDate(date.getDate() - (dataPoints - i));

            const visualizations = Math.floor(20 + i * 1.5 + Math.sin(i) * 10);
            const registrations = Math.floor(visualizations * 0.15 + Math.random() * 2);
            const losses = Math.floor(Math.random() * (registrations * 0.5));

            mockStats.push({
                date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
                visualizations: visualizations,
                registrations: registrations,
                losses: losses
            });
        }
        this.chartData.labels = mockStats.map((s) => s.date);
        this.chartData.datasets[0].data = mockStats.map((s) => s.visualizations);
        this.chartData.datasets[1].data = mockStats.map((s) => s.registrations);
        this.chartData.datasets[2].data = mockStats.map((s) => s.losses);

        this.totalVisualizations = mockStats.reduce((acc, s) => acc + s.visualizations, 0);
        this.totalRegistrations = mockStats.reduce((acc, s) => acc + s.registrations, 0);
        this.totalLosses = mockStats.reduce((acc, s) => acc + s.losses, 0);

        this.growthPercentage = this.totalVisualizations > 0
            ? (this.totalRegistrations / this.totalVisualizations) * 100
            : 0;

        this.usersChurnPercentage = this.totalRegistrations > 0
            ? (this.totalLosses / this.totalRegistrations) * 100
            : 0;

        if (this.chart) {
            this.chart.update();
        }
    }
}