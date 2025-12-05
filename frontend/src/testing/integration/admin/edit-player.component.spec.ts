import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { EditPlayerComponent } from '../../../app/components/admin/edit-player/edit-player.component';
import { PlayerService } from '../../../app/services/player.service';
import { TeamService } from '../../../app/services/team.service';

import { PositionPlayerGlobal } from '../../../app/models/position-player.model';
import { TeamSummary } from '../../../app/models/team.model';
import { Pictures } from '../../../app/models/pictures.model';
import { MockFactory } from '../../utils/mock-factory';

describe('Edit Player Component Integration Tests', () => {
	let fixture: ComponentFixture<EditPlayerComponent>;
	let component: EditPlayerComponent;
	let httpMock: HttpTestingController;

	const apiPlayers = 'https://localhost:8443/api/v1/players';
	const apiTeams = 'https://localhost:8443/api/v1/teams';

	const mockPlayer: PositionPlayerGlobal = MockFactory.buildPositionPlayerGlobalMock(
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
		TestBed.configureTestingModule({
			imports: [EditPlayerComponent],
			providers: [
				PlayerService,
				TeamService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
			],
		});

		fixture = TestBed.createComponent(EditPlayerComponent);
		component = fixture.componentInstance;
		component.player = { ...mockPlayer };
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should upload a valid .webp picture successfully', () => {
		const file = new File(['dummy'], 'test.webp', { type: 'image/webp' });

		component.uploadPicture(mockPlayer.name, file);

		const req = httpMock.expectOne(`${apiPlayers}/${mockPlayer.name}/pictures`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body instanceof FormData).toBeTrue();

		const newPic: Pictures = { url: 'http://new.com/pic.webp', publicId: 'new001' };
		req.flush(newPic);

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Picture uploaded successfully');
		expect(component.player.picture?.publicId).toBe('new001');
	});

	it('should delete the player successfully', () => {
		component.deletePlayer();

		const req = httpMock.expectOne(`${apiPlayers}/${mockPlayer.name}`);
		expect(req.request.method).toBe('DELETE');

		req.flush({ status: 'SUCCESS', message: 'deleted' });

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Aaron Judge successfully deleted');
	});

	it('should load available teams when opening modal', () => {
		component.showTeamsModal();

		const req = httpMock.expectOne(`${apiTeams}/available?page=0&size=10`);
		expect(req.request.method).toBe('GET');

		const mockTeam: TeamSummary = MockFactory.buildTeamSummaryMock(
			'Los Angeles Dodgers',
			'LAD',
			'NL',
			'West'
		);

		req.flush({
			content: [mockTeam],
			page: { size: 10, number: 0, totalPages: 1, totalElements: 1 },
		});

		expect(component.selectTeamButtonClicked).toBeTrue();
		component.selector.items$.subscribe((item) => {
			expect(item.length).toBe(1);
			expect(item[0].name).toBe('Los Angeles Dodgers');
		});
	});

	it('should send update request for position player', () => {
		component.formInputs.atBats = 999;

		component.confirm();

		const req = httpMock.expectOne(`${apiPlayers}/position-players/Aaron Judge`);
		expect(req.request.method).toBe('PATCH');

		req.flush({ status: 'SUCCESS' });

		expect(component.finish).toBeTrue();
	});
});