import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CacheComponent } from '../../../app/components/stats/cache/cache.component';
import { CacheService } from '../../../app/services/cache.service';

describe('Cache Component Integration Test', () => {
    let fixture: ComponentFixture<CacheComponent>;
    let component: CacheComponent;
    let httpMock: HttpTestingController;

	const apiUrl = '/api/v1/cache';

    const mockCaches: string[] = ['get-users', 'get-teams', 'get-players'];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [CacheComponent],
            providers: [
                CacheService,
                provideHttpClient(),
                provideHttpClientTesting(),
            ]
        });

        fixture = TestBed.createComponent(CacheComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch and display caches on init', () => {
        fixture.detectChanges();

        const req = httpMock.expectOne(apiUrl);
        expect(req.request.method).toBe('GET');

        req.flush(mockCaches);

        expect(component.caches).toEqual(mockCaches);
        expect(component.caches.length).toBe(3);
    });

    it('should clear a specific cache successfulyy', () => {
        const cacheName = 'get-users';
        fixture.detectChanges();
        httpMock.expectOne(apiUrl).flush(mockCaches);

        component.clearCache(cacheName);

        const req = httpMock.expectOne(`${apiUrl}/${cacheName}`);
        expect(req.request.method).toBe('DELETE');
        req.flush({ status: 'SUCCESS', message: 'Success Message' });

        expect(component.success).toBeTrue();
    });

    it('should clear all caches successfully', () => {
        fixture.detectChanges();
        httpMock.expectOne(apiUrl).flush(mockCaches);

        component.clearAllCaches();

        const req = httpMock.expectOne(apiUrl);
        expect(req.request.method).toBe('DELETE');
        req.flush({ status: 'SUCCESS', message: 'All cleared' });

        expect(component.success).toBeTrue();
    });
});