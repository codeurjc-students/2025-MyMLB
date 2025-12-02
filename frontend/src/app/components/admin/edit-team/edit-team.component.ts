import { Stadium } from '../../../models/stadium.model';
import { CommonModule } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
	HostListener,
	Input,
	OnInit,
	Output,
} from '@angular/core';
import { TeamInfo, UpdateTeamRequest } from '../../../models/team.model';
import { TeamService } from '../../../services/team.service';
import { SuccessModalComponent } from '../../success-modal/success-modal.component';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { FormsModule } from '@angular/forms';
import { BackgroundColorService } from '../../../services/background-color.service';
import { ActionButtonsComponent } from '../action-buttons/action-buttons.component';
import { StadiumService } from '../../../services/stadium.service';
import { SelectElementModalComponent } from '../../modal/select-element-modal/select-element-modal.component';
import { EntityFormMapperService } from '../../../services/utilities/entity-form-mapper.service';
import { EditEntityComponent } from '../../../models/utilities/edit-entity-component.model';

@Component({
	selector: 'app-edit-team',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		FormsModule,
		SuccessModalComponent,
		ErrorModalComponent,
		ActionButtonsComponent,
		SelectElementModalComponent,
	],
	templateUrl: './edit-team.component.html',
})
export class EditTeamComponent
	extends EditEntityComponent<TeamInfo, UpdateTeamRequest>
	implements OnInit
{
	@Input() team!: TeamInfo;
	@Output() backToMenu = new EventEmitter<void>();

	public availableStadiums: Stadium[] = [];
	public currentPage = 0;
	public readonly pageSize = 10;
	public hasMore = true;
	public selectStadiumButtonClicked = false;
	public isClose = false;

	public override formInputs: any = {
		city: undefined as string | undefined,
		info: undefined as string | undefined,
		newChampionship: undefined as number | undefined,
		stadiumName: undefined as string | undefined
	};

	constructor(
		mapper: EntityFormMapperService,
		private teamService: TeamService,
		private stadiumService: StadiumService,
		private backgroundService: BackgroundColorService,
	) {
		super(mapper);
	}

	ngOnInit(): void {
		this.hydrateForm();
	}

	protected getFieldMap(): Record<string, string> {
		return {
			city: 'city',
			info: 'generalInfo',
			stadiumName: 'stadium.name'
		};
	}

	protected getEntity() {
		return this.team;
	}

	protected updateEntityService(request: UpdateTeamRequest) {
		const req: UpdateTeamRequest = { ...request };
		if (this.formInputs.newChampionship !== undefined) {
			req.newChampionship = this.formInputs.newChampionship;
		}
		return this.teamService.updateTeam(this.team.teamStats.name, req);
	}

	public showStadiumsModal() {
		this.selectStadiumButtonClicked = true;
		this.availableStadiums = [];
		this.loadMoreStadiums(0);
	}

	private loadMoreStadiums(page: number) {
		this.stadiumService.getAvailableStadiums(page, this.pageSize).subscribe({
			next: (response) => {
				this.availableStadiums = [...this.availableStadiums, ...response.content];
				this.currentPage = response.page.number;
				this.hasMore = response.page.totalPages > this.currentPage + 1;
			},
			error: () => (this.errorMessage = 'Error trying to show the stadiums'),
		});
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.loadMoreStadiums(this.currentPage + 1);
		}
	}

	public selectStadium(item: unknown) {
		const stadium = item as Stadium;
		this.formInputs.stadiumName = stadium.name;
		this.success = true;
		this.successMessage = 'Stadium Selected';
		this.availableStadiums = this.availableStadiums.filter(
			(stad) => stad.name !== stadium.name
		);
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

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
	}

	public getBackgroundColor(abbreviation: string) {
		return this.backgroundService.getBackgroundColor(abbreviation);
	}

	public override updateDashboard() {
		if (this.formInputs.city && this.formInputs.city !== this.team.city) {
			const prevCity = this.team.city;
			this.team.city = this.formInputs.city;
			this.team.teamStats.name = this.team.teamStats.name.replace(prevCity, this.team.city);
		}

		super.updateDashboard();

		if (this.formInputs.newChampionship !== undefined) {
			this.team.championships.push(this.formInputs.newChampionship);
			this.formInputs.newChampionship = undefined;
		}
	}
}