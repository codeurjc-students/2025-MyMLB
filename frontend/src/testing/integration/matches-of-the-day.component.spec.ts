import { ComponentFixture } from "@angular/core/testing";
import { MatchService, PaginatedMatches } from "../../app/services/match.service";
import { MatchesOfTheDayComponent } from "../../app/components/match/matches-of-the-day/matches-of-the-day.component";

describe('Matches of the Day Integration Tests', () => {
	let fixture: ComponentFixture<MatchesOfTheDayComponent>;
	let component: MatchesOfTheDayComponent;
	let matchServiceSpy: jasmine.SpyObj<MatchService>;

	const mockMatch: PaginatedMatches = {
			content: [
				{
					homeTeam: { name: 'Yankees', abbreviation: 'NYY', league: 'AL', division: 'East' },
					awayTeam: { name: 'Red Sox', abbreviation: 'BOS', league: 'AL', division: 'East' },
					homeScore: 5,
					awayScore: 3,
					date: '2025-10-22 19:05',
					status: 'Finished',
				},
			],
			page: {
				size: 10,
				number: 0,
				totalElements: 1,
				totalPages: 2,
			},
		};
});