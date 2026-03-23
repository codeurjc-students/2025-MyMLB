import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApiPerformanceComponent } from '../../../../app/components/stats/api-performance/api-performance.component';
import { AnalyticsService } from '../../../../app/services/analytics.service';
import { ExportService } from '../../../../app/services/utilities/export.service';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { APIAnalytics } from '../../../../app/models/analytics.model';

Chart.register(...registerables);

describe('API Performance Component Tests', () => {
	let component: ApiPerformanceComponent;
	let fixture: ComponentFixture<ApiPerformanceComponent>;
	let analyticsServiceSpy: jasmine.SpyObj<AnalyticsService>;
	let exportServiceSpy: jasmine.SpyObj<ExportService>;

	const mockResponse: APIAnalytics[] = [
		{
			timeStamp: '2026-03-22',
			totalRequests: 100,
			totalErrors: 10,
			totalSuccesses: 90,
			averageResponseTime: 25.4,
			mostDemandedEndpoints: [
				{
					uri: '/test',
					count: 10,
				},
				{
					uri: '/profile',
					count: 5
				}
			],
		},
		{
			timeStamp: '2026-03-10',
			totalRequests: 50,
			totalErrors: 20,
			totalSuccesses: 30,
			averageResponseTime: 18.4,
			mostDemandedEndpoints: [
				{
					uri: '/me',
					count: 20,
				},
				{
					uri: '/purchase',
					count: 3
				}
			],
		},
	];

	beforeEach(() => {
		analyticsServiceSpy = jasmine.createSpyObj('AnalyticsService', ['getAPIPerformanceHistory',]);
		exportServiceSpy = jasmine.createSpyObj('ExportService', ['downloadPNG', 'downloadZip']);

		analyticsServiceSpy.getAPIPerformanceHistory.and.returnValue(of(mockResponse));

		TestBed.configureTestingModule({
			imports: [ApiPerformanceComponent],
			providers: [
				provideHttpClient(),
				{ provide: AnalyticsService, useValue: analyticsServiceSpy },
				{ provide: ExportService, useValue: exportServiceSpy },
			],
		});

		fixture = TestBed.createComponent(ApiPerformanceComponent);
		component = fixture.componentInstance;
	});

	it('should fetch data', () => {
		fixture.detectChanges();

		expect(analyticsServiceSpy.getAPIPerformanceHistory).toHaveBeenCalledWith('1h');
		expect(component.totalRequests).toBe(150);
		expect(component.totalErrors).toBe(30);
	});

	it('should update charts when the data is retrieved correctly from the API', () => {
		fixture.detectChanges();

		expect(component.performanceChartData.labels?.length).toBe(2);
		expect(component.endpointsChartData.labels?.[0]).toBe('/me');
		expect(component.endpointsChartData.datasets[0].data[0]).toBe(20);
	});

	it('should change date range and refresh data', () => {
		fixture.detectChanges();
		analyticsServiceSpy.getAPIPerformanceHistory.calls.reset();

		component.onDateRangeChange('1d');

		expect(component.selectedDateRange).toBe('1d');
		expect(analyticsServiceSpy.getAPIPerformanceHistory).toHaveBeenCalledWith('1d');
	});

	it('should toggle all checkboxes when Select All is clicked', () => {
		component.allChartsSelected = false;

		component.toggleSelectedCharts();

		expect(component.allChartsSelected).toBeTrue();
		expect(component.selectedChartsToDownload.perfChart).toBeTrue();
		expect(component.selectedChartsToDownload.errorChart).toBeTrue();
		expect(component.selectedChartsToDownload.endpointChart).toBeTrue();
	});

	it('should update the selected charts to download correctly', () => {
		component.selectedChartsToDownload.perfChart = true;
		component.selectedChartsToDownload.errorChart = true;
		component.selectedChartsToDownload.endpointChart = false;

		component.updateAllSelectedState();
		expect(component.allChartsSelected).toBeFalse();

		component.selectedChartsToDownload.endpointChart = true;
		component.updateAllSelectedState();
		expect(component.allChartsSelected).toBeTrue();
	});

	it('should download png file when only one chart is selected to download', async () => {
		fixture.detectChanges();

		const mockCanvas = document.createElement('canvas');
		component.performanceChart = {
			chart: { canvas: mockCanvas },
		} as any;

		component.selectedChartsToDownload.perfChart = true;
		component.selectedChartsToDownload.errorChart = false;
		component.selectedChartsToDownload.endpointChart = false;

		await component.downloadChartAsPNG();

		expect(exportServiceSpy.downloadPNG).toHaveBeenCalled();
		expect(exportServiceSpy.downloadZip).not.toHaveBeenCalled();
	});

	it('should download zip file when multiple charts are selected to download', async () => {
		fixture.detectChanges();

		const mockChart = { toBase64Image: () => 'data:image/png;base64,123' };
		component.performanceChart = { chart: mockChart } as any;
		component.errorChart = { chart: mockChart } as any;

		component.selectedChartsToDownload.perfChart = true;
		component.selectedChartsToDownload.errorChart = true;

		await component.downloadChartAsPNG();

		expect(exportServiceSpy.downloadZip).toHaveBeenCalled();
	});
});