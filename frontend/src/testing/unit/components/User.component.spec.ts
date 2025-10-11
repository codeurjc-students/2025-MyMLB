import { User } from './../../../app/Models/user.model';
import { UserService } from '../../../app/services/user.service';
import { UserComponent } from '../../../app/components/user/user.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

describe('User Component Tests', () => {
	let fixture: ComponentFixture<UserComponent>;
	let component: UserComponent;
	let mockUserService: jasmine.SpyObj<UserService>;

	const mockUsers: User[] = [
		{ username: 'user1', email: 'user1@mail.com' },
		{ username: 'user2', email: 'user2@mail.com' },
	];

	beforeEach(() => {
		mockUserService = jasmine.createSpyObj('UserService', ['getAllUsers']);

		TestBed.configureTestingModule({
			imports: [UserComponent],
			providers: [{ provide: UserService, useValue: mockUserService }],
		});

		fixture = TestBed.createComponent(UserComponent);
		component = fixture.componentInstance;
	});

	it('should create the component', () => {
		expect(component).toBeTruthy();
	});

	it('should load users successfully and render them', () => {
		mockUserService.getAllUsers.and.returnValue(of(mockUsers));

		fixture.detectChanges(); // triggers ngOnInit

		const compiled = fixture.nativeElement as HTMLElement;
		const listItems = compiled.querySelectorAll('li');

		expect(component.success).toBeTrue();
		expect(component.allUsers.length).toBe(2);
		expect(listItems.length).toBe(2);
		expect(listItems[0].textContent).toContain('user1');
		expect(listItems[1].textContent).toContain('user2');
	});

	it('should handle error and show error message', () => {
		mockUserService.getAllUsers.and.returnValue(throwError(() => new Error('Network error')));

		fixture.detectChanges();

		const compiled = fixture.nativeElement as HTMLElement;
		const errorMessage = compiled.querySelector('p');

		expect(component.success).toBeFalse();
		expect(component.allUsers.length).toBe(0);
		expect(errorMessage?.textContent).toContain('Error while loading the users');
	});
});