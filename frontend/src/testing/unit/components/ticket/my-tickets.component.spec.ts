import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MyTicketsComponent } from "../../../../app/components/ticket/my-tickets/my-tickets.component";
import { UserService } from "../../../../app/services/user.service";
import { AuthService } from "../../../../app/services/auth.service";
import { provideHttpClient } from "@angular/common/http";
import { MockFactory } from "../../../utils/mock-factory";
import { of, throwError } from "rxjs";

describe('My Tickets Component Tests', () => {
    let component: MyTicketsComponent;
    let fixture: ComponentFixture<MyTicketsComponent>;
    let userServiceSpy: jasmine.SpyObj<UserService>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        userServiceSpy = jasmine.createSpyObj('UserService', ['getPurchasedTickets']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['getActiveUser']);

        authServiceSpy.getActiveUser.and.returnValue(of({ username: 'testuser', roles: ['USER'] }));
        userServiceSpy.getPurchasedTickets.and.returnValue(of([]));

        TestBed.configureTestingModule({
            imports: [MyTicketsComponent],
            providers: [
                provideHttpClient(),
                { provide: UserService, useValue: userServiceSpy },
                { provide: AuthService, useValue: authServiceSpy },
            ]
        });

        fixture = TestBed.createComponent(MyTicketsComponent);
        component = fixture.componentInstance;
    });

    it('should initialize username and fetch tickets on ngOnInit', () => {
        const mockTickets = [
            MockFactory.buildTicketMock(1, 'Boston Red Sox', 'New York Yankees', 'Yankee Stadium', 100, new Date(), 'New Sector', 'S-1')
        ];

        authServiceSpy.getActiveUser.and.returnValue(of({ username: 'test', roles: ['ADMIN'] }));
        userServiceSpy.getPurchasedTickets.and.returnValue(of(mockTickets));

        fixture.detectChanges();

        expect(component.username).toBe('test');
        expect(component.tickets).toEqual(mockTickets);
        expect(userServiceSpy.getPurchasedTickets).toHaveBeenCalled();
    });

    it('should handle error when fetching tickets fails', () => {
        userServiceSpy.getPurchasedTickets.and.returnValue(throwError(() => new Error('API Error')));

        fixture.detectChanges();

        expect(component.error).toBeTrue();
        expect(component.errorMessage).toBe('An error occur while loading the tickets');
    });
});