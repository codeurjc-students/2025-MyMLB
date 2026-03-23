import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FavTeamsAnalyticsComponent } from '../../../../app/components/stats/fav-teams-analytics/fav-teams-analytics.component';
import { AnalyticsService } from '../../../../app/services/analytics.service';
import { ExportService } from '../../../../app/services/utilities/export.service';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

describe('FavTeams Analytics Component Tests', () => {
    let component: FavTeamsAnalyticsComponent;
    let fixture: ComponentFixture<FavTeamsAnalyticsComponent>;
    let analyticsServiceSpy: jasmine.SpyObj<AnalyticsService>;
    let exportServiceSpy: jasmine.SpyObj<ExportService>;

    const mockFavTeamsData: Record<string, number> = {
		'New York Yankees': 10,
		'Los Angeles Dodgers': 8,
		'Boston Red Sox': 5
	};

    beforeEach(() => {
        analyticsServiceSpy = jasmine.createSpyObj('AnalyticsService', ['getFavTeamsAnalytics']);
        exportServiceSpy = jasmine.createSpyObj('ExportService', ['downloadPNG']);
        analyticsServiceSpy.getFavTeamsAnalytics.and.returnValue(of(mockFavTeamsData));

        TestBed.configureTestingModule({
            imports: [FavTeamsAnalyticsComponent],
            providers: [
                provideHttpClient(),
                { provide: AnalyticsService, useValue: analyticsServiceSpy },
                { provide: ExportService, useValue: exportServiceSpy }
            ]
        });

        fixture = TestBed.createComponent(FavTeamsAnalyticsComponent);
        component = fixture.componentInstance;
    });

    it('should fetch favorite teams analytics', () => {
        fixture.detectChanges();

        expect(analyticsServiceSpy.getFavTeamsAnalytics).toHaveBeenCalled();
        expect(component.loading).toBeFalse();
        expect(component.barChartData.labels).toEqual(['New York Yankees', 'Los Angeles Dodgers', 'Boston Red Sox']);
        expect(component.barChartData.datasets[0].data).toEqual([10, 8, 5]);
    });

    it('should handle error when service fails', () => {
        const errorMsg = 'Error de conexión';
        analyticsServiceSpy.getFavTeamsAnalytics.and.returnValue(throwError(() => new Error(errorMsg)));

        fixture.detectChanges();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain(`Error fetching the data: ${errorMsg}`);
        expect(component.loading).toBeFalse();
    });

    it('should emit backToStatsDashboard when goBackToDashboard is called', () => {
        spyOn(component.backToStatsDashboard, 'emit');

        component.goBackToDashboard();

        expect(component.backToStatsDashboard.emit).toHaveBeenCalled();
    });

    it('should call export service when downloading chart as PNG', () => {
        fixture.detectChanges();

        const mockCanvas = document.createElement('canvas');
        component.chart = {
            chart: {
                canvas: mockCanvas
            },
            update: jasmine.createSpy('update')
        } as any;

        component.downloadChartAsPNG();

        expect(exportServiceSpy.downloadPNG).toHaveBeenCalledWith(
            mockCanvas,
            jasmine.stringMatching(/Favorite_Teams_Analytics_/)
        );
    });
});