import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from '../../app/components/navbar/navbar.component';
import { AuthService } from '../../app/services/auth.service';
import { ThemeService } from '../../app/services/theme.service';
import { UserRole } from '../../app/models/auth/user-role.model';
import { BackgroundColorService } from '../../app/services/background-color.service';
import { SelectedTeamService } from '../../app/services/selected-team.service';
import { UserService } from '../../app/services/user.service';
import { SupportService } from '../../app/services/support.service';
import { provideRouter } from '@angular/router';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('Navigation Bar Integration Tests', () => {
    let fixture: ComponentFixture<NavbarComponent>;
    let component: NavbarComponent;

    const mockBackgroundService = {
        navBarBackground: (abbr: string | undefined) => abbr ? `bg-${abbr.toLowerCase()}` : 'bg-default',
        navBarItemsHover: () => 'hover-class',
        navBarItemsActive: () => 'active-class',
        toggleButton: () => 'btn-class'
    };

    const selectedTeamSubject = new Subject<any>();
    const mockSelectedTeamService = {
        selectedTeam$: selectedTeamSubject.asObservable(),
        clearSelectedTeam: () => {}
    };

    const mockUserService = { profilePicture$: of('') };
    const mockSupportService = { opentTickets$: of(0) };

    const currentUserSubject = new BehaviorSubject<UserRole>({ username: '', roles: ['GUEST'] });

    const mockAuthService = {
        currentUser$: currentUserSubject.asObservable(),
        getActiveUser: () => of({ username: '', roles: ['GUEST'] }),
        setCurrentUser: (user: UserRole) => currentUserSubject.next(user)
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NavbarComponent],
            providers: [
                { provide: AuthService, useValue: mockAuthService },
                { provide: BackgroundColorService, useValue: mockBackgroundService },
                { provide: SelectedTeamService, useValue: mockSelectedTeamService },
                { provide: UserService, useValue: mockUserService },
                { provide: SupportService, useValue: mockSupportService },
                ThemeService,
                provideRouter([]),
				provideHttpClient(),
            	provideHttpClientTesting(),
            ],
        });

        fixture = TestBed.createComponent(NavbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should update roles and UI when an ADMIN is emitted', () => {
        currentUserSubject.next({ username: 'adminUser', roles: ['ADMIN'] });
        fixture.detectChanges();

        expect(component.username).toEqual('adminUser');
        expect(component.roles).toContain('ADMIN');

        const compiled = fixture.nativeElement as HTMLElement;
        expect(compiled.textContent).toContain('Edit Info');
    });

    it('should update roles and UI when a regular USER is emitted', () => {
        currentUserSubject.next({ username: 'testUser', roles: ['GUEST', 'USER'] });
        fixture.detectChanges();

        expect(component.username).toEqual('testUser');
        expect(component.roles).toContain('USER');
    });

    it('should set GUEST status if user is empty', () => {
        currentUserSubject.next({ username: '', roles: ['GUEST'] });
        fixture.detectChanges();

        expect(component.username).toEqual('');
        expect(component.roles).toEqual(['GUEST']);
    });

    it('should update navBarStyleClass when a team is selected', () => {
        selectedTeamSubject.next({ teamStats: { abbreviation: 'NYY' } });
        fixture.detectChanges();
        expect(component.navBarStyleClass).toBe('bg-default');
    });

    it('should emit toggleDarkMode event when button is clicked', () => {
        spyOn(component.toggleDarkMode, 'emit');
        component.toggleDarkModeButton();
        expect(component.toggleDarkMode.emit).toHaveBeenCalled();
    });
});