import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MatchService, PaginatedMatches } from "../../app/services/match.service";
import { MatchesOfTheDayComponent } from "../../app/components/match/matches-of-the-day/matches-of-the-day.component";
import { of } from "rxjs";

describe('Matches of the Day Integration Tests', () => {
	let fixture: ComponentFixture<MatchesOfTheDayComponent>;
	let component: MatchesOfTheDayComponent;
	let matchServiceSpy: jasmine.SpyObj<MatchService>;

	const generateMockMatches = (count: number): PaginatedMatches => ({
        content: Array.from({ length: count }, (_, i) => ({
            homeTeam: { name: `Team ${i}`, abbreviation: `T${i}`, league: 'AL', division: 'East' },
            awayTeam: { name: `Opponent ${i}`, abbreviation: `O${i}`, league: 'AL', division: 'East' },
            homeScore: i,
            awayScore: i + 1,
            date: `2025-10-22 19:${String(i).padStart(2, '0')}`,
            status: 'Finished',
        })),
        page: {
            size: 10,
            number: count === 10 ? 0 : 1,
            totalElements: 15,
            totalPages: 2,
        },
    });

	beforeEach(() => {
		matchServiceSpy = jasmine.createSpyObj('MatchService', ['getMatchesOfTheDay']);

		TestBed.configureTestingModule({
			imports: [MatchesOfTheDayComponent],
			providers: [{ provide: MatchService, useValue: matchServiceSpy}]
		});

		fixture = TestBed.createComponent(MatchesOfTheDayComponent);
		component = fixture.componentInstance;
	});

	it('should load the first 10 matches scheduled for today', () => {
		matchServiceSpy.getMatchesOfTheDay.and.returnValue(of(generateMockMatches(10)));
		fixture.detectChanges();

		expect(component.pageSize).toBe(10);
		expect(component.currentPage).toBe(0);
		expect(component.hasMore).toBeTrue();
		expect(component.matches.length).toBe(10);
		expect(component.matches[0].homeTeam.abbreviation).toBe('T0');
		expect(component.matches[0].awayTeam.abbreviation).toBe('O0');
		expect(component.matches[9].homeTeam.abbreviation).toBe('T9');
		expect(component.matches[9].awayTeam.abbreviation).toBe('O9');
	});

	it('should load the remaining matches scheduled for today', () => {
		matchServiceSpy.getMatchesOfTheDay.and.callFake((page : number) => {
			return of(generateMockMatches(page === 0 ? 10: 5));
		});
		fixture.detectChanges();
		component.loadNextPage();

		expect(component.matches.length).toBe(15);
		expect(component.currentPage).toBe(1);
		expect(component.pageSize).toBe(10);
		expect(component.hasMore).toBeFalse();
	});
});