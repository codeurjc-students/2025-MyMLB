import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StadiumService } from '../../../app/services/stadium.service';
import { Pictures } from '../../../app/models/pictures.model';
import { AuthResponse } from '../../../app/models/auth/auth-response.model';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { PaginatedStadiums } from '../../../app/models/pagination.model';
import { MockFactory } from '../../utils/mock-factory';

describe('Stadium Service Tests', () => {
	let service: StadiumService;
	let httpMock: HttpTestingController;

	const apiUrl = 'https://localhost:8443/api/v1/stadiums';

	const mockPicture: Pictures = {
		url: 'https://example.com/image.webp',
		publicId: 'abc123',
	};

	const mockAuthResponse: AuthResponse = {
		status: 'SUCCESS',
		message: 'Picture removed successfully'
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [StadiumService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		service = TestBed.inject(StadiumService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should return all available stadiums', () => {
		const mockResponse: PaginatedStadiums = {
		content: [
			{
			name: 'Yankee Stadium',
			openingDate: 2009,
			teamName: 'New York Yankees',
			pictures: [],
			},
		],
		page: {
			size: 10,
			number: 0,
			totalElements: 1,
			totalPages: 1,
		},
		};

		service.getAvailableStadiums(0, 10).subscribe((stadiums) => {
		expect(stadiums.content.length).toBe(1);
		expect(stadiums.content[0].name).toBe('Yankee Stadium');
		expect(stadiums.page.totalPages).toBe(1);
	});

    const req = httpMock.expectOne(`${apiUrl}/available?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

	it('should fetch stadium pictures', () => {
		const stadiumName = 'Yankee Stadium';

		service.getStadiumPictures(stadiumName).subscribe((pictures) => {
			expect(pictures.length).toBe(1);
			expect(pictures[0]).toEqual(mockPicture);
		});

		const req = httpMock.expectOne(
			`${apiUrl}/${stadiumName}/pictures`
		);
		expect(req.request.method).toBe('GET');
		req.flush([mockPicture]);
	});

	it('should upload a stadium picture', () => {
		const stadiumName = 'Yankee Stadium';
		const file = new File(['dummy'], 'test.webp', { type: 'image/webp' });

		service.uploadPicture(stadiumName, file).subscribe((picture) => {
			expect(picture).toEqual(mockPicture);
		});

		const req = httpMock.expectOne(
			`${apiUrl}/${stadiumName}/pictures`
		);
		expect(req.request.method).toBe('POST');
		expect(req.request.body instanceof FormData).toBeTrue();
		req.flush(mockPicture);
	});

	it('should remove a stadium picture', () => {
		const stadiumName = 'Yankee Stadium';
		const publicId = 'abc123';

		service.removePicture(stadiumName, publicId).subscribe((response) => {
			expect(response.status).toBe('SUCCESS');
			expect(response.message).toBe('Picture removed successfully');
		});

		const req = httpMock.expectOne(
			`${apiUrl}/${stadiumName}/pictures?publicId=${publicId}`
		);
		expect(req.request.method).toBe('DELETE');
		req.flush(mockAuthResponse);
	});

	it('should create a stadium', () => {
		const request = { name: 'Fenway Park', openingDate: 1912 };
		const mockSummary = MockFactory.buildStadiumMock('Fenway Park', 1912, []);

		service.createStadium(request).subscribe((summary) => {
		expect(summary.name).toBe('Fenway Park');
		expect(summary.openingDate).toBe(1912);
		expect(summary.pictures.length).toBe(0);
		});

		const req = httpMock.expectOne(apiUrl);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual(request);
		req.flush(mockSummary);
	});
});