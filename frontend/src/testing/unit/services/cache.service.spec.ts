import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withFetch } from "@angular/common/http";
import { CacheService } from "../../../app/services/cache.service";
import { AuthResponse } from "../../../app/models/auth.model";

describe('Cache Service Tests', () => {
    let service: CacheService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                CacheService,
                provideHttpClient(withFetch()),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(CacheService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch the list of active caches', () => {
        const mockCaches: string[] = ['get-users', 'get-teams', 'get-players'];

        service.getCaches().subscribe((caches) => {
            expect(caches.length).toBe(3);
            expect(caches).toEqual(mockCaches);
        });
        const req = httpMock.expectOne(`${service['apiUrl']}`);
        expect(req.request.method).toBe('GET');
        req.flush(mockCaches);
    });

    it('should clear a single cache', () => {
        const cacheName = 'get-users';
        const mockResponse: AuthResponse = {
            status: 'SUCCESS',
            message: `Cache ${cacheName} successfully restored`
        };

        service.clearCache(cacheName).subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${service['apiUrl']}/${cacheName}`);
        expect(req.request.method).toBe('DELETE');
        req.flush(mockResponse);
    });

    it('should clear all caches', () => {
        const mockResponse: AuthResponse = {
            status: 'SUCCESS',
            message: 'Caches successfully restored'
        };

        service.clearAllCaches().subscribe((response) => {
            expect(response).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${service['apiUrl']}`);
        expect(req.request.method).toBe('DELETE');
        req.flush(mockResponse);
    });
});