import { AuthService } from './../../services/auth.service';
import { Component, EventEmitter, OnInit, Input, Output, ChangeDetectionStrategy, inject, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DropdownMenuComponent } from "./dropdown-menu/dropdown-menu.component";
import { combineLatest, filter, startWith, Subscription } from 'rxjs';
import { BackgroundColorService } from '../../services/background-color.service';
import { SelectedTeamService } from '../../services/selected-team.service';
import { UserService } from '../../services/user.service';
import { SupportService } from '../../services/support.service';
import { PollingService } from '../../services/utilities/polling.service';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule, CommonModule, DropdownMenuComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit, OnDestroy {
	@Input() isDarkMode: boolean = false;
	@Output() toggleDarkMode: EventEmitter<void> = new EventEmitter();

	private authService = inject(AuthService);
	private userService = inject(UserService);
	private backgroundService = inject(BackgroundColorService);
	private selectTeamService = inject(SelectedTeamService);
	private supportService = inject(SupportService);
	private pollingService = inject(PollingService);
	private router = inject(Router);
	private cdr = inject(ChangeDetectorRef);

	private selectedTeamAbbr: string | undefined = '';
	public roles: string[] = ['GUEST'];
	public username: string = '';
	public currentRoute = '';
	public navBarStyleClass = '';
	public profilePicture = '';
	public currentOpenTickets: number = 0;

	public isMenuOpen = false;

	public ngOnInit() {
		this.authService.currentUser$.subscribe(user => {
			this.roles = user?.roles || ['GUEST'];
			this.username = user?.username || '';
			if (this.roles.includes('ADMIN')) {
				this.pollingService.initPolling();
			}
			else {
				this.pollingService.stopPolling();
			}
			this.cdr.detectChanges();
		});

		combineLatest([
			this.router.events.pipe(
				filter(event => event instanceof NavigationEnd),
				startWith(new NavigationEnd(0, this.router.url, this.router.url))
			),
			this.selectTeamService.selectedTeam$
		]).subscribe(([event, team]) => {
			const url = (event as NavigationEnd).urlAfterRedirects;
			this.currentRoute = url;
			this.isMenuOpen = false;

			if (url.includes('/team') && team?.teamStats?.abbreviation) {
				this.selectedTeamAbbr = team.teamStats.abbreviation;
				this.navBarStyleClass = this.navBarBackgroundColor(this.selectedTeamAbbr);
			}
			else {
				this.selectedTeamAbbr = '';
				this.navBarStyleClass = this.navBarBackgroundColor(undefined);
			}
			this.cdr.detectChanges();
		});

		this.userService.profilePicture$.subscribe(url => {
			this.profilePicture = url;
			this.cdr.detectChanges();
		});

		this.supportService.opentTickets$.subscribe(amount => {
			this.currentOpenTickets = amount;
			this.cdr.detectChanges();
		});
	}

	ngOnDestroy() {
		this.selectTeamService.clearSelectedTeam();
		this.pollingService.stopPolling();
	}

	public toggleMenu(): void {
        this.isMenuOpen = !this.isMenuOpen;
    }

	public toggleDarkModeButton(): void {
		this.toggleDarkMode.emit();
	}

	get profileLink(): string {
		return this.roles.includes('USER') || this.roles.includes('ADMIN') ? '/profile' : '/auth';
	}

	public applyStyle(page: string): string {
		return this.currentRoute === page || this.currentRoute.startsWith(page + '/') ? this.activeItem() : '';
	}

	public navBarBackgroundColor(abbreviation: string | undefined) {
		return this.backgroundService.navBarBackground(abbreviation);
	}

	public itemsHover(): string {
		return this.backgroundService.navBarItemsHover(this.selectedTeamAbbr);
	}

	private activeItem(): string {
		return this.backgroundService.navBarItemsActive(this.selectedTeamAbbr);
	}

	public toggleButtonColor(): string {
		return this.backgroundService.toggleButton(this.selectedTeamAbbr);
	}

	public redirect(page: string) {
		this.isMenuOpen = false;
		switch (page) {
			case 'profile':
				this.router.navigate(['profile']);
				break;
			case 'fav-teams':
				this.router.navigate(['favorite-teams']);
				break;
			case 'standings':
				this.router.navigate(['standings']);
				break;
			case 'edit-menu':
				this.router.navigate(['edit-menu']);
				break;
			case 'create-stadium':
				this.router.navigate(['create-stadium']);
				break;
			case 'create-player':
				this.router.navigate(['create-player']);
				break;
			case 'inbox':
				this.router.navigate(['inbox']);
				break;
			case 'tickets':
				this.router.navigate(['my-tickets']);
				break;
			default:
				this.router.navigate(['coming-soon']);
				break;
		}
	}

	public getProfilePicture() {
		return (this.profilePicture === '') ? 'assets/account-avatar.png' : this.profilePicture;
	}
}