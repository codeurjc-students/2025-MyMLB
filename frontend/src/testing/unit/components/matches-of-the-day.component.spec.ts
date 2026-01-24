import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatchesOfTheDayComponent } from '../../../app/components/match/matches-of-the-day/matches-of-the-day.component';
import { MatchService, PaginatedMatches } from '../../../app/services/match.service';
import { of, throwError } from 'rxjs';

describe('Matches Of The Day Component Tests', () => {
	let component: MatchesOfTheDayComponent;
	let fixture: ComponentFixture<MatchesOfTheDayComponent>;
	let matchServiceSpy: jasmine.SpyObj<MatchService>;

	const mockMatch: PaginatedMatches = {
		content: [
			{
				homeTeam: { name: 'Yankees', abbreviation: 'NYY', league: 'AL', division: 'East' },
				awayTeam: { name: 'Red Sox', abbreviation: 'BOS', league: 'AL', division: 'East' },
				homeScore: 5,
				awayScore: 3,
				date: '2025-10-22 19:05',
				status: 'FINISHED',
				stadiumName: 'Yankee Stadium'
			},
		],
		page: {
			size: 10,
			number: 0,
			totalElements: 1,
			totalPages: 2,
		},
	};

	beforeEach(() => {
		matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchesOfTheDay']);

		TestBed.configureTestingModule({
			providers: [{ provide: MatchService, useValue: matchServiceSpy }]
		});

		fixture = TestBed.createComponent(MatchesOfTheDayComponent);
		component = fixture.componentInstance;
	});

	it('should load matches on init', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(of(mockMatch));

		component.ngOnInit();

		expect(matchServiceSpy.getMatchesOfTheDay).toHaveBeenCalledWith(0, 10);
		expect(component.matches.length).toBe(1);
		expect(component.currentPage).toBe(0);
		expect(component.hasMore).toBeTrue();
		expect(component.errorMessage).toBe('');
	});

	it('should load next page if hasMore is true', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(of(mockMatch));
		component.currentPage = 0;
		component.hasMore = true;

		component.loadNextPage();

		expect(matchServiceSpy.getMatchesOfTheDay).toHaveBeenCalledWith(1, 10);
	});

	it('should not load next page if hasMore is false', () => {
		component.hasMore = false;
		component.loadNextPage();

		expect(matchServiceSpy.getMatchesOfTheDay).not.toHaveBeenCalled();
	});

	it('should set errorMessage on service error', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(
			throwError(() => new Error('Service error'))
		);
		component.loadMoreGames(0);
		expect(component.errorMessage).toBe('Error trying to show the matches');
	});
});