import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { EditStadiumComponent } from '../../../app/components/admin/edit-stadium/edit-stadium.component';
import { StadiumService } from '../../../app/services/stadium.service';
import { Stadium } from '../../../app/models/stadium.model';
import { Pictures } from '../../../app/models/pictures.model';

describe('Edit Stadium Component Integration Tests', () => {
	let fixture: ComponentFixture<EditStadiumComponent>;
	let component: EditStadiumComponent;
	let httpMock: HttpTestingController;

	const apiUrl = '/api/v1/stadiums';

	const mockStadium: Stadium = {
		name: 'Yankee Stadium',
		openingDate: 2009,
		teamName: 'New York Yankees',
		pictures: [{ url: 'http://test.com/pic.webp', publicId: 'pic1' }],
		pictureMap: {
			url: 'https://test_pic/123',
			publicId: '123'
		}
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [EditStadiumComponent],
			providers: [StadiumService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		fixture = TestBed.createComponent(EditStadiumComponent);
		component = fixture.componentInstance;
		component.stadium = { ...mockStadium };
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	// Parametrized Tests for picture uploading
	[
	{
		description: 'upload a valid picture successfully',
		isMap: false,
		suffix: '',
		check: (newPic: Pictures) => expect(component.pictures).toContain(newPic)
	},
	{
		description: 'update the stadium map picture successfully',
		isMap: true,
		suffix: '/map',
		check: (newPic: Pictures) => expect(component.pictureMap).toEqual(newPic)
	}
	].forEach(({ description, isMap, suffix, check }) => {

	it(`should ${description}`, () => {
		const file = new File(['dummy content'], 'test.png', { type: 'image/png' });
		const newPic: Pictures = { url: `http://test.com/${isMap ? 'map' : 'new'}.png`, publicId: 'id123' };

		component.savePicture(mockStadium.name, file, isMap);

		const req = httpMock.expectOne(`${apiUrl}/${mockStadium.name}/pictures${suffix}`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body instanceof FormData).toBeTrue();

		req.flush(newPic);

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Picture uploaded successfully');
		expect(component.loading).toBeFalse();
		check(newPic);
	});

	});

	it('should remove picture successfully', () => {
		component.pictureToDelete = mockStadium.pictures[0];
		component.removePicture();

		const req = httpMock.expectOne(
			`${apiUrl}/${mockStadium.name}/pictures?publicId=${component.pictureToDelete.publicId}`
		);
		expect(req.request.method).toBe('DELETE');

		req.flush({ status: 'SUCCESS', message: 'deleted' });

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Picture successfully deleted');
	});

	it('should handle remove picture error', () => {
		component.pictureToDelete = mockStadium.pictures[0];
		component.removePicture();

		const req = httpMock.expectOne(
			`${apiUrl}/${mockStadium.name}/pictures?publicId=${component.pictureToDelete.publicId}`
		);
		req.flush({}, { status: 500, statusText: 'Server Error' });

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occurred while trying to delete the image');
	});
});