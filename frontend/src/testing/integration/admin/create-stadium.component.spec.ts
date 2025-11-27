import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { Router } from '@angular/router';
import { CreateStadiumComponent } from '../../../app/components/admin/create-stadium/create-stadium.component';
import { StadiumService } from '../../../app/services/stadium.service';
import { StadiumSummary } from '../../../app/models/stadium.model';

describe('Create Stadium Component Integration Tests', () => {
	let fixture: ComponentFixture<CreateStadiumComponent>;
	let component: CreateStadiumComponent;
	let httpMock: HttpTestingController;
	let routerSpy: jasmine.SpyObj<Router>;

	const apiUrl = 'https://localhost:8443/api/v1/stadiums';

	beforeEach(() => {
		routerSpy = jasmine.createSpyObj('Router', ['navigate']);

		TestBed.configureTestingModule({
			imports: [CreateStadiumComponent],
			providers: [
				StadiumService,
				provideHttpClient(withFetch()),
				provideHttpClientTesting(),
				{ provide: Router, useValue: routerSpy },
			],
		});

		fixture = TestBed.createComponent(CreateStadiumComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
		fixture.detectChanges();
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should send POST request and set success on valid data', () => {
		component.nameInput = 'Fenway Park';
		component.openingDateInput = 1912;

		component.saveChanges();

		const req = httpMock.expectOne(apiUrl);
		expect(req.request.method).toBe('POST');
		expect(req.request.body).toEqual({ name: 'Fenway Park', openingDate: 1912 });

		const mockResponse: StadiumSummary = {
			name: 'Fenway Park',
			openingDate: 1912,
			pictures: [],
		};
		req.flush(mockResponse);

		expect(component.success).toBeTrue();
		expect(component.successMessage).toBe('Fenway Park successfully created');
	});

	it('should set error when backend returns 400 and fields are empty', () => {
		component.nameInput = '';
		component.openingDateInput = undefined;

		component.saveChanges();

		const req = httpMock.expectOne(apiUrl);
		req.flush({ message: 'error' }, { status: 400, statusText: 'Bad Request' });

		expect(component.error).toBeTrue();
		expect(component.errorMessage).toBe('All the fields are required');
	});

	it('should set error when backend returns 400 and stadium exists', () => {
		component.nameInput = 'Yankee Stadium';
		component.openingDateInput = 2009;

		component.saveChanges();

		const req = httpMock.expectOne(apiUrl);
		req.flush({ message: 'duplicate' }, { status: 400, statusText: 'Bad Request' });

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