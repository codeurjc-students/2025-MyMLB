import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MyTicketsComponent } from "../../../../app/components/ticket/my-tickets/my-tickets.component";
import { UserService } from "../../../../app/services/user.service";
import { AuthService } from "../../../../app/services/auth.service";
import { provideHttpClient } from "@angular/common/http";
import { MockFactory } from "../../../utils/mock-factory";
import { of, throwError } from "rxjs";
import { TicketService } from "../../../../app/services/ticket/ticket.service";

describe('My Tickets Component Tests', () => {
    let component: MyTicketsComponent;
    let fixture: ComponentFixture<MyTicketsComponent>;
    let userServiceSpy: jasmine.SpyObj<UserService>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;
	let ticketServiceSpy: jasmine.SpyObj<TicketService>;

    beforeEach(() => {
        userServiceSpy = jasmine.createSpyObj('UserService', ['getPurchasedTickets']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['getActiveUser']);
		ticketServiceSpy = jasmine.createSpyObj('TicketService', ['downloadPdf']);

        authServiceSpy.getActiveUser.and.returnValue(of({ username: 'testuser', roles: ['USER'] }));
        userServiceSpy.getPurchasedTickets.and.returnValue(of([]));

        TestBed.configureTestingModule({
            imports: [MyTicketsComponent],
            providers: [
                provideHttpClient(),
                { provide: UserService, useValue: userServiceSpy },
                { provide: AuthService, useValue: authServiceSpy },
                { provide: TicketService, useValue: ticketServiceSpy },
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

	it('should download the ticket pdf successfully', () => {
		const mockPdf = new Blob(['fakePdf'], { type: 'application/pdf' });
		ticketServiceSpy.downloadPdf.and.returnValue(of(mockPdf));

		const spyCreateUrl = spyOn(window.URL, 'createObjectURL').and.returnValue('blob:mock-url');
		const spyRevokeUrl = spyOn(window.URL, 'revokeObjectURL');

		const mockAnchor = document.createElement('a');
		spyOn(document, 'createElement').and.returnValue(mockAnchor);
		const spyClick = spyOn(mockAnchor, 'click');

		component.downloadPdf(1);

		expect(spyCreateUrl).toHaveBeenCalledWith(mockPdf);
		expect(mockAnchor.download).toBe('MLB_Portal_Ticket.pdf');
		expect(mockAnchor.href).toContain('blob:mock-url');
		expect(spyClick).toHaveBeenCalled();
		expect(spyRevokeUrl).toHaveBeenCalledWith('blob:mock-url');
	});
});