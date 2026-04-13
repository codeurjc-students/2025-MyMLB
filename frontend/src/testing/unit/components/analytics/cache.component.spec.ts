import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CacheComponent } from '../../../../app/components/stats/cache/cache.component';
import { CacheService } from '../../../../app/services/cache.service';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { AuthResponse } from '../../../../app/models/auth.model';

describe('Cache Component Tests', () => {
    let component: CacheComponent;
    let fixture: ComponentFixture<CacheComponent>;
    let cacheServiceSpy: jasmine.SpyObj<CacheService>;

    const mockCaches = ['get-users', 'get-teams', 'get-players'];

    beforeEach(() => {
        cacheServiceSpy = jasmine.createSpyObj('CacheService', ['getCaches', 'clearCache', 'clearAllCaches']);

        cacheServiceSpy.getCaches.and.returnValue(of(mockCaches));
        cacheServiceSpy.clearCache.and.returnValue(of({ status: 'SUCCESS', message: 'Cache cleared' } as AuthResponse));
        cacheServiceSpy.clearAllCaches.and.returnValue(of({ status: 'SUCCESS', message: 'All caches cleared' } as AuthResponse));

        TestBed.configureTestingModule({
            imports: [CacheComponent],
            providers: [
                provideHttpClient(),
                { provide: CacheService, useValue: cacheServiceSpy }
            ]
        });

        fixture = TestBed.createComponent(CacheComponent);
        component = fixture.componentInstance;
    });

    it('should load caches on initialization', () => {
        fixture.detectChanges();

        expect(cacheServiceSpy.getCaches).toHaveBeenCalled();
        expect(component.caches).toEqual(mockCaches);
        expect(component.loading).toBeFalse();
    });

    it('should handle error when loading caches fails', () => {
        const errorResponse = { error: { message: 'API error' } };
        cacheServiceSpy.getCaches.and.returnValue(throwError(() => errorResponse));

        fixture.detectChanges();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain('API error');
        expect(component.loading).toBeFalse();
    });

    it('should clear a specific cache and show success message', () => {
        const cacheToClear = 'get-users';
        fixture.detectChanges();

        component.clearCache(cacheToClear);

        expect(cacheServiceSpy.clearCache).toHaveBeenCalledWith(cacheToClear);
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe(`Cache ${cacheToClear} successfully restored`);
        expect(component.loading).toBeFalse();
    });

    it('should handle error when clearing a specific cache fails', () => {
        const errorResponse = { error: { message: 'Cache not found' } };
        cacheServiceSpy.clearCache.and.returnValue(throwError(() => errorResponse));

        component.clearCache('invalid-cache');

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain('Cache not found');
        expect(component.loading).toBeFalse();
    });

    it('should clear all caches and show success message', () => {
        component.clearAllCaches();

        expect(cacheServiceSpy.clearAllCaches).toHaveBeenCalled();
        expect(component.success).toBeTrue();
        expect(component.successMessage).toBe('Caches successfully restored');
        expect(component.loading).toBeFalse();
    });

    it('should handle error when clearing all caches fails', () => {
        const errorResponse = { error: { message: 'Unexpected server error' } };
        cacheServiceSpy.clearAllCaches.and.returnValue(throwError(() => errorResponse));

        component.clearAllCaches();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toContain('Unexpected server error');
        expect(component.loading).toBeFalse();
    });

    it('should emit backToStatsDashboard when goBackToDashboard is called', () => {
        spyOn(component.backToStatsDashboard, 'emit');

        component.goBackToDashboard();

        expect(component.backToStatsDashboard.emit).toHaveBeenCalled();
    });
});