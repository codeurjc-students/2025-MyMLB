// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { FavTeamComponent } from '../../app/components/team/fav-team/fav-team.component';
// import { UserService } from '../../app/services/user.service';
// import { TeamService, SimplifiedTeam } from '../../app/services/team.service';
// import { AuthService } from '../../app/services/auth.service';
// import { SelectedTeamService } from '../../app/services/selected-team.service';
// import { Router } from '@angular/router';
// import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
// import { provideHttpClient, withFetch } from '@angular/common/http';
// import { MockFactory } from '../utils/mock-factory';

// describe('Favorite Team Component Integration Test', () => {
// 	let fixture: ComponentFixture<FavTeamComponent>;
// 	let component: FavTeamComponent;
// 	let httpMock: HttpTestingController;
// 	let routerSpy: jasmine.SpyObj<Router>;

// 	const apiUrl = 'https://localhost:8443/api/users';
// 	const favTeamsUrl = `${apiUrl}/favorites/teams`;
// 	const activeUserUrl = 'https://localhost:8443/api/auth/me';
// 	const standingsUrl = 'https://localhost:8443/api/teams/standings';

// 	const mockTeams: SimplifiedTeam[] = [
// 		MockFactory.buildSimpliefiedTeamMock('team1', 't1', 'AL', 'EAST'),
// 		MockFactory.buildSimpliefiedTeamMock('team2', 't2', 'NL', 'WEST'),
// 	];

// 	const mockUserRole = MockFactory.buildMockUserRole('user1', ['USER']);

// 	beforeEach(() => {
// 		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

// 		TestBed.configureTestingModule({
// 			imports: [FavTeamComponent],
// 			providers: [
// 				UserService,
// 				TeamService,
// 				AuthService,
// 				SelectedTeamService,
// 				{ provide: Router, useValue: routerSpy },
// 				provideHttpClient(withFetch()),
// 				provideHttpClientTesting(),
// 			],
// 		});

// 		httpMock = TestBed.inject(HttpTestingController);

// 		httpMock.expectOne(activeUserUrl).flush(mockUserRole);

// 		fixture = TestBed.createComponent(FavTeamComponent);
// 		component = fixture.componentInstance;
// 		component.ngOnInit();

// 		httpMock.expectOne(favTeamsUrl).flush(mockTeams);

// 		httpMock.expectOne(activeUserUrl).flush(mockUserRole);

// 		fixture.detectChanges();
// 	});

// 	afterEach(() => {
// 		httpMock.verify();
// 	});

// 	it('should initialize with favorite teams and username', () => {
// 		component.favTeams$.subscribe((teams) => {
// 			expect(teams.length).toBe(2);
// 			expect(teams[0].name).toBe('team1');
// 			expect(component.username).toBe('user1');
// 		});
// 	});

// 	it('should add a favorite team successfully', () => {
// 		const newTeam = MockFactory.buildSimpliefiedTeamMock('team3', 't3', 'NL', 'CENTRAL');
// 		component.addFavoriteTeam(newTeam);

// 		httpMock.expectOne(`${favTeamsUrl}/team3`).flush(MockFactory.buildMockResponse('SUCCESS', 'Team successfully added'));

// 		httpMock.expectOne(favTeamsUrl).flush([...mockTeams, newTeam]);

// 		httpMock.expectOne(standingsUrl).flush({});

// 		expect(component.successMessage).toBe('Team successfully added');
// 		expect(component.showSuccessModal).toBeTrue();
// 	});

// 	it('should remove a favorite team successfully', () => {
// 		const teamToRemove = mockTeams[0];
// 		component.openDeleteModal(teamToRemove);
// 		component.confirmDeleteTeam();

// 		httpMock.expectOne(`${favTeamsUrl}/${teamToRemove.name}`).flush(MockFactory.buildMockResponse('SUCCESS', 'Team successfully removed'));

// 		httpMock.expectOne(favTeamsUrl).flush(mockTeams.filter((t) => t.name !== teamToRemove.name));

// 		httpMock.expectOne(standingsUrl).flush({});

// 		expect(component.successMessage).toBe('Team successfully removed');
// 		expect(component.showSuccessModal).toBeTrue();
// 		expect(component.showDeleteModal).toBeFalse();
// 	});
// });