import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StadiumService } from '../../../app/services/stadium.service';
import { Pictures } from '../../../app/models/pictures.model';
import { AuthResponse } from '../../../app/models/auth.model';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { MockFactory } from '../../utils/mock-factory';

describe('Stadium Service Tests', () => {
	let service: StadiumService;
	let httpMock: HttpTestingController;

	const mockAuthResponse: AuthResponse = {
		status: 'SUCCESS',
		message: 'Picture removed successfully',
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
		const mockPictureMap: Pictures = {
			url: 'https://test_pic/123',
			publicId: '123'
		};
		const mockStadium = MockFactory.buildStadiumCompleteMock(
			'Yankee Stadium',
			2009,
			'New York Yankees',
			[],
			mockPictureMap
		);
		const mockResponse = MockFactory.buildPaginatedResponse(mockStadium);

		service.getAvailableStadiums(0, 10).subscribe((stadiums) => {
			expect(stadiums.content.length).toBe(1);
			expect(stadiums.content[0].name).toBe('Yankee Stadium');
			expect(stadiums.page.totalPages).toBe(1);
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/available?page=0&size=10`);
		expect(req.request.method).toBe('GET');
		req.flush(mockResponse);
	});

	// Parametrized Tests for picture uploading
	[
		{
			description: 'upload a stadium picture',
			method: (name: string, file: File) => service.uploadPicture(name, file),
			suffix: ''
		},
		{
			description: 'update the stadium map picture',
			method: (name: string, file: File) => service.editStadiumMapPicture(name, file),
			suffix: '/map'
		}
	].forEach(({ description, method, suffix }) => {

		it(`should ${description}`, () => {
			const stadiumName = 'Yankee Stadium';
			const file = new File(['dummy'], 'test.png', { type: 'image/png' });
			const mockResponse: Pictures = {
				url: `https://example.com/${file.name}`,
				publicId: 'abc123',
			};

			method(stadiumName, file).subscribe((picture) => {
				expect(picture).toEqual(mockResponse);
			});

			const req = httpMock.expectOne(`${service['apiUrl']}/${stadiumName}/pictures${suffix}`);

			expect(req.request.method).toBe('POST');
			expect(req.request.body instanceof FormData).toBeTrue();
			expect(req.request.body.has('file')).toBeTrue();

			req.flush(mockResponse);
		});
	});

	it('should remove a stadium picture', () => {
		const stadiumName = 'Yankee Stadium';
		const publicId = 'abc123';

		service.removePicture(stadiumName, publicId).subscribe((response) => {
			expect(response.status).toBe('SUCCESS');
			expect(response.message).toBe('Picture removed successfully');
		});

		const req = httpMock.expectOne(`${service['apiUrl']}/${stadiumName}/pictures?publicId=${publicId}`);
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

		const req = httpMock.expectOne(service['apiUrl']);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual(request);
		req.flush(mockSummary);
	});
});