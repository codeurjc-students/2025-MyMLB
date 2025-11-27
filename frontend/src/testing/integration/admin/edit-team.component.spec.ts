import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { EditTeamComponent } from '../../../app/components/admin/edit-team/edit-team.component';
import { TeamService } from '../../../app/services/team.service';
import { StadiumService } from '../../../app/services/stadium.service';
import { BackgroundColorService } from '../../../app/services/background-color.service';
import { TeamInfo } from '../../../app/models/team.model';
import { Stadium } from '../../../app/models/stadium.model';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';

describe('Edit Team Component Integration Tests', () => {
	let fixture: ComponentFixture<EditTeamComponent>;
	let component: EditTeamComponent;
	let httpMock: HttpTestingController;

	const teamApiUrl = 'https://localhost:8443/api/v1/teams';
	const stadiumApiUrl = 'https://localhost:8443/api/v1/stadiums';

	const mockTeamInfo: TeamInfo = {
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

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [EditTeamComponent],
			providers: [
				TeamService,
				StadiumService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				{
					provide: BackgroundColorService,
					useValue: { getBackgroundColor: () => 'bg-default' },
				},
			],
		});

		fixture = TestBed.createComponent(EditTeamComponent);
		component = fixture.componentInstance;
		component.team = { ...mockTeamInfo };
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('initializes stadiumInput from team', () => {
		expect(component.stadiumInput).toBe('Yankee Stadium');
	});

	it('loads stadiums via StadiumService', () => {
		component.showStadiumsModal();

		const req = httpMock.expectOne(
			`${stadiumApiUrl}/available?page=0&size=${component.pageSize}`
		);
		expect(req.request.method).toBe('GET');

		req.flush({
			content: [
				{
					name: 'Fenway Park',
					openingDate: 1912,
					teamName: 'Boston Red Sox',
					pictures: [],
				} as Stadium,
			],
			page: { size: component.pageSize, number: 0, totalElements: 1, totalPages: 1 },
		});

		expect(component.availableStadiums.length).toBe(1);
		expect(component.availableStadiums[0].name).toBe('Fenway Park');
		expect(component.availableStadiums[0].teamName).toBe('Boston Red Sox');
	});

	it('updates team on confirm success', () => {
		component.cityInput = 'Boston';
		component.confirm();

		const req = httpMock.expectOne(`${teamApiUrl}/${mockTeamInfo.teamStats.name}`);
		expect(req.request.method).toBe('PATCH');

		const response: AuthResponse = { status: 'SUCCESS', message: 'Team successfully updated' };
		req.flush(response);

		expect(component.finish).toBeTrue();
		expect(component.team.city).toBe('Boston');
	});

	it('sets error flag on confirm failure', () => {
		component.confirm();

		const req = httpMock.expectOne(`${teamApiUrl}/${mockTeamInfo.teamStats.name}`);
		req.flush({}, { status: 400, statusText: 'Bad Request' });

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toContain('Invalid Stadium');
	});

	it('emits backToMenu when goToEditMenu is called', () => {
		spyOn(component.backToMenu, 'emit');
		component.goToEditMenu();
		expect(component.backToMenu.emit).toHaveBeenCalled();
	});
});