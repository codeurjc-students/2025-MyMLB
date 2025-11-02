import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StandingsWidgetComponent } from '../../../app/components/standings/standings-widget/standings-widget.component';
import { TeamService, StandingsResponse } from '../../../app/services/team.service';
import { BackgroundColorService } from '../../../app/services/background-color.service';
import { of } from 'rxjs';

describe('Standings Component Tests', () => {
	let component: StandingsWidgetComponent;
	let fixture: ComponentFixture<StandingsWidgetComponent>;
	let teamServiceSpy: jasmine.SpyObj<TeamService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;

	const mockStandings: StandingsResponse = {
		'American League': {
			East: [
				{
					name: 'Yankees',
					abbreviation: 'NYY',
					league: 'American League',
					division: 'East',
					totalGames: 162,
					wins: 90,
					losses: 72,
					pct: 0.556,
					gamesBehind: 0,
					lastTen: '6-4'
				},
			],
		},
		'National League': {
			West: [
				{
					name: 'Dodgers',
					abbreviation: 'LAD',
					league: 'National League',
					division: 'West',
					totalGames: 162,
					wins: 100,
					losses: 62,
					pct: 0.617,
					gamesBehind: 0,
					lastTen: '8-2'
				},
			],
		},
	};

	beforeEach(() => {
		const teamServiceMock = jasmine.createSpyObj('TeamService', ['getStandings']);
		const backgroundServiceMock = jasmine.createSpyObj('BackgroundColorService', [
			'getBackgroundColor',
		]);

		TestBed.configureTestingModule({
			imports: [StandingsWidgetComponent],
			providers: [
				{ provide: TeamService, useValue: teamServiceMock },
				{ provide: BackgroundColorService, useValue: backgroundServiceMock },
			],
		});

		fixture = TestBed.createComponent(StandingsWidgetComponent);
		component = fixture.componentInstance;
		teamServiceSpy = TestBed.inject(TeamService) as jasmine.SpyObj<TeamService>;
		backgroundServiceSpy = TestBed.inject(
			BackgroundColorService
		) as jasmine.SpyObj<BackgroundColorService>;
	});

	it('should load standings on init', () => {
		teamServiceSpy.getStandings.and.returnValue(of(mockStandings));
		fixture.detectChanges();

		expect(component.standings.length).toBe(2);
		expect(component.standings[0].league).toBe('American League');
		expect(component.standings[1].division).toBe('West');
		expect(component.errorMessage).toBe('');
	});

	it('should navigate to next and previous standings', () => {
		teamServiceSpy.getStandings.and.returnValue(of(mockStandings));
		fixture.detectChanges();

		expect(component.currentIndex).toBe(0);
		component.next();
		expect(component.currentIndex).toBe(1);
		component.previous();
		expect(component.currentIndex).toBe(0);
	});
});