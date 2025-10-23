import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StandingsWidgetComponent } from '../../app/components/standings/standings-widget/standings-widget.component';
import { TeamService } from '../../app/services/team.service';
import { Team } from '../../app/models/team-model';
import { of, throwError } from 'rxjs';

describe('Standings Component Integration Tests', () => {
	let fixture: ComponentFixture<StandingsWidgetComponent>;
	let component: StandingsWidgetComponent;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;

	const mockResponse = {
		American: {
			East: [
				{ name: 'Yankees', abbreviation: 'NYY', wins: 10, losses: 5 } as Team,
				{ name: 'Red Sox', abbreviation: 'BOS', wins: 8, losses: 7 } as Team,
			],
			West: [{ name: 'Astros', abbreviation: 'HOU', wins: 12, losses: 3 } as Team],
		},
		National: {
			Central: [{ name: 'Cubs', abbreviation: 'CHC', wins: 9, losses: 6 } as Team],
		},
	};

	beforeEach(() => {
		teamServiceSpy = jasmine.createSpyObj('TeamService', ['getStandings']);

		TestBed.configureTestingModule({
			imports: [StandingsWidgetComponent],
			providers: [{ provide: TeamService, useValue: teamServiceSpy }],
		});

		fixture = TestBed.createComponent(StandingsWidgetComponent);
		component = fixture.componentInstance;
	});

	it('should return standings correctly from service response', () => {
        teamServiceSpy.getStandings.and.returnValue(of(mockResponse));
        fixture.detectChanges();

        expect(component.standings.length).toBe(3);
        expect(component.standings[0].league).toBe('American');
        expect(component.standings[0].division).toBe('East');
        expect(component.standings[0].teams.length).toBe(2);
        expect(component.errorMessage).toBe('');
    });

    it('should set errorMessage when service fails', () => {
        teamServiceSpy.getStandings.and.returnValue(throwError(() => new Error('Service error')));
        fixture.detectChanges();

        expect(component.errorMessage).toBe('Error trying to load the standings');
        expect(component.standings.length).toBe(0);
    });

    it('should navigate correctly with next() and previous()', () => {
        teamServiceSpy.getStandings.and.returnValue(of(mockResponse));
        fixture.detectChanges();

        const total = component.standings.length;
        const initial = component.currentIndex;

        component.next();
        expect(component.currentIndex).toBe((initial + 1) % total);

        component.previous();
        expect(component.currentIndex).toBe((total + initial) % total);
    });
});