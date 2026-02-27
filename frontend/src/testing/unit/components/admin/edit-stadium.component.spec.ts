import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EditStadiumComponent } from '../../../../app/components/admin/edit-stadium/edit-stadium.component';
import { StadiumService } from '../../../../app/services/stadium.service';
import { MockFactory } from '../../../../testing/utils/mock-factory';
import { Stadium } from '../../../../app/models/stadium.model';
import { Pictures } from '../../../../app/models/pictures.model';
import { AuthResponse } from '../../../../app/models/auth.model';

describe('Edit Stadium Component Tests', () => {
	let component: EditStadiumComponent;
	let fixture: ComponentFixture<EditStadiumComponent>;
	let stadiumServiceSpy: jasmine.SpyObj<StadiumService>;

	const mockPicture: Pictures = { url: 'http://test.com/img.webp', publicId: '123' };
	const mockPictureMap: Pictures = {
		url: 'https://test_pic/123',
		publicId: '123'
	};
	const mockStadium: Stadium = MockFactory.buildStadiumCompleteMock(
		'Yankee Stadium',
		2009,
		'New York Yankees',
		[mockPicture],
		mockPictureMap
	);

	beforeEach(() => {
		stadiumServiceSpy = jasmine.createSpyObj('StadiumService', [
			'uploadPicture',
			'editStadiumMapPicture',
			'removePicture',
		]);

		TestBed.configureTestingModule({
			imports: [EditStadiumComponent],
			providers: [{ provide: StadiumService, useValue: stadiumServiceSpy }],
		}).compileComponents();

		fixture = TestBed.createComponent(EditStadiumComponent);
		component = fixture.componentInstance;
		component.stadium = mockStadium;
		fixture.detectChanges();
	});

	it('should verifyPictures return true if less than 5', () => {
		expect(component.verifyPictures()).toBeTrue();
	});

	it('should set error when uploading a picture over 1MB', () => {
		const invalidPicture = 'a'.repeat(1024 * 1024 + 1);
		const file = new File([invalidPicture], 'test.png', { type: 'image/png' });
		const event = { target: { files: [file] } } as unknown as Event;

		component.handleFileUpload(event, false);

    	expect(component.error).toBeTrue();
    	expect(component.errorMessage).toBe('The picture must be less than 1MB');
	});

	// Parametrized Tests for picture uploading
	[
		{
			description: 'upload a picture',
			isMap: false,
			spyMethod: () => stadiumServiceSpy.uploadPicture,
			verify: () => expect(component.pictures).toContain(mockPicture)
		},
		{
			description: 'update the stadium map picture',
			isMap: true,
			spyMethod: () => stadiumServiceSpy.editStadiumMapPicture,
			verify: () => expect(component.pictureMap.url).toBe('http://test.com/newMap.png')
		}
	].forEach(({ description, isMap, spyMethod, verify }) => {
		it(`should ${description} successfully`, () => {
			const file = new File(['dummy'], 'test.png', { type: 'image/png' });
			const mockResponse = isMap
				? { url: 'http://test.com/newMap.png', publicId: '123' }
				: mockPicture;

			spyMethod().and.returnValue(of(mockResponse));

			component.savePicture(mockStadium.name, file, isMap);

			expect(component.success).toBeTrue();
			expect(component.successMessage).toBe('Picture uploaded successfully');
			expect(component.loading).toBeFalse();
			verify();
		});
	});

	it('should handle upload picture error', () => {
		const file = new File(['dummy'], 'test.webp', { type: 'image/webp' });
		stadiumServiceSpy.uploadPicture.and.returnValue(throwError(() => new Error('fail')));

		component.savePicture(mockStadium.name, file, false);

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('An error occurred trying to store the picture');
	});

	it('should open confirmation modal when handleRemovePicture is called', () => {
		component.handleRemovePicture(mockPicture);
		expect(component.openConfirmationModal).toBeTrue();
		expect(component.pictureToDelete).toBe(mockPicture);
	});

	it('should cancel confirmation modal', () => {
		component.openConfirmationModal = true;
		component.cancelConfirmationModal();
		expect(component.openConfirmationModal).toBeFalse();
	});

	it('should remove picture successfully', () => {
		const mockResponse: AuthResponse = {
			status: 'SUCCESS',
			message: 'Picture successfully deleted'
		};
		stadiumServiceSpy.removePicture.and.returnValue(of(mockResponse));

		component.pictureToDelete = mockPicture;
		component.removePicture();

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Picture successfully deleted');
		expect(component.pictures.length).toBe(0);
		expect(component.openConfirmationModal).toBeFalse();
	});

	it('should emit backToMenu when goToEditMenu is called', () => {
		spyOn(component.backToMenu, 'emit');
		component.goToEditMenu();
		expect(component.finish).toBeFalse();
		expect(component.backToMenu.emit).toHaveBeenCalled();
	});

	it('should set finish true when confirm is called', () => {
		component.confirm();
		expect(component.finish).toBeTrue();
	});
});