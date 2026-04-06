import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PlayerService } from '../../../app/services/player.service';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import {
	CreatePlayerRequest,
	PlayerRanking,
	PositionPlayerGlobal,
} from '../../../app/models/position-player.model';
import { MockFactory } from '../../utils/mock-factory';
import { Pictures } from '../../../app/models/pictures.model';
import { PitcherGlobal } from '../../../app/models/pitcher.model';
import { AuthResponse } from '../../../app/models/auth.model';
import { PaginatedResponse } from '../../../app/models/pagination.model';

describe('Player Service Tests', () => {
	let service: PlayerService;
	let httpMock: HttpTestingController;

	const mockCreatePitcherRequest: CreatePlayerRequest = {
		name: 'Paul Skenes',
		playerNumber: 67,
		position: 'SP',
		teamName: 'Pitsburgh Pirates',
	};

	const mockCreatePositionPlayerRequest: CreatePlayerRequest = {
		name: 'Gleyber Torres',
		playerNumber: 25,
		position: '2B',
		teamName: 'Detroit Tigers',
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [PlayerService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});
		service = TestBed.inject(PlayerService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should create a position player successfully', () => {
		const mockPositionPlayer: PositionPlayerGlobal = MockFactory.buildPositionPlayerGlobalMock(
			'Gleyber Torres',
			'Detroit Tigers',
			'2B',
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			{ url: '', publicId: '' }
		);
		service.createPositionPlayer(mockCreatePositionPlayerRequest).subscribe((response) => {
			expect(response).toEqual(mockPositionPlayer);
			expect(response.name).toEqual('Gleyber Torres');
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/position-player`);
		expect(req.request.method).toBe('POST');
		req.flush(mockPositionPlayer);
	});

	it('should create a pitcher successfully', () => {
		const mockPitcher: PitcherGlobal = {
			name: 'Paul Skenes',
			playerNumber: 67,
			position: 'SP',
			teamName: 'Pittsburgh Pirates',
			era: 3.2,
			games: 10,
			wins: 5,
			losses: 2,
			inningsPitched: 60,
			totalStrikeouts: 80,
			walks: 10,
			hitsAllowed: 40,
			runsAllowed: 20,
			whip: 1.1,
			saves: 0,
			saveOpportunities: 0,
			picture: { url: '', publicId: '' },
			apiDataSource: true
		};

		service.createPitcher(mockCreatePitcherRequest).subscribe((response) => {
			expect(response).toEqual(mockPitcher);
			expect(response.name).toEqual('Paul Skenes');
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/pitcher`);
		expect(req.request.method).toBe('POST');
		req.flush(mockPitcher);
	});

	it('should update picture successfully', () => {
		const mockFile = new File([''], 'pic.webp', { type: 'image/webp' });
		const mockPictures: Pictures = { url: 'http://img.com/pic.webp', publicId: '123' };

		service.updatePicture('Paul Skenes', mockFile).subscribe((response) => {
			expect(response).toEqual(mockPictures);
			expect(response.url).toContain('pic.webp');
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/Paul Skenes/pictures`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body instanceof FormData).toBeTrue();
		req.flush(mockPictures);
	});

	it('should update position player successfully', () => {
		const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Player successfully updated' };

		service
			.updatePositionPlayer('Gleyber Torres', { hits: 50 } as any)
			.subscribe((response) => {
				expect(response).toEqual(mockResponse);
			});

		const req = httpMock.expectOne(`${service['apiUrl']}/position-players/Gleyber Torres`);
		expect(req.request.method).toBe('PATCH');
		req.flush(mockResponse);
	});

	it('should update pitcher successfully', () => {
		const mockResponse: AuthResponse = { status: 'SUCCESS', message: 'Player successfully updated' };

		service.updatePitcher('Paul Skenes', { era: 2.5 } as any).subscribe((response) => {
			expect(response).toEqual(mockResponse);
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/pitchers/Paul Skenes`);
		expect(req.request.method).toBe('PATCH');
		req.flush(mockResponse);
	});

	it('should delete player successfully', () => {
		service.deletePlayer('Paul Skenes').subscribe((response) => {
			expect(response).toEqual({ success: true });
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/Paul Skenes`);
		expect(req.request.method).toBe('DELETE');
		req.flush({ success: true });
	});

	it('should fetch single stat rankings with pagination and filters', () => {
		const mockPaginatedResponse: PaginatedResponse<PlayerRanking> = {
			content: [
				{ name: 'Aaron Judge', picture: '', stat: 0.333 },
				{ name: 'Jose Altuve', picture: '', stat: 0.300 }
			],
			page: { size: 10, totalElements: 2, totalPages: 1, number: 0 }
		};

		const teams = ['New York Yankees'];
		service.getPlayerSingleStatRankings(0, 10, 'position', 'average', teams, 'AL', 'EAST')
			.subscribe((response) => {
				expect(response.content.length).toBe(2);
				expect(response.content[0].name).toBe('Aaron Judge');
			});

		const req = httpMock.expectOne((request) =>
			request.url === `${service['apiUrl']}/rankings` &&
			request.params.get('page') === '0' &&
			request.params.get('size') === '10' &&
			request.params.get('playerType') === 'position' &&
			request.params.get('stat') === 'average' &&
			request.params.get('teamNames') === 'New York Yankees' &&
			request.params.get('league') === 'AL' &&
			request.params.get('division') === 'EAST'
		);

		expect(req.request.method).toBe('GET');
		req.flush(mockPaginatedResponse);
	});

	it('should fetch all stats rankings', () => {
		const mockAllRankings: Record<string, PlayerRanking[]> = {
			homeRuns: [{ name: 'Aaron Judge', picture: '', stat: 40 }],
			average: [{ name: 'Jose Altuve', picture: '', stat: 0.333 }]
		};

		service.getPlayerAllStatsRankings('position', undefined, 'NL').subscribe((response) => {
			expect(response['average'][0].stat).toBe(0.333);
			expect(response['homeRuns'][0].stat).toBe(40);
		});

		const req = httpMock.expectOne((request) =>
			request.url === `${service['apiUrl']}/rankings/all` &&
			request.params.get('playerType') === 'position' &&
			request.params.get('league') === 'NL'
		);

		expect(req.request.method).toBe('GET');
		req.flush(mockAllRankings);
	});

	it('should trigger refresh player rankings successfully', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Player Rankings successfully updated!'
		};

		service.refreshPlayerRankings().subscribe((response) => {
			expect(response.status).toBe('SUCCESS');
			expect(response.message).toContain('updated');
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/sync`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual({});
		req.flush(mockResponse);
	});
});