import { AuthService } from './../../services/auth.service';
import { Component, EventEmitter, OnInit, Input, Output, ChangeDetectionStrategy, inject } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DropdownMenuComponent } from "./dropdown-menu/dropdown-menu.component";
import { filter } from 'rxjs';
import { BackgroundColorService } from '../../services/background-color.service';
import { SelectedTeamService } from '../../services/selected-team.service';
import { UserService } from '../../services/user.service';
import { SupportService } from '../../services/support.service';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [RouterModule, CommonModule, DropdownMenuComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
	@Input() isDarkMode: boolean = false;
	@Output() toggleDarkMode: EventEmitter<void> = new EventEmitter();

	private authService = inject(AuthService);
	private userService = inject(UserService);
	private backgroundService = inject(BackgroundColorService);
	private selectTeamService = inject(SelectedTeamService);
	private supportService = inject(SupportService);
	private router = inject(Router);

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
			if (user) {
				this.roles = user.roles;
				this.username = user.username;
			} else {
				this.roles = ['GUEST'];
				this.username = '';
			}
		});

		this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
			this.currentRoute = event.urlAfterRedirects;
			this.isMenuOpen = false;
			if (!this.currentRoute.includes('/team')) {
				this.navBarStyleClass = this.navBarBackgroundColor(undefined);
				this.selectedTeamAbbr = '';
			}
		});

		this.currentRoute = this.router.url;

		this.selectTeamService.selectedTeam$.subscribe((team) => {
			this.selectedTeamAbbr = team?.teamStats.abbreviation;
			this.navBarStyleClass = this.navBarBackgroundColor(team?.teamStats.abbreviation);
		});

		this.userService.profilePicture$.subscribe((url) => this.profilePicture = url);

		this.supportService.opentTickets$.subscribe(ammount => this.currentOpenTickets = ammount);
	}

	ngOnDestroy() {
		this.selectTeamService.clearSelectedTeam();
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
			default:
				this.router.navigate(['coming-soon']);
				break;
		}
	}

	public getProfilePicture() {
		return (this.profilePicture === '') ? 'assets/account-avatar.png' : this.profilePicture;
	}
}