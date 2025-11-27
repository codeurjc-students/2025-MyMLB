import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { EditMenuComponent } from '../../../app/components/admin/edit-menu/edit-menu.component';
import { SearchService } from '../../../app/services/search.service';
import { BackgroundColorService } from '../../../app/services/background-color.service';
import { PaginatedSearchs } from '../../../app/models/pagination.model';
import { TeamInfo } from '../../../app/models/team.model';

describe('Edit Menu Component Integration Tests', () => {
	let fixture: ComponentFixture<EditMenuComponent>;
	let component: EditMenuComponent;
	let httpMock: HttpTestingController;

	const apiUrl = '/api/v1/searchs/team?query=Yankees&page=0&size=10';

	const mockTeam: TeamInfo = {
		teamStats: {
			name: 'New York Yankees',
			abbreviation: 'NYY',
			league: 'AL',
			division: 'East',
			totalGames: 162,
			wins: 100,
			losses: 62,
			pct: 0.617,
			gamesBehind: 0,
			lastTen: '7-3',
		},
		city: 'New York',
		generalInfo: 'Founded in 1901',
		championships: [1903, 1923],
		stadium: { name: 'Yankee Stadium', openingDate: 2009, pictures: [] },
		positionPlayers: [],
		pitchers: [],
	};

	const mockResponse: PaginatedSearchs = {
		content: [mockTeam],
		page: { size: 10, number: 0, totalElements: 1, totalPages: 1 },
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [EditMenuComponent],
			providers: [
				SearchService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				{
					provide: BackgroundColorService,
					useValue: { getBackgroundColor: () => 'bg-default' },
				},
			],
		});

		fixture = TestBed.createComponent(EditMenuComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should set error when searchQuery is empty', () => {
		component.searchQuery = '';
		component.performSearch();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toContain('Please enter');
	});

	it('should set error when playerType is missing for player search', () => {
		component.searchQuery = 'Judge';
		component.searchType = 'player';
		component.playerType = null;
		component.performSearch();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toContain('Please select the type of player');
	});

	it('should perform search and populate results', () => {
		component.searchQuery = 'Yankees';
		component.searchType = 'team';
		component.performSearch();

		const req = httpMock.expectOne(apiUrl);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);

		expect(component.searchResults.length).toBe(1);
		expect((component.searchResults[0] as TeamInfo).teamStats.abbreviation).toBe('NYY');
		expect(component.noResultsMessage).toBe('');
	});

	it('should handle no results', () => {
		component.searchQuery = 'Unknown';
		component.searchType = 'team';
		component.performSearch();

		const req = httpMock.expectOne('/api/v1/searchs/team?query=Unknown&page=0&size=10');
		req.flush({ content: [], page: { size: 10, number: 0, totalElements: 0, totalPages: 0 } });

		expect(component.noResultsMessage).toBe('No results were found');
		expect(component.searchResults.length).toBe(0);
	});

	it('should handle search error', () => {
		component.searchQuery = 'Yankees';
		component.searchType = 'team';
		component.performSearch();

		const req = httpMock.expectOne(apiUrl);
		req.flush({}, { status: 500, statusText: 'Server Error' });

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occur during the search');
	});
});