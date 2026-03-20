import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { StatsService } from "../../../app/services/stats.service";
import { VisibilityStats } from "../../../app/models/stats.model";
import { AuthResponse } from "../../../app/models/auth.model";

describe('Stats Service Tests', () => {
    let service: StatsService;
    let httpMock: HttpTestingController;
    const apiUrl = 'https://localhost:8443/api/v1/stats';

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                StatsService,
                provideHttpClient(withFetch()),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(StatsService);
        httpMock = TestBed.inject(HttpTestingController);

        sessionStorage.clear();
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch visibility stats within a date range', () => {
        const mockStats: VisibilityStats[] = [
            { date: '2026-03-01', visualizations: 10, newUsers: 2, deletedUsers: 0 }
        ];
        const dateFrom = '2026-03-01';
        const dateTo = '2026-03-15';

        service.getVisibilityStats(dateFrom, dateTo).subscribe((stats) => {
            expect(stats.length).toBe(1);
            expect(stats).toEqual(mockStats);
        });

        const req = httpMock.expectOne(`${apiUrl}/visibility?dateFrom=${dateFrom}&dateTo=${dateTo}`);
        expect(req.request.method).toBe('GET');
        req.flush(mockStats);
    });

    it('should update visualizations', () => {
        const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Successfully update visualizations'};

        service.updateVisualizations().subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${apiUrl}/visibility/visualizations`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual({});
        req.flush(mockResponse);
    });

    it('should update new users', () => {
		const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Successfully update new users'};

        service.updateNewUsers().subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${apiUrl}/visibility/registrations`);
        expect(req.request.method).toBe('POST');
        req.flush(mockResponse);
    });

    it('should update deleted users', () => {
        const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Successfully update delete users'};

        service.updateDeletedUsers().subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${apiUrl}/visibility/losses`);
        expect(req.request.method).toBe('POST');
        req.flush(mockResponse);
    });

    it('should track visitor if it has not visited the application yet', () => {
        const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Successfully update visualizations'};

        service.trackVisitor();

		const req = httpMock.expectOne(`${apiUrl}/visibility/visualizations`);
        expect(req.request.method).toBe('POST');
        req.flush(mockResponse);

        expect(sessionStorage.getItem('newVisitor')).toBe('true');
    });
});