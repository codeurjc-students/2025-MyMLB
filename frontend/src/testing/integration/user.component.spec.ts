import { UserComponent } from '../../app/components/user/user.component';
import { UserService } from '../../app/services/user.service';
import { User } from '../../app/models/user.model';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('UserComponentIntegration', () => {
	let fixture: ComponentFixture<UserComponent>;
	let component: UserComponent;
	let httpMock: HttpTestingController;

	const mockUsers: User[] = [
		{ username: 'user1', email: 'user1@mail.com' },
		{ username: 'user2', email: 'user2@mail.com' },
	];

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [UserComponent],
			providers: [UserService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		fixture = TestBed.createComponent(UserComponent);
		component = fixture.componentInstance;
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should create the component and fetch users', () => {
		fixture.detectChanges(); // Triggers ngOnInit

		const req = httpMock.expectOne('https://localhost:8443/api/users');
		expect(req.request.method).toBe('GET');

		req.flush(mockUsers); // Mock the response from the backend

		fixture.detectChanges(); // Update the DOM

		const compiled = fixture.nativeElement as HTMLElement;
		const listItems = compiled.querySelectorAll('li');

		expect(component.success).toBeTrue();
		expect(component.allUsers.length).toBe(2);
		expect(listItems.length).toBe(2);
		expect(listItems[0].textContent).toContain('user1');
		expect(listItems[1].textContent).toContain('user2');
	});

	it('should show error message on failed request', () => {
		fixture.detectChanges();

		const req = httpMock.expectOne('https://localhost:8443/api/users');
		req.error(new ErrorEvent('Network error'));

		fixture.detectChanges();

		const compiled = fixture.nativeElement as HTMLElement;
		const errorMessage = compiled.querySelector('p');

		expect(component.success).toBeFalse();
		expect(component.allUsers.length).toBe(0);
		expect(errorMessage?.textContent).toContain('Error while loading the users');
	});
});
