import { SelectedTeamService } from './../../../services/selected-team.service';
import { SimplifiedTeam, TeamService } from './../../../services/team.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostListener, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { RemoveConfirmationModalComponent } from '../../remove-confirmation-modal/remove-confirmation-modal.component';
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { Observable, take } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
	selector: 'app-fav-team',
	standalone: true,
	imports: [CommonModule, RemoveConfirmationModalComponent, SuccessModalComponent],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './fav-team.component.html',
})
export class FavTeamComponent implements OnInit {
	public favTeams$!: Observable<SimplifiedTeam[]>;
	public username = '';
	public errorMessage = '';
	public successMessage = '';
	public showDeleteModal = false;
	private teamToDelete!: SimplifiedTeam;
	public showSuccessModal = false;
	public addButtonClicked = false;
	public availableTeams: SimplifiedTeam[] = [];
	public visibleTeams: SimplifiedTeam[] = [];
	private pageSize = 10;
	public currentPage = 0;
	public removeModalIcon = `<svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20" class="w-12 h-12 mx-auto">
		<path fill-rule="evenodd" clip-rule="evenodd"
			d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
		></path>
	</svg>`;

	constructor(
		private userService: UserService,
		private backgroundService: BackgroundColorService,
		private teamService: TeamService,
		private selectedTeamService: SelectedTeamService,
		private authService: AuthService,
		private router: Router
	) {}

	ngOnInit() {
		this.favTeams$ = this.userService.favTeams$;
		this.userService.getFavTeams();
		this.getUsername();
	}

	private getUsername() {
		this.authService.getActiveUser().subscribe({
			next: response => this.username = response.username,
			error: (_) => this.errorMessage = 'Unexpected error while retrieving the user'
		});
	}

	// Add Favorite Team

	private updateAvailableTeams(teams: SimplifiedTeam[]) {
		this.favTeams$.pipe(take(1)).subscribe((favs) => {
			this.availableTeams = teams.filter(team => !favs.some(f => f.name === team.name));
			this.currentPage = 0;
			this.visibleTeams = this.availableTeams.slice(0, this.pageSize);
		});
	}

	public loadTeams() {
		this.addButtonClicked = true;
		this.errorMessage = '';
		this.teamService.getTeamsNamesAndAbbr().subscribe({
			next: (response) => this.updateAvailableTeams(response),
			error: (_) => this.errorMessage = 'Error while loading the teams'
		});
	}

	public addFavoriteTeam(team: SimplifiedTeam) {
		this.errorMessage = '';
		this.successMessage = '';
		this.userService.addFavTeam(team).subscribe({
			next: (_) => {
				this.successMessage = 'Team successfully added';
				this.showSuccessModal = true;
				this.userService.getFavTeams();
				this.teamService.getTeamsNamesAndAbbr().subscribe((response) => {
					this.updateAvailableTeams(response);
				});
			},
			error: (err) => this.errorMessage = err.message
		});
	}

	@HostListener('document:keydown', ['$event'])
	handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.addButtonClicked = false;
		}
	}

	// Remove Favorite Team

	public openDeleteModal(team: SimplifiedTeam) {
		this.showDeleteModal = true;
		this.teamToDelete = team;
	}

	public cancelDeleteModal() {
		this.showDeleteModal = false;
	}

	public confirmDeleteTeam() {
		this.errorMessage = '';
		this.successMessage = '';
		this.userService.removeFavTeam(this.teamToDelete).subscribe({
			next: (_) => {
				this.showDeleteModal = false;
				this.successMessage = 'Team successfully removed';
				this.showSuccessModal = true;
				this.userService.getFavTeams();
				this.teamService.getTeamsNamesAndAbbr().subscribe((response) => {
					this.updateAvailableTeams(response);
				});
			},
			error: (err) => (this.errorMessage = err.message),
		});
	}

	// Background Logo

	public logoBackground(team: SimplifiedTeam) {
		return this.backgroundService.getBackgroundColor(team.abbreviation);
	}

	// Select Team
	public selectTeam(teamName: string) {
		this.teamService.getTeamInfo(teamName).subscribe({
			next: (response) => {
				this.selectedTeamService.setSelectedTeam(response);
				this.router.navigate(['team', teamName]);
			},
			error: (err) => {
				this.errorMessage = err.message;
			},
		});
	}

	// Pagination
	public loadMoreTeams() {
		this.currentPage++;
		const start = this.currentPage * this.pageSize;
		const end = start + this.pageSize;
		this.visibleTeams = [...this.visibleTeams, ...this.availableTeams.slice(start, end)];
	}
}