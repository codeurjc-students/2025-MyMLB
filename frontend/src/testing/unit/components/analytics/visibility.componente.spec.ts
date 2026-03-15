import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { VisibilityComponent } from '../../../../app/components/stats/visibility/visibility.component';
import { StatsService } from '../../../../app/services/stats.service';
import { ExportService } from '../../../../app/services/utilities/export.service';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

describe('Visibility Component Tests', () => {
    let component: VisibilityComponent;
    let fixture: ComponentFixture<VisibilityComponent>;
    let statsServiceSpy: jasmine.SpyObj<StatsService>;
    let exportServiceSpy: jasmine.SpyObj<ExportService>;

    const mockStats = [
        { date: '2026-03-01', visualizations: 100, newUsers: 10, deletedUsers: 2 },
        { date: '2026-03-02', visualizations: 200, newUsers: 20, deletedUsers: 5 }
    ];

    beforeEach(() => {
        statsServiceSpy = jasmine.createSpyObj('StatsService', ['getVisibilityStats']);
        exportServiceSpy = jasmine.createSpyObj('ExportService', ['downloadPNG']);

		statsServiceSpy.getVisibilityStats.and.returnValue(of(mockStats));

        TestBed.configureTestingModule({
            imports: [VisibilityComponent],
            providers: [
                provideHttpClient(),
                { provide: StatsService, useValue: statsServiceSpy },
                { provide: ExportService, useValue: exportServiceSpy }
            ]
        });

        fixture = TestBed.createComponent(VisibilityComponent);
        component = fixture.componentInstance;
    });

    it('should initialize with default date range and fetch data', () => {
        fixture.detectChanges();

        expect(component.loading).toBeFalse();
        expect(statsServiceSpy.getVisibilityStats).toHaveBeenCalled();
        expect(component.chartData.labels?.length).toBe(2);
        expect(component.totalVisualizations).toBe(300);
        expect(component.totalRegistrations).toBe(30);
    });

    it('should calculate growth and churn rate correctly', () => {
        fixture.detectChanges();
        expect(component.growthPercentage).toBe(10);
        expect(component.deletedUsersPercentage).toBeCloseTo(23.33, 1);
    });

    it('should handle error when service fails', () => {
        statsServiceSpy.getVisibilityStats.and.returnValue(throwError(() => new Error('API Error')));

        fixture.detectChanges();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain('Error loading the chart');
        expect(component.loading).toBeFalse();
    });

    it('should update data after a short delay when date filter changes', fakeAsync(() => {
        fixture.detectChanges();
        statsServiceSpy.getVisibilityStats.calls.reset();

        component.onDateFilterChange();

        expect(component.loading).toBeTrue();
        tick(400);

        expect(statsServiceSpy.getVisibilityStats).toHaveBeenCalled();
        expect(component.loading).toBeFalse();
    }));

    it('should call export service when downloading chart as PNG', () => {
        fixture.detectChanges();
        component.chart = {
            chart: {
                canvas: {} as HTMLCanvasElement,
                update: jasmine.createSpy('update')
            }
        } as any;

        component.downloadChartAsPNG();

        expect(exportServiceSpy.downloadPNG).toHaveBeenCalled();
    });

    it('should reset date filters and update data', () => {
        fixture.detectChanges();
        const mockFp = {
            setDate: jasmine.createSpy('setDate'),
            set: jasmine.createSpy('set')
        };
        component.dateFrom.nativeElement._flatpickr = mockFp;
        component.dateTo.nativeElement._flatpickr = mockFp;

        component.resetDateFilter();

        expect(mockFp.setDate).toHaveBeenCalled();
        expect(mockFp.set).toHaveBeenCalledWith('maxDate', jasmine.any(Date));
    });
});