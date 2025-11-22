import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EditMenuComponent } from '../../../../app/components/admin/edit-menu/edit-menu.component';
import { SearchService } from '../../../../app/services/search.service';
import { BackgroundColorService } from '../../../../app/services/background-color.service';
import { MockFactory } from '../../../../testing/utils/mock-factory';
import { Team } from '../../../../app/models/team.model';
import { PitcherGlobal } from '../../../../app/models/pitcher.model';
import { PaginatedSearchs } from '../../../../app/models/pagination.model';

describe('Edit Menu Component Tests', () => {
	let component: EditMenuComponent;
	let fixture: ComponentFixture<EditMenuComponent>;
	let searchServiceSpy: jasmine.SpyObj<SearchService>;
	let backgroundServiceSpy: jasmine.SpyObj<BackgroundColorService>;

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
		}).compileComponents();

		fixture = TestBed.createComponent(EditMenuComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should show error if searchQuery is empty', () => {
		component.searchQuery = '';
		component.performSearch();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe(
			'Please enter the team, stadium or player you want to edit'
		);
	});

	it('should show error if searchType is player and playerType is null', () => {
		component.searchQuery = 'John Doe';
		component.searchType = 'player';
		component.playerType = null;
		component.performSearch();
		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('Please select the type of player you want to edit');
	});

	it('should perform search and set results', () => {
		const mockTeam = MockFactory.buildTeamMocks(
			'Yankees',
			'NYY',
			'AL',
			'East',
			162,
			90,
			72,
			0.556,
			0,
			'7-3'
		);
		const mockResponse: PaginatedSearchs = MockFactory.buildPaginatedSearchsForTeam(mockTeam);
		searchServiceSpy.search.and.returnValue(of(mockResponse));

		component.searchQuery = 'Yankees';
		component.searchType = 'team';
		component.performSearch();

		expect(component.searchResults.length).toBe(1);
		expect((component.searchResults[0] as Team).abbreviation).toBe('NYY');
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
		const mockTeam = MockFactory.buildTeamMocks(
			'Yankees',
			'NYY',
			'AL',
			'East',
			162,
			90,
			72,
			0.556,
			0,
			'7-3'
		);

		const firstPage = MockFactory.buildPaginatedSearchsForTeam(mockTeam);
		const mockTeam2 = MockFactory.buildTeamMocks(
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
		const secondPage = MockFactory.buildPaginatedSearchsForTeam(mockTeam2);

		searchServiceSpy.search.and.returnValues(of(firstPage), of(secondPage));

		component.searchQuery = 'NY';
		component.searchType = 'team';
		component.performSearch();
		component.loadNextPage();

		expect(component.searchResults.length).toBe(1);
	});

	it('should set currentView correctly when editing a team', () => {
		const mockTeam = MockFactory.buildTeamMocks(
			'Yankees',
			'NYY',
			'AL',
			'East',
			162,
			90,
			72,
			0.556,
			0,
			'7-3'
		);
		component.edit(mockTeam);
		expect(component.currentView).toBe('team');
		expect(component.selectedResult).toBe(mockTeam);
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
			0
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

	it('should call backgroundService.getBackgroundColor', () => {
		backgroundServiceSpy.getBackgroundColor.and.returnValue('bg-blue-900');
		const result = component.getBackgroundColor('NYY');
		expect(result).toBe('bg-blue-900');
		expect(backgroundServiceSpy.getBackgroundColor).toHaveBeenCalledWith('NYY');
	});
});