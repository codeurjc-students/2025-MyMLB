import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FavTeamComponent } from '../../../app/components/team/fav-team/fav-team.component';
import { UserService } from '../../../app/services/user.service';
import { TeamService } from '../../../app/services/team.service';
import { AuthService } from '../../../app/services/auth.service';
import { SelectedTeamService } from '../../../app/services/selected-team.service';
import { Router } from '@angular/router';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { MockFactory } from '../../utils/mock-factory';
import { TeamSummary } from '../../../app/models/team.model';

describe('Favorite Team Component Integration Test', () => {
	let fixture: ComponentFixture<FavTeamComponent>;
	let component: FavTeamComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const apiUrl = 'https://localhost:8443/api/users';
	const favTeamsUrl = `${apiUrl}/favorites/teams`;
	const activeUserUrl = 'https://localhost:8443/api/auth/me';
	const standingsUrl = 'https://localhost:8443/api/teams/standings';

	const mockTeams: TeamSummary[] = [
		MockFactory.buildTeamSummaryMock('team1', 't1', 'AL', 'EAST'),
		MockFactory.buildTeamSummaryMock('team2', 't2', 'NL', 'WEST'),
	];

	const mockUserRole = MockFactory.buildMockUserRole('user1', ['USER']);

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [FavTeamComponent],
			providers: [
				UserService,
				TeamService,
				AuthService,
				SelectedTeamService,
				{ provide: Router, useValue: routerSpy },
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		httpMock = TestBed.inject(HttpTestingController);

		fixture = TestBed.createComponent(FavTeamComponent);
		component = fixture.componentInstance;
		component.ngOnInit();

		httpMock.match(favTeamsUrl).forEach(req => req.flush(mockTeams));
		httpMock.match(activeUserUrl).forEach(req => req.flush(mockUserRole));

		fixture.detectChanges();

		httpMock.match(favTeamsUrl).forEach(req => req.flush(mockTeams));
		httpMock.match(activeUserUrl).forEach(req => req.flush(mockUserRole));
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should initialize with favorite teams and username', () => {
		component.favTeams$.subscribe((teams) => {
			expect(teams.length).toBe(2);
			expect(teams[0].name).toBe('team1');
			expect(component.username).toBe('user1');
		});
		httpMock.match(favTeamsUrl).forEach(req => req.flush(mockTeams));
		httpMock.match(activeUserUrl).forEach(req => req.flush(mockUserRole));
		httpMock.verify();
	});

	it('should add a favorite team successfully', () => {
		const newTeam = MockFactory.buildTeamSummaryMock('team3', 't3', 'NL', 'CENTRAL');
		component.addFavoriteTeam(newTeam);

		httpMock.expectOne(`${favTeamsUrl}/team3`).flush(MockFactory.buildMockResponse('SUCCESS', 'Team successfully added'));

		httpMock.match(favTeamsUrl).forEach(req => req.flush([...mockTeams, newTeam]));

		httpMock.match(activeUserUrl).forEach(req => req.flush(mockUserRole));

		httpMock.match(standingsUrl).forEach(req => req.flush({}));

		expect(component.successMessage).toBe('Team successfully added');
		expect(component.showSuccessModal).toBeTrue();
	});

	it('should remove a favorite team successfully', () => {
		const teamToRemove = mockTeams[0];
		component.openDeleteModal(teamToRemove);
		component.confirmDeleteTeam();

		httpMock.expectOne(`${favTeamsUrl}/${teamToRemove.name}`).flush(MockFactory.buildMockResponse('SUCCESS', 'Team successfully removed'));

		httpMock.match(favTeamsUrl).forEach(req => req.flush(mockTeams.filter(t => t.name !== teamToRemove.name)));

		httpMock.match(activeUserUrl).forEach(req => req.flush(mockUserRole));

		httpMock.match(standingsUrl).forEach(req => req.flush({}));

		expect(component.successMessage).toBe('Team successfully removed');
		expect(component.showSuccessModal).toBeTrue();
		expect(component.showDeleteModal).toBeFalse();
	});
});