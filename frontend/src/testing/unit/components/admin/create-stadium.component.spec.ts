import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { CreateStadiumComponent } from '../../../../app/components/admin/create-stadium/create-stadium.component';
import { StadiumService } from '../../../../app/services/stadium.service';
import { CreateStadiumRequest, StadiumSummary } from '../../../../app/models/stadium.model';

describe('Create Stadium Component Tests', () => {
	let component: CreateStadiumComponent;
	let stadiumServiceSpy: jasmine.SpyObj<StadiumService>;
	let routerSpy: jasmine.SpyObj<Router>;

	beforeEach(() => {
		stadiumServiceSpy = jasmine.createSpyObj('StadiumService', ['createStadium']);
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [CreateStadiumComponent],
			providers: [
				{ provide: StadiumService, useValue: stadiumServiceSpy },
				{ provide: Router, useValue: routerSpy },
			],
		});

		const fixture = TestBed.createComponent(CreateStadiumComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should build request correctly', () => {
		component.nameInput = 'Fenway Park';
		component.openingDateInput = 1912;

		const request = (component as any).buildRequest() as CreateStadiumRequest;
		expect(request.name).toBe('Fenway Park');
		expect(request.openingDate).toBe(1912);
	});

	it('should set success flag and message on saveChanges success', () => {
		const mockResponse: StadiumSummary = {
			name: 'Fenway Park',
			openingDate: 1912,
			pictures: [],
		};
		stadiumServiceSpy.createStadium.and.returnValue(of(mockResponse));

		component.nameInput = 'Fenway Park';
		component.openingDateInput = 1912;
		component.saveChanges();

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Fenway Park successfully created');
	});

	it('should set error and message when fields are empty', () => {
		stadiumServiceSpy.createStadium.and.returnValue(throwError(() => new Error('error')));

		component.nameInput = '';
		component.openingDateInput = undefined;
		component.saveChanges();

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('All the fields are required');
	});

	it('should set error and message when stadium already exists', () => {
		stadiumServiceSpy.createStadium.and.returnValue(throwError(() => new Error('error')));

		component.nameInput = 'Yankee Stadium';
		component.openingDateInput = 2009;
		component.saveChanges();

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('A stadium with this name already exists');
	});

	it('should navigate to home on returnToHome', () => {
		component.returnToHome();
		expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
	});

	it('should return current year', () => {
		const year = component.getCurrentYear();
		expect(year).toBe(new Date().getFullYear());
	});
});