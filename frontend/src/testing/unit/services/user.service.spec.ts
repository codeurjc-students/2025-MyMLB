import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from '../../../app/services/user.service';
import { User } from '../../../app/models/user.model';

describe('User Service Tests', () => {
	let service: UserService;
	let httpMock: HttpTestingController;

	const mockUsers: User[] = [
		{ username: 'user1', email: 'user1@mail.com' },
		{ username: 'user2', email: 'user2@mail.com' },
	];

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [UserService, provideHttpClient(withFetch()), provideHttpClientTesting()],
		});

		service = TestBed.inject(UserService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	afterEach(() => {
		httpMock.verify();
	});

	it('should be created', () => {
		expect(service).toBeTruthy();
	});

	it('should fetch all users via GET', () => {
		service.getAllUsers().subscribe((users) => {
			expect(users.length).toBe(2);
			expect(users).toEqual(mockUsers);
		});

		const req = httpMock.expectOne('https://localhost:8443/api/users');
		expect(req.request.method).toBe('GET');
		req.flush(mockUsers);
	});
});
