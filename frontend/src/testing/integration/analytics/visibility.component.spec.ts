import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { VisibilityComponent } from '../../../app/components/stats/visibility/visibility.component';
import { ExportService } from '../../../app/services/utilities/export.service';
import { Chart, registerables } from 'chart.js';
import { AnalyticsService } from '../../../app/services/analytics.service';

Chart.register(...registerables);

describe('Visibility Component Integration Test', () => {
    let fixture: ComponentFixture<VisibilityComponent>;
    let component: VisibilityComponent;
    let httpMock: HttpTestingController;

    const apiUrl = 'https://localhost:8443/api/v1/analytics/visibility';

    const mockStatsResponse = [
        { date: '2026-03-01', visualizations: 100, newUsers: 20, deletedUsers: 5 },
        { date: '2026-03-02', visualizations: 200, newUsers: 30, deletedUsers: 10 }
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [VisibilityComponent],
            providers: [
                AnalyticsService,
                ExportService,
                provideHttpClient(),
                provideHttpClientTesting(),
            ]
        });

        fixture = TestBed.createComponent(VisibilityComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

	it('should fetch metrics from API correctly', () => {
		const dateFrom = new Date();
		dateFrom.setMonth(dateFrom.getMonth() - 1);
		const dateTo = new Date();

		const formattedDateFrom = dateFrom.toISOString().split('T')[0];
		const formattedDateTo = dateTo.toISOString().split('T')[0];
        fixture.detectChanges();

        const req = httpMock.expectOne(`${apiUrl}?dateFrom=${formattedDateFrom}&dateTo=${formattedDateTo}`);

        expect(req.request.method).toBe('GET');

        req.flush(mockStatsResponse);

        expect(component.totalVisualizations).toBe(300);
        expect(component.totalRegistrations).toBe(50);
        expect(component.growthPercentage).toBeCloseTo(16.67, 1);
    });
});