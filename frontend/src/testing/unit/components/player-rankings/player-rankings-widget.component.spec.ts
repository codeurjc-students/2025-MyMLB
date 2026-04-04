import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlayerRankingsWidgetComponent } from '../../../../app/components/player-rankings/player-rankings-widget/player-rankings-widget.component';
import { PlayerService } from '../../../../app/services/player.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { PlayerRanking } from '../../../../app/models/position-player.model';
import { PaginatedResponse } from '../../../../app/models/pagination.model';

describe('Player Rankings Widget Component Tests', () => {
    let component: PlayerRankingsWidgetComponent;
    let fixture: ComponentFixture<PlayerRankingsWidgetComponent>;
    let playerServiceSpy: jasmine.SpyObj<PlayerService>;
    let routerSpy: jasmine.SpyObj<Router>;

    const mockRankingResponse: PaginatedResponse<PlayerRanking> = {
        content: [
            { name: 'Aaron Judge', picture: '', stat: 0.311 },
            { name: 'Juan Soto', picture: '' , stat: 0.288 }
        ],
        page: { size: 5, totalElements: 2, totalPages: 1, number: 0 }
    };

    beforeEach(() => {
        playerServiceSpy = jasmine.createSpyObj('PlayerService', ['getPlayerSingleStatRankings', 'refreshPlayerRankings']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        playerServiceSpy.getPlayerSingleStatRankings.and.returnValue(of(mockRankingResponse));
        playerServiceSpy.refreshPlayerRankings.and.returnValue(of({ status: 'SUCCESS', message: 'Ok' } as any));

        TestBed.configureTestingModule({
            imports: [PlayerRankingsWidgetComponent],
            providers: [
                provideHttpClient(),
                { provide: PlayerService, useValue: playerServiceSpy },
                { provide: Router, useValue: routerSpy }
            ]
        });

        fixture = TestBed.createComponent(PlayerRankingsWidgetComponent);
        component = fixture.componentInstance;
    });

    it('should load data correctly ', () => {
        component.playerType = 'position';

        fixture.detectChanges();

        expect(component.isPitcher).toBeFalse();
        expect(component.selectedStat).toBe('AVG');
        expect(playerServiceSpy.getPlayerSingleStatRankings).toHaveBeenCalledWith(0, 5, 'position', 'average');
        expect(component.ranking.length).toBe(2);
    });

    it('should change stat and reload ranking', () => {
        component.playerType = 'position';
        fixture.detectChanges();
        playerServiceSpy.getPlayerSingleStatRankings.calls.reset();

        component.onStatFilterChange('HR');

        expect(component.selectedStat).toBe('HR');
        expect(playerServiceSpy.getPlayerSingleStatRankings).toHaveBeenCalledWith(0, 5, 'position', 'homeRuns');
    });

    it('should format stats correctly', () => {
        expect(component.statFormatter(0.3115, 'AVG')).toBe('.311');
        expect(component.statFormatter(3.456, 'ERA')).toBe('3.46');
        expect(component.statFormatter(40, 'HR')).toBe('40');
        expect(component.statFormatter(null as any, 'AVG')).toBe('-');
    });

    it('should navigate to rankings page', () => {
        component.playerType = 'pitcher';

        component.goToFullRankings();

        expect(routerSpy.navigate).toHaveBeenCalledWith(['player-rankings'], {
            queryParams: { playerType: 'pitcher' }
        });
    });

    it('should refresh rankings', () => {
        component.playerType = 'position';
        fixture.detectChanges();
        playerServiceSpy.getPlayerSingleStatRankings.calls.reset();

        component.refresh();

        expect(playerServiceSpy.refreshPlayerRankings).toHaveBeenCalled();
        expect(playerServiceSpy.getPlayerSingleStatRankings).toHaveBeenCalled();
    });
});