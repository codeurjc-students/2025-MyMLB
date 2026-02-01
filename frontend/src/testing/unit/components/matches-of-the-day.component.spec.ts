import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatchesOfTheDayComponent } from '../../../app/components/match/matches-of-the-day/matches-of-the-day.component';
import { MatchService } from '../../../app/services/match.service';
import { of, throwError } from 'rxjs';
import { MockFactory } from '../../utils/mock-factory';

describe('Matches Of The Day Component Tests', () => {
	let component: MatchesOfTheDayComponent;
	let fixture: ComponentFixture<MatchesOfTheDayComponent>;
	let matchServiceSpy: jasmine.SpyObj<MatchService>;

	const mockHomeTeam = MockFactory.buildTeamSummaryMock('New York Yankees', 'NYY', 'AL', 'EAST');
	const mockAwayTeam = MockFactory.buildTeamSummaryMock('Boston Red Sox', 'BOS', 'AL', 'EAST');
	const mockMatch = MockFactory.buildShowMatchMock(1, mockAwayTeam, mockHomeTeam, 2, 3, '2026-05-15', 'IN PROGRESS');
	const mockResponse = MockFactory.buildPaginatedResponse(mockMatch);

	beforeEach(() => {
		matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchesOfTheDay']);

		TestBed.configureTestingModule({
			providers: [{ provide: MatchService, useValue: matchServiceSpy }]
		});

		fixture = TestBed.createComponent(MatchesOfTheDayComponent);
		component = fixture.componentInstance;
	});

	it('should load matches on init', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(of(mockResponse));

		component.ngOnInit();

		expect(matchServiceSpy.getMatchesOfTheDay).toHaveBeenCalledWith(0, 10);
		expect(component.matches.length).toBe(1);
		expect(component.currentPage).toBe(2);
		expect(component.hasMore).toBeFalse();
		expect(component.errorMessage).toBe('');
	});

	it('should load next page if hasMore is true', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(of(mockResponse));
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