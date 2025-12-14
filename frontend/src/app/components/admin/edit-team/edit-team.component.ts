import { Stadium } from '../../../models/stadium.model';
import { CommonModule } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
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
import { PaginatedSelectorService } from '../../../services/utilities/paginated-selector.service';
import { EscapeCloseDirective } from "../../../directives/escape-close.directive";

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
    EscapeCloseDirective
],
	templateUrl: './edit-team.component.html',
})
export class EditTeamComponent
	extends EditEntityComponent<TeamInfo, UpdateTeamRequest>
	implements OnInit
{
	@Input() team!: TeamInfo;
	@Output() backToMenu = new EventEmitter<void>();

	public readonly pageSize = 10;
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
		public selector: PaginatedSelectorService<Stadium>,
		public backgroundService: BackgroundColorService,
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

		if (this.formInputs.stadiumName && this.formInputs.stadiumName !== this.team.stadium.name) {
			req.newStadiumName = this.formInputs.stadiumName;
		}

		if (this.formInputs.newChampionship !== undefined) {
			req.newChampionship = this.formInputs.newChampionship;
		}

		return this.teamService.updateTeam(this.team.teamStats.name, req);
	}

	public showStadiumsModal() {
		this.selectStadiumButtonClicked = true;
		this.selector.reset();
    	this.selector.loadPage(0, this.pageSize, this.stadiumService.getAvailableStadiums.bind(this.stadiumService));
	}

	public loadNextPage() {
		this.selector.loadNextPage(this.pageSize, this.stadiumService.getAvailableStadiums.bind(this.stadiumService));
	}

	public selectStadium(item: unknown) {
		const stadium = item as Stadium;
		this.formInputs.stadiumName = this.selector.select(stadium, (s) => s.name);
		this.success = true;
		this.successMessage = 'Stadium Selected';
	}

	public closeStadiumModal = () => {
		this.isClose = true;
		setTimeout(() => {
			this.selectStadiumButtonClicked = false;
			this.isClose = false;
		}, 300);
	}

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
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