import { TeamService } from './../../../services/team.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { BackgroundColorService } from '../../../services/background-color.service';
import { RemoveConfirmationModalComponent } from '../../remove-confirmation-modal/remove-confirmation-modal.component';
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { Observable, take } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { TeamSummary } from '../../../models/team.model';
import { SelectElementModalComponent } from "../../modal/select-element-modal/select-element-modal.component";
import { EscapeCloseDirective } from "../../../directives/escape-close.directive";

@Component({
	selector: 'app-fav-team',
	standalone: true,
	imports: [CommonModule, RemoveConfirmationModalComponent, SuccessModalComponent, SelectElementModalComponent, EscapeCloseDirective],
	changeDetection: ChangeDetectionStrategy.Default,
	templateUrl: './fav-team.component.html',
})
export class FavTeamComponent implements OnInit {
	public favTeams$!: Observable<TeamSummary[]>;
	public username = '';
	public errorMessage = '';
	public successMessage = '';
	public showDeleteModal = false;
	private teamToDelete!: TeamSummary;
	public showSuccessModal = false;
	public addButtonClicked = false;
	public availableTeams: TeamSummary[] = [];
	public visibleTeams: TeamSummary[] = [];
	private pageSize = 10;
	public currentPage = 0;
	public isClose = false;
	public hasMore!: boolean;

	constructor(
		private userService: UserService,
		public backgroundService: BackgroundColorService,
		private teamService: TeamService,
		private authService: AuthService
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

	private updateAvailableTeams(teams: TeamSummary[]) {
		this.favTeams$.pipe(take(1)).subscribe((favs) => {
			this.availableTeams = teams.filter(team => !favs.some(f => f.name === team.name));
			this.currentPage = 0;
			this.visibleTeams = this.availableTeams.slice(0, this.pageSize);
			this.hasMore = this.visibleTeams.length < this.availableTeams.length;
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

	public addFavoriteTeam(item: unknown) {
		const team = item as TeamSummary;
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

	public closeTeamModal = () => {
		this.isClose = true;
		setTimeout(() => {
			this.addButtonClicked = false;
			this.isClose = false;
		}, 300);
	}

	// Remove Favorite Team

	public openDeleteModal(team: TeamSummary) {
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

	// Select Team
	public selectTeam(teamName: string) {
		this.teamService.selectTeam(teamName).subscribe({
			error: (err) => this.errorMessage = err.message
		});
	}

	// Pagination
	public loadMoreTeams() {
		this.currentPage++;
		const start = this.currentPage * this.pageSize;
		const end = start + this.pageSize;
		this.visibleTeams = [...this.visibleTeams, ...this.availableTeams.slice(start, end)];
		this.hasMore = this.visibleTeams.length < this.availableTeams.length;
	}

	public loadMoreTeamsCondition() {
		this.hasMore = this.visibleTeams.length < this.availableTeams.length;
		return this.hasMore;
	}
}