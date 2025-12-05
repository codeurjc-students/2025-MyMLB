import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Router } from '@angular/router';

import { CreatePlayerComponent } from '../../../app/components/admin/create-player/create-player.component';
import { PlayerService } from '../../../app/services/player.service';
import { TeamService } from '../../../app/services/team.service';
import { PaginatedSelectorService } from '../../../app/services/utilities/paginated-selector.service';
import { MockFactory } from '../../utils/mock-factory';
import { PositionPlayerGlobal } from '../../../app/models/position-player.model';

describe('Create Player Component Integration Tests', () => {
	let fixture: ComponentFixture<CreatePlayerComponent>;
	let component: CreatePlayerComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const apiPlayers = 'https://localhost:8443/api/v1/players';
	const apiTeams = 'https://localhost:8443/api/v1/teams';

	const mockPositionPlayer: PositionPlayerGlobal = MockFactory.buildPositionPlayerGlobalMock(
		'Aaron Judge',
		'New York Yankees',
		'RF',
		500,
		80,
		150,
		30,
		2,
		45,
		110,
		0.3,
		0.4,
		1.0,
		0.6,
		{ url: 'http://pic.com/a', publicId: 'a1' }
	);

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [CreatePlayerComponent],
			providers: [
				PlayerService,
				TeamService,
				PaginatedSelectorService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				{ provide: Router, useValue: routerSpy },
			],
		});

		fixture = TestBed.createComponent(CreatePlayerComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should create a position player successfully', () => {
		component.nameInput = mockPositionPlayer.name;
		component.playerNumberInput = 99;
		component.teamNameInput = mockPositionPlayer.teamName;
		component.positionInput = mockPositionPlayer.position;

		component.createPlayer();

		const req = httpMock.expectOne(`${apiPlayers}/position-players`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body.name).toBe('Aaron Judge');

		req.flush(mockPositionPlayer);

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Aaron Judge  successfully created');
	});

	it('should select a team and update teamNameInput', () => {
		const team = MockFactory.buildTeamSummaryMock('Boston Red Sox', 'BOS', 'AL', 'EAST');
		component.selectTeam(team);

		expect(component.selectedTeam).toBeTrue();
		expect(component.successMessage).toBe('Team Selected');
		expect(component.teamNameInput).toBe('Boston Red Sox');
	});
});