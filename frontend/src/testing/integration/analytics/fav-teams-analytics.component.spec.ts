import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { FavTeamsAnalyticsComponent } from '../../../app/components/stats/fav-teams-analytics/fav-teams-analytics.component';
import { AnalyticsService } from '../../../app/services/analytics.service';
import { ExportService } from '../../../app/services/utilities/export.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

describe('FavTeams Analytics Component Integration Test', () => {
    let fixture: ComponentFixture<FavTeamsAnalyticsComponent>;
    let component: FavTeamsAnalyticsComponent;
    let httpMock: HttpTestingController;

    const apiUrl = 'https://localhost:8443/api/v1/analytics/fav-teams';

    const mockFavTeamsData: Record<string, number> = {
		'New York Yankees': 10,
		'Los Angeles Dodgers': 8,
		'Boston Red Sox': 5
	};

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [FavTeamsAnalyticsComponent],
            providers: [
                AnalyticsService,
                ExportService,
                provideHttpClient(),
                provideHttpClientTesting(),
            ]
        });

        fixture = TestBed.createComponent(FavTeamsAnalyticsComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch favorite teams analytics', () => {
        fixture.detectChanges();

        const req = httpMock.expectOne(apiUrl);
        expect(req.request.method).toBe('GET');

        req.flush(mockFavTeamsData);

        expect(component.loading).toBeFalse();
        expect(component.error).toBeFalse();

        expect(component.barChartData.labels).toContain('New York Yankees');
        expect(component.barChartData.labels?.length).toBe(3);

        const chartData = component.barChartData.datasets[0].data;
        expect(chartData).toEqual([10, 8, 5]);
    });
});