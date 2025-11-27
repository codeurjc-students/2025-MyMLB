import { Stadium } from './../../../models/stadium.model';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { TeamInfo, UpdateTeamRequest } from '../../../models/team.model';
import { TeamService } from '../../../services/team.service';
import { SuccessModalComponent } from "../../success-modal/success-modal.component";
import { ErrorModalComponent } from "../../modal/error-modal/error-modal.component";
import { FormsModule } from '@angular/forms';
import { BackgroundColorService } from '../../../services/background-color.service';
import { ActionButtonsComponent } from "../action-buttons/action-buttons.component";
import { StadiumService } from '../../../services/stadium.service';
import { SelectElementModalComponent } from "../../modal/select-element-modal/select-element-modal.component";

@Component({
	selector: 'app-edit-team',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [CommonModule, FormsModule, SuccessModalComponent, ErrorModalComponent, ActionButtonsComponent, SelectElementModalComponent],
	templateUrl: './edit-team.component.html'
})
export class EditTeamComponent implements OnInit {
	@Input() team!: TeamInfo;
	@Output() backToMenu = new EventEmitter<void>();

	public request: UpdateTeamRequest = {};
	public availableStadiums: Stadium[] = [];

	public cityInput = '';
	public infoInput = '';
	public stadiumInput = '';
	public newChampionshipInput: number | undefined = undefined;

	public error = false;
	public success = false;
	public finish = false;

	public errorMessage = '';
	public successMessage = '';

	public currentPage = 0;
	public readonly pageSize = 10;
	public hasMore = true;

	public selectStadiumButtonClicked = false;
	public isClose = false;

	constructor(private teamService: TeamService, private stadiumService: StadiumService, private backgroundService: BackgroundColorService) {}

	ngOnInit(): void {
		this.stadiumInput = this.team.stadium.name;
	}

	public showStadiumsModal() {
		this.selectStadiumButtonClicked = true;
		this.availableStadiums = [];
		this.loadMoreStadiums(0);
	}

	public loadMoreStadiums(page: number) {
		this.stadiumService.getAvailableStadiums(page, this.pageSize).subscribe({
			next: (response) => {
				this.availableStadiums = [...this.availableStadiums, ...response.content];
				this.currentPage = response.page.number;
				this.hasMore = response.page.totalPages > this.currentPage + 1;

			},
			error: (_) => this.errorMessage = 'Error trying to show the stadiums'
		});
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.loadMoreStadiums(this.currentPage + 1);
		}
	}

	public selectStadium(item: unknown) {
		const stadium = item as Stadium;
		this.stadiumInput = stadium.name;
		this.success = true;
		this.successMessage = 'Stadium Selected';
		this.availableStadiums = this.availableStadiums.filter(stad => stad.name !== stadium.name);
	}

	public closeStadiumModal() {
		this.isClose = true;
		setTimeout(() => {
			this.selectStadiumButtonClicked = false;
			this.isClose = false;
		}, 300);
	}

	@HostListener('document:keydown', ['$event'])
	public handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.closeStadiumModal();
		}
	}

	private prepareRequest() {
		this.request = {};

		if (this.cityInput && this.cityInput !== this.team.city) {
			this.request.city = this.cityInput;
		}
		if (this.infoInput && this.infoInput !== this.team.generalInfo) {
			this.request.newInfo = this.infoInput;
		}
		if (this.newChampionshipInput) {
			this.request.newChampionship = this.newChampionshipInput;
		}
		if (this.stadiumInput && this.stadiumInput !== this.team.stadium.name) {
			this.request.newStadiumName = this.stadiumInput;
		}
	}

	private updateDasboard() {
		if (this.cityInput && this.cityInput !== this.team.city) {
			const aux = this.team.city;
			this.team.city = this.cityInput;
			this.team.teamStats.name = this.team.teamStats.name.replace(aux, this.cityInput);
		}
		if (this.infoInput && this.infoInput !== this.team.generalInfo) {
			this.team.generalInfo = this.infoInput;
		}
		if (this.newChampionshipInput) {
			this.team.championships.push(this.newChampionshipInput!);
		}
		if (this.stadiumInput && this.stadiumInput !== this.team.stadium.name) {
			this.team.stadium.name = this.stadiumInput;
		}
	}

	public confirm() {
		this.prepareRequest();
		this.teamService.updateTeam(this.team.teamStats.name, this.request).subscribe({
			next: (_) => {
				this.finish = true;
				this.updateDasboard();
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Invalid Stadium. The stadium must exists and not have any team assigned';
			}
		});
	}

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
	}

	public getBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}
}