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

	const apiUrl = 'https://localhost:8443/api/v1/stadiums';

	const mockStadium: Stadium = {
		name: 'Yankee Stadium',
		openingDate: 2009,
		teamName: 'New York Yankees',
		pictures: [{ url: 'http://test.com/pic.webp', publicId: 'pic1' }],
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

	it('should upload a valid webp picture successfully', () => {
		const file = new File(['dummy'], 'test.webp', { type: 'image/webp' });
		component.uploadPicture(mockStadium.name, file);

		const req = httpMock.expectOne(`${apiUrl}/${mockStadium.name}/pictures`);
		expect(req.request.method).toBe('POST');
		expect(req.request.body instanceof FormData).toBeTrue();

		const newPic: Pictures = { url: 'http://test.com/new.webp', publicId: 'newPic' };
		req.flush(newPic);

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Picture uploaded successfully');
		expect(component.pictures.some((p) => p.publicId === 'newPic')).toBeTrue();
	});

	it('should set error when uploading non-webp picture', () => {
		const file = new File(['dummy'], 'test.png', { type: 'image/png' });
		component.uploadPicture(mockStadium.name, file);

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('Only .webp images are allowed');
	});

	it('should handle upload error from service', () => {
		const file = new File(['dummy'], 'test.webp', { type: 'image/webp' });
		component.uploadPicture(mockStadium.name, file);

		const req = httpMock.expectOne(`${apiUrl}/${mockStadium.name}/pictures`);
		req.flush({}, { status: 500, statusText: 'Server Error' });

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occurred trying to store the picture');
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