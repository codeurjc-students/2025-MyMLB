import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApiPerformanceComponent } from '../../../app/components/stats/api-performance/api-performance.component';
import { ExportService } from '../../../app/services/utilities/export.service';
import { Chart, registerables } from 'chart.js';
import { AnalyticsService } from '../../../app/services/analytics.service';
import { APIAnalytics } from '../../../app/models/analytics.model';

Chart.register(...registerables);

describe('API Performance Component Integration Test', () => {
    let fixture: ComponentFixture<ApiPerformanceComponent>;
    let component: ApiPerformanceComponent;
    let httpMock: HttpTestingController;

    const apiUrl = 'https://localhost:8443/api/v1/analytics/api-performance';

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
        TestBed.configureTestingModule({
            imports: [ApiPerformanceComponent],
            providers: [
                AnalyticsService,
                ExportService,
                provideHttpClient(),
                provideHttpClientTesting(),
            ]
        });

        fixture = TestBed.createComponent(ApiPerformanceComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch data from API', () => {
        fixture.detectChanges();

        const req = httpMock.expectOne(`${apiUrl}?dateRange=1h`);
        expect(req.request.method).toBe('GET');

        req.flush(mockResponse);

        expect(component.totalRequests).toBe(150); // 100 + 50
        expect(component.totalErrors).toBe(30); // 10 + 20
        expect(component.totalSuccesses).toBe(120); // 90 + 30

        expect(component.totalErrorRate).toBe(20) // (30 / 150) * 100
        expect(component.successRate).toBeCloseTo(80); // (120 / 150) * 100
    });

    it('should retrieve new data from the API when date range changes', () => {
        fixture.detectChanges();

        const initialReq = httpMock.expectOne(`${apiUrl}?dateRange=1h`);
        initialReq.flush(mockResponse);

        component.onDateRangeChange('1d');

        const newReq = httpMock.expectOne(`${apiUrl}?dateRange=1d`);

        expect(newReq.request.method).toBe('GET');
        newReq.flush([]);
    });
});