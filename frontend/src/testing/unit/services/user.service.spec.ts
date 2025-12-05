import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from '../../../app/services/user.service';
import { User } from '../../../app/models/user.model';
import { MockFactory } from '../../utils/mock-factory';
import { skip } from 'rxjs';
import { TeamSummary } from '../../../app/models/team.model';

describe('User Service Tests', () => {
	let service: UserService;
	let httpMock: HttpTestingController;
	const apiUrl = 'https://localhost:8443/api/v1/users';

	const mockUsers: User[] = [
		MockFactory.buildUserMocks('user1', 'user1@gmail.com'),
		MockFactory.buildUserMocks('user2', 'user2@gmail.com')
	];

	const mockFavteams: TeamSummary[] = [
		MockFactory.buildTeamSummaryMock('team1', 't1', 'AL', 'EAST'),
		MockFactory.buildTeamSummaryMock('team2', 't2', 'NL', 'WEST'),
	];

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [UserService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		service = TestBed.inject(UserService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should fetch all favorite teams of the active user', () => {
		service.favTeams$.pipe(skip(1)).subscribe((favs) => {
			expect(favs.length).toBe(2);
			expect(favs).toEqual(mockFavteams);
		});
		service.getFavTeams();

		const req = httpMock.expectOne(`${apiUrl}/favorites/teams`);
		expect(req.request.method).toBe('GET');
		expect(req.request.withCredentials).toBeTrue();
		req.flush(mockFavteams);
	});

	it('should return the currently selected favorite teams', () => {
		service.getFavTeams();
		const req = httpMock.expectOne(`${apiUrl}/favorites/teams`);
		expect(req.request.method).toBe('GET');
		req.flush(mockFavteams);

		const selectedTeams = service.getSelectedFavTeams();
		expect(selectedTeams.length).toBe(2);
		expect(selectedTeams).toEqual(mockFavteams);
	});

	it('should add the team to the favorite list', () => {
		const newFavTeam = MockFactory.buildTeamSummaryMock('team3', 't3', 'NL', 'CENTRAL');
		const mockResponse = MockFactory.buildMockResponse('SUCCESS', 'Team Succesfullly Added');
		service.addFavTeam(newFavTeam).subscribe((response) => {
			expect(response.status).toEqual(mockResponse.status);
			expect(response.message).toEqual(mockResponse.message);
			expect(response).toEqual(mockResponse);
		});

		const req = httpMock.expectOne(`${apiUrl}/favorites/teams/${newFavTeam.name}`);
		expect(req.request.method).toBe('POST');
		expect(req.request.withCredentials).toBeTrue();
		req.flush(mockResponse);
	});

	it('should remove the team from the favorite list', () => {
		const mockResponse = MockFactory.buildMockResponse('SUCCESS', 'Team Succesfullly Remove');
		service.removeFavTeam(mockFavteams[0]).subscribe((response) => {
			expect(response.status).toEqual(mockResponse.status);
			expect(response.message).toEqual(mockResponse.message);
			expect(response).toEqual(mockResponse);
		});

		const req = httpMock.expectOne(`${apiUrl}/favorites/teams/${mockFavteams[0].name}`);
		expect(req.request.method).toBe('DELETE');
		expect(req.request.withCredentials).toBeTrue();
		req.flush(mockResponse);
	});
});
