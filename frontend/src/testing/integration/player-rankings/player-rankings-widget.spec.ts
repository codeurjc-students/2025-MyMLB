import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { PlayerRankingsWidgetComponent } from '../../../app/components/player-rankings/player-rankings-widget/player-rankings-widget.component';
import { PlayerService } from '../../../app/services/player.service';
import { PaginatedResponse } from '../../../app/models/pagination.model';
import { PlayerRanking } from '../../../app/models/position-player.model';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '../../../app/services/auth.service';

describe('Player Rankings Widget Component Integration Test', () => {
    let fixture: ComponentFixture<PlayerRankingsWidgetComponent>;
    let component: PlayerRankingsWidgetComponent;
    let httpMock: HttpTestingController;
	let authServiceMock: any;
    let router: Router;

    const apiUrl = '/api/v1/players';

    const mockRankingsResponse: PaginatedResponse<PlayerRanking> = {
        content: [
            { name: 'Aaron Judge', picture: '', stat: 0.311 },
            { name: 'Juan Soto', picture: '', stat: 0.288 }
        ],
        page: { size: 5, totalElements: 2, totalPages: 1, number: 0 }
    };

    beforeEach(() => {
		authServiceMock = {
			currentUser$: new BehaviorSubject({ username: 'test-user', roles: ['USER'] }),
		};
        TestBed.configureTestingModule({
            imports: [PlayerRankingsWidgetComponent],
            providers: [
                PlayerService,
				{ provide: AuthService, useValue: authServiceMock },
                provideHttpClient(),
                provideHttpClientTesting(),
                {
                    provide: Router,
                    useValue: { navigate: jasmine.createSpy('navigate') }
                }
            ]
        });

        fixture = TestBed.createComponent(PlayerRankingsWidgetComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
        router = TestBed.inject(Router);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch position player rankings on init', () => {
        component.playerType = 'position';
        fixture.detectChanges();

        const req = httpMock.expectOne((request) =>
            request.url === `${apiUrl}/rankings` &&
            request.params.get('playerType') === 'position' &&
            request.params.get('stat') === 'average'
        );

        expect(req.request.method).toBe('GET');
        req.flush(mockRankingsResponse);

        expect(component.ranking.length).toBe(2);
        expect(component.ranking[0].name).toBe('Aaron Judge');
        expect(component.loading).toBeFalse();
    });

    it('should fetch pitcher rankings when playerType is pitcher', () => {
        component.playerType = 'pitcher';

        fixture.detectChanges();

        const req = httpMock.expectOne((request) =>
            request.url === `${apiUrl}/rankings` &&
            request.params.get('playerType') === 'pitcher' &&
            request.params.get('stat') === 'era'
        );

        expect(req.request.method).toBe('GET');
        req.flush(mockRankingsResponse);

        expect(component.isPitcher).toBeTrue();
        expect(component.selectedStat).toBe('ERA');
    });
});