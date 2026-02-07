import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EditMenuComponent } from '../../../../app/components/admin/edit-menu/edit-menu.component';
import { SearchService } from '../../../../app/services/search.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { MockFactory } from '../../../../testing/utils/mock-factory';
import { TeamInfo } from '../../../../app/models/team.model';
import { PitcherGlobal } from '../../../../app/models/pitcher.model';
import { PaginatedSearchs } from '../../../../app/models/pagination.model';

describe('Edit Menu Component Tests', () => {
	let component: EditMenuComponent;
	let fixture: ComponentFixture<EditMenuComponent>;
	let searchServiceSpy: jasmine.SpyObj<SearchService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;

	const teamStats = MockFactory.buildTeamMocks(
		'New York Yankees',
		'NYY',
		'American',
		'East',
		162,
		100,
		62,
		0.617,
		0,
		'7-3'
	);

	const stadium = MockFactory.buildStadiumCompleteMock(
		'Yankee Stadium',
		2009,
		'New York Yankees',
		[]
	);

	const mockTeamInfo: TeamInfo = MockFactory.buildTeamInfoMock(
		teamStats,
		'New York',
		'Founded in 1901',
		[1903, 1923, 1996, 2009],
		stadium,
		[],
		[]
	);

	beforeEach(() => {
		searchServiceSpy = jasmine.createSpyObj('SearchService', ['search']);
		backgroundServiceSpy = jasmine.createSpyObj('BackgroundColorService', [
			'getBackgroundColor',
		]);

		TestBed.configureTestingModule({
			imports: [EditMenuComponent],
			providers: [
				{ provide: SearchService, useValue: searchServiceSpy },
				{ provide: BackgroundColorService, useValue: backgroundServiceSpy },
			],
		});

		fixture = TestBed.createComponent(EditMenuComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should show error if searchType is player and the type of player is not provided', () => {
		component.searchQuery = 'Jasson Dominguez';
		component.searchType = 'player';
		component.playerType = null;
		component.performSearch();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('Please select the type of player you want to edit');
	});

	it('should perform search', () => {
		const mockResponse: PaginatedSearchs = MockFactory.buildPaginatedSearchsForTeam(mockTeamInfo);
		searchServiceSpy.search.and.returnValue(of(mockResponse));

		component.searchQuery = 'Yankees';
		component.searchType = 'team';
		component.performSearch();

		expect(component.searchResults.length).toBe(1);
		expect((component.searchResults[0] as TeamInfo).teamStats.abbreviation).toBe('NYY');
		expect(component.noResultsMessage).toBe('');
		expect(component.hasMore).toBeFalse();
	});

	it('should set noResultsMessage when search returns empty', () => {
		const mockResponse: PaginatedSearchs = {
			content: [],
			page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
		};
		searchServiceSpy.search.and.returnValue(of(mockResponse));

		component.searchQuery = 'Unknown';
		component.searchType = 'team';
		component.performSearch();

		expect(component.noResultsMessage).toBe('No results were found');
		expect(component.searchResults.length).toBe(0);
	});

	it('should handle search error', () => {
		searchServiceSpy.search.and.returnValue(throwError(() => new Error('Network error')));

		component.searchQuery = 'Yankees';
		component.searchType = 'team';
		component.performSearch();

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occur during the search');
	});

	it('should append results when loading next page', () => {
		const firstPage = MockFactory.buildPaginatedSearchsForTeam(mockTeamInfo);
		const teamStats2 = MockFactory.buildTeamMocks(
			'Mets',
			'NYM',
			'NL',
			'East',
			162,
			80,
			82,
			0.494,
			10,
			'5-5'
		);

		const stadium2 = MockFactory.buildStadiumCompleteMock('City Field', 2001, 'New York Mets', []);

		const mockTeamInfo2: TeamInfo = MockFactory.buildTeamInfoMock(
			teamStats2,
			'New York',
			'Founded in 1901',
			[1903, 1923, 1996, 2009],
			stadium2,
			[],
			[]
		);

		const secondPage = MockFactory.buildPaginatedSearchsForTeam(mockTeamInfo2);

		searchServiceSpy.search.and.returnValues(of(firstPage), of(secondPage));

		component.searchQuery = 'NY';
		component.searchType = 'team';
		component.performSearch();
		component.loadNextPage();

		expect(component.searchResults.length).toBe(1);
	});

	it('should set currentView correctly when editing a pitcher', () => {
		const mockPitcher: PitcherGlobal = MockFactory.buildPitcherGlobalMock(
			'Cole',
			'Yankees',
			'SP',
			30,
			15,
			5,
			2.5,
			200,
			220,
			40,
			150,
			60,
			1.05,
			0,
			0,
			{url: '', publicId: ''}
		);
		component.edit(mockPitcher);
		expect(component.currentView).toBe('player');
		expect(component.selectedResult).toBe(mockPitcher);
	});

	it('should toggle search type and player type states', () => {
		component.toggleSearchType(true);
		expect(component.isSearchTypeOpen).toBeTrue();
		component.toggleSearchType(false);
		expect(component.isSearchTypeOpen).toBeFalse();

		component.togglePlayerType(true);
		expect(component.isPlayerTypeOpen).toBeTrue();
		component.togglePlayerType(false);
		expect(component.isPlayerTypeOpen).toBeFalse();
	});
});