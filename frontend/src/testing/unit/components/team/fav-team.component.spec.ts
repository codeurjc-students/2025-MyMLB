import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FavTeamComponent } from '../../../../app/components/team/fav-team/fav-team.component';
import { UserService } from '../../../../app/services/user.service';
import { TeamService } from './../../../../app/services/team.service';
import { AuthService } from '../../../../app/services/auth.service';
import { SelectedTeamService } from '../../../../app/services/selected-team.service';
import { Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { MockFactory } from '../../../utils/mock-factory';
import { TeamSummary } from '../../../../app/models/team.model';

describe('FavTeam Component Tests', () => {
	let component: FavTeamComponent;
	let fixture: ComponentFixture<FavTeamComponent>;
	let mockUserService: any;
	let mockTeamService: any;
	let mockAuthService: any;
	let mockSelectedTeamService: any;
	let mockRouter: any;

	const mockFavTeams: TeamSummary[] = [
		MockFactory.buildTeamSummaryMock('Team A', 'TA', 'L1', 'D1'),
		MockFactory.buildTeamSummaryMock('Team B', 'TB', 'L1', 'D2'),
	];

	const allTeams: TeamSummary[] = [
		...mockFavTeams,
		MockFactory.buildTeamSummaryMock('Team C', 'TC', 'L2', 'D1'),
		MockFactory.buildTeamSummaryMock('Team D', 'TD', 'L2', 'D2'),
		MockFactory.buildTeamSummaryMock('Team E', 'TE', 'L1', 'D1'),
		MockFactory.buildTeamSummaryMock('Team F', 'TF', 'L1', 'D2'),
		MockFactory.buildTeamSummaryMock('Team G', 'TG', 'L2', 'D1'),
		MockFactory.buildTeamSummaryMock('Team H', 'TH', 'L2', 'D2'),
		MockFactory.buildTeamSummaryMock('Team I', 'TI', 'L1', 'D1'),
		MockFactory.buildTeamSummaryMock('Team J', 'TJ', 'L1', 'D2'),
		MockFactory.buildTeamSummaryMock('Team K', 'TK', 'L2', 'D1'),
		MockFactory.buildTeamSummaryMock('Team L', 'TL', 'L2', 'D2'),
		MockFactory.buildTeamSummaryMock('Team M', 'TM', 'L1', 'D1'),
		MockFactory.buildTeamSummaryMock('Team N', 'TN', 'L1', 'D2'),
	];

	const favTeamsSubject = new BehaviorSubject<TeamSummary[]>(mockFavTeams);

	beforeEach(async () => {
		mockUserService = {
			favTeams$: favTeamsSubject.asObservable(),
			getFavTeams: jasmine.createSpy('getFavTeams').and.callFake(() => {
				favTeamsSubject.next(mockFavTeams);
			}),
			addFavTeam: jasmine
				.createSpy('addFavTeam')
				.and.returnValue(of(MockFactory.buildMockResponse('Success', 'Added'))),
			removeFavTeam: jasmine
				.createSpy('removeFavTeam')
				.and.returnValue(of(MockFactory.buildMockResponse('Success', 'Removed'))),
		};

		mockTeamService = {
			getTeamsNamesAndAbbr: jasmine
				.createSpy('getTeamsNamesAndAbbr')
				.and.returnValue(of(allTeams))
		};

		mockAuthService = {
			getActiveUser: jasmine
				.createSpy('getActiveUser')
				.and.returnValue(of(MockFactory.buildMockUserRole('testUser', ['USER']))),
		};

		mockSelectedTeamService = {
			setSelectedTeam: jasmine.createSpy('setSelectedTeam'),
		};

		mockRouter = {
			navigate: jasmine.createSpy('navigate'),
		};

		await TestBed.configureTestingModule({
			imports: [FavTeamComponent],
			providers: [
				{ provide: UserService, useValue: mockUserService },
				{ provide: TeamService, useValue: mockTeamService },
				{ provide: AuthService, useValue: mockAuthService },
				{ provide: SelectedTeamService, useValue: mockSelectedTeamService },
				{ provide: Router, useValue: mockRouter }
			],
		}).compileComponents();

		fixture = TestBed.createComponent(FavTeamComponent);
		component = fixture.componentInstance;
		favTeamsSubject.next(mockFavTeams);
		fixture.detectChanges();
	});

	describe('ngOnInit', () => {
		it('should initialize favTeams$ and call service methods', () => {
			expect(component.favTeams$).toEqual(mockUserService.favTeams$);
			expect(mockUserService.getFavTeams).toHaveBeenCalled();
			expect(mockAuthService.getActiveUser).toHaveBeenCalled();
			expect(component.username).toBe('testUser');
			expect(component.errorMessage).toBe('');
		});

		it('should set errorMessage if getActiveUser fails', () => {
			mockAuthService.getActiveUser.and.returnValue(
				throwError(() => new Error('Auth Failed'))
			);
			const newFixture = TestBed.createComponent(FavTeamComponent);
			const newComponent = newFixture.componentInstance;
			newFixture.detectChanges();

			expect(newComponent.username).toBe('');
			expect(newComponent.errorMessage).toBe('Unexpected error while retrieving the user');
		});
	});

	describe('loadTeams', () => {
		it('should set addButtonClicked to true and load available teams', () => {
			component.addButtonClicked = false;
			component.errorMessage = 'Previous Error';
			component.loadTeams();

			expect(component.addButtonClicked).toBeTrue();
			expect(component.errorMessage).toBe('');
			expect(mockTeamService.getTeamsNamesAndAbbr).toHaveBeenCalled();

			const expectedAvailableTeams = allTeams.filter(
				(t) => t.name !== 'Team A' && t.name !== 'Team B'
			);
			expect(component.availableTeams.length).toBe(allTeams.length - 2);
			expect(component.availableTeams).toEqual(expectedAvailableTeams);

			expect(component.currentPage).toBe(0);
			expect(component.visibleTeams.length).toBe(10);
			expect(component.visibleTeams).toEqual(expectedAvailableTeams.slice(0, 10));
		});

		it('should set errorMessage if getTeamsNamesAndAbbr fails', () => {
			mockTeamService.getTeamsNamesAndAbbr.and.returnValue(
				throwError(() => new Error('Load Teams Failed'))
			);
			component.loadTeams();
			expect(component.errorMessage).toBe('Error while loading the teams');
			expect(component.availableTeams.length).toBe(0);
		});
	});

	describe('addFavoriteTeam', () => {
		const teamToAdd = MockFactory.buildTeamSummaryMock('New Team', 'NT', 'L3', 'D3');

		beforeEach(() => {
			mockTeamService.getTeamsNamesAndAbbr.and.returnValue(of(allTeams));
		});

		it('should call userService.addFavTeam and show success modal on success', () => {
			component.addFavoriteTeam(teamToAdd);

			expect(mockUserService.addFavTeam).toHaveBeenCalledWith(teamToAdd);
			expect(component.successMessage).toBe('Team successfully added');
			expect(component.showSuccessModal).toBeTrue();
			expect(component.errorMessage).toBe('');

			expect(mockUserService.getFavTeams).toHaveBeenCalledTimes(2);
			expect(mockTeamService.getTeamsNamesAndAbbr).toHaveBeenCalledTimes(1);
		});
	});

	describe('Remove Favorite Team', () => {
		const teamToRemove = mockFavTeams[0];

		it('should confirm delete and show success modal on success', () => {
			component.openDeleteModal(teamToRemove);

			mockTeamService.getTeamsNamesAndAbbr.and.returnValue(of(allTeams));

			component.confirmDeleteTeam();

			expect(mockUserService.removeFavTeam).toHaveBeenCalledWith(teamToRemove);
			expect(component.showDeleteModal).toBeFalse();
			expect(component.successMessage).toBe('Team successfully removed');
			expect(component.showSuccessModal).toBeTrue();
			expect(component.errorMessage).toBe('');

			expect(mockUserService.getFavTeams).toHaveBeenCalledTimes(2);
			expect(mockTeamService.getTeamsNamesAndAbbr).toHaveBeenCalledTimes(1);
		});
	});

	describe('loadMoreTeams', () => {
		beforeEach(() => {
			component.loadTeams();
			expect(component.availableTeams.length).toBe(12);
			expect(component.visibleTeams.length).toBe(10);
			expect(component.currentPage).toBe(0);
		});

		it('should increment currentPage and load next page of teams', () => {
			component.loadMoreTeams();

			expect(component.currentPage).toBe(1);
			expect(component.visibleTeams.length).toBe(12);

			const expectedLastTwo = allTeams
				.filter((t) => t.name !== 'Team A' && t.name !== 'Team B')
				.slice(10, 12);
			expect(component.visibleTeams[10]).toEqual(expectedLastTwo[0]);
			expect(component.visibleTeams[11]).toEqual(expectedLastTwo[1]);
		});

		it('should load only the remaining teams if less than pageSize are left', () => {
			component.loadMoreTeams();

			component.loadMoreTeams();

			expect(component.currentPage).toBe(2);
			expect(component.visibleTeams.length).toBe(12);

			const expectedAvailableTeams = allTeams.filter(
				(t) => t.name !== 'Team A' && t.name !== 'Team B'
			);
			expect(component.visibleTeams).toEqual(expectedAvailableTeams);
		});
	});
});