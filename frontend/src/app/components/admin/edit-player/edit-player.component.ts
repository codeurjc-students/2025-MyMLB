import { CommonModule } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
	Input,
	OnInit,
	Output,
	SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SelectElementModalComponent } from '../../modal/select-element-modal/select-element-modal.component';
import { ActionButtonsComponent } from '../action-buttons/action-buttons.component';
import {
	EditPositionPlayerRequest,
	PositionPlayerGlobal,
} from '../../../models/position-player.model';
import { EditPitcherRequest, PitcherGlobal } from '../../../models/pitcher.model';
import { TeamSummary } from '../../../models/team.model';
import { SuccessModalComponent } from '../../success-modal/success-modal.component';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { PlayerService } from '../../../services/player.service';
import { RemoveConfirmationModalComponent } from '../../remove-confirmation-modal/remove-confirmation-modal.component';
import { finalize } from 'rxjs';
import { LoadingModalComponent } from '../../modal/loading-modal/loading-modal.component';
import { TeamService } from '../../../services/team.service';
import { EntityFormMapperService } from '../../../services/utilities/entity-form-mapper.service';
import { EditEntityComponent } from '../../../models/utilities/edit-entity-component.model';
import { PaginatedSelectorService } from '../../../services/utilities/paginated-selector.service';
import { ValidationService } from '../../../services/utilities/validation.service';
import { EscapeCloseDirective } from "../../../directives/escape-close.directive";

@Component({
	selector: 'app-edit-player',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
    CommonModule,
    FormsModule,
    SelectElementModalComponent,
    ActionButtonsComponent,
    SuccessModalComponent,
    ErrorModalComponent,
    RemoveConfirmationModalComponent,
    LoadingModalComponent,
    EscapeCloseDirective
],
	templateUrl: './edit-player.component.html',
})
export class EditPlayerComponent extends EditEntityComponent<PositionPlayerGlobal | PitcherGlobal,EditPositionPlayerRequest | EditPitcherRequest> implements OnInit {
	@Input() player!: PositionPlayerGlobal | PitcherGlobal;
	@Output() backToMenu = new EventEmitter<void>();

	public selectTeamButtonClicked = false;
	public isClose = false;

	public readonly pageSize = 10;

	public positionPlayerFieldMap: Record<string, string> = {
		number: 'playerNumber',
		position: 'position',
		teamName: 'teamName',
		atBats: 'atBats',
		walks: 'walks',
		hits: 'hits',
		doubles: 'doubles',
		triples: 'triples',
		homeRuns: 'homeRuns',
		rbis: 'rbis',
		average: 'average',
		obp: 'obp',
		ops: 'ops',
		slugging: 'slugging',
	};

	public pitcherFieldMap: Record<string, string> = {
		number: 'playerNumber',
		position: 'position',
		teamName: 'teamName',
		games: 'games',
		wins: 'wins',
		losses: 'losses',
		era: 'era',
		inningsPitched: 'inningsPitched',
		totalStrikeouts: 'totalStrikeouts',
		walks: 'walks',
		hitsAllowed: 'hitsAllowed',
		runsAllowed: 'runsAllowed',
		whip: 'whip',
		saves: 'saves',
		saveOpportunities: 'saveOpportunities'
	};

	public positionPlayerEditableFields = [
		{ key: 'atBats', label: 'At Bats' },
		{ key: 'walks', label: 'Walks' },
		{ key: 'hits', label: 'Hits' },
		{ key: 'doubles', label: 'Doubles' },
		{ key: 'triples', label: 'Triples' },
		{ key: 'homeRuns', label: 'Home Runs' },
		{ key: 'rbis', label: 'RBIs' },
	] as const;

	public positionPlayerReadonlyFields = [
		{ key: 'average', label: 'AVG' },
		{ key: 'obp', label: 'OBP' },
		{ key: 'ops', label: 'OPS' },
		{ key: 'slugging', label: 'SLG' },
	] as const;

	public pitcherEditableFields = [
		{ key: 'games', label: 'Games' },
		{ key: 'wins', label: 'Wins' },
		{ key: 'losses', label: 'Losses' },
		{ key: 'inningsPitched', label: 'Innings Pitched' },
		{ key: 'totalStrikeouts', label: 'Strikeouts' },
		{ key: 'walks', label: 'Walks' },
		{ key: 'hitsAllowed', label: 'Hits Allowed' },
		{ key: 'runsAllowed', label: 'Runs Allowed' },
		{ key: 'saves', label: 'Saves' },
		{ key: 'saveOpportunities', label: 'Save Opportunities' },
	] as const;

	public pitcherReadonlyFields = [
		{ key: 'era', label: 'ERA' },
		{ key: 'whip', label: 'WHIP' },
	] as const;

	public availablePositions = [
		'C',
		'1B',
		'2B',
		'3B',
		'SS',
		'LF',
		'CF',
		'RF',
		'DH',
		'SP',
		'CP',
		'RP',
	];

	public showDeleteConfirmationModal = false;
	public isPositionInputOpen = false;

	constructor(
		mapper: EntityFormMapperService,
		private playerService: PlayerService,
		private teamService: TeamService,
		public selector: PaginatedSelectorService<TeamSummary>,
		public validationService: ValidationService
	) {
		super(mapper);
	}

	ngOnInit() {
		this.hydrateForm();
		this.selector.setLabelFn((team) => team.name);
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['player'] && changes['player'].currentValue) {
			this.hydrateForm();
		}
	}

	protected getFieldMap(): Record<string, string> {
		return this.validationService.isPositionPlayer(this.player)
			? this.positionPlayerFieldMap
			: this.pitcherFieldMap;
	}

	protected getEntity() {
		return this.player;
	}

	protected updateEntityService(request: any) {
		return this.validationService.isPositionPlayer(this.player)
			? this.playerService.updatePositionPlayer(this.player.name, request)
			: this.playerService.updatePitcher(this.player.name, request);
	}

	public showTeamsModal() {
		this.selectTeamButtonClicked = true;
		this.selector.reset();
    	this.selector.loadPage(0, this.pageSize, this.teamService.getAvailableTeams.bind(this.teamService));
	}

	public loadNextPage() {
		this.selector.loadNextPage(this.pageSize, this.teamService.getAvailableTeams.bind(this.teamService));
	}

	public selectTeam(item: unknown) {
		const team = item as TeamSummary;
		this.resetState();
		this.success = true;
		this.successMessage = 'Team Selected';
		this.formInputs.teamName = this.selector.select(team, (t) => t.name);
	}

	public closeTeamModal = () => {
		this.isClose = true;
		setTimeout(() => {
			this.selectTeamButtonClicked = false;
			this.isClose = false;
		}, 300);
	}

	public uploadPicture(stadiumName: string, file: File) {
		this.resetState();
		this.loading = true;

		if (file.type !== 'image/webp') {
			this.error = true;
			this.errorMessage = 'Only .webp images are allowed';
			this.loading = false;
			return;
		}

		this.playerService
			.updatePicture(stadiumName, file)
			.pipe(finalize(() => (this.loading = false)))
			.subscribe({
				next: (savedPic) => {
					this.success = true;
					this.successMessage = 'Picture uploaded successfully';
					this.player.picture = savedPic;
				},
				error: () => {
					this.error = true;
					this.errorMessage = 'An error occurred trying to store the picture';
				},
			});
	}

	public handleFileUpload(event: Event): void {
		const input = event.target as HTMLInputElement;
		if (input.files && input.files.length > 0) {
			const file = input.files[0];

			if (file.type !== 'image/webp') {
				this.error = true;
				this.errorMessage = 'Only .webp images are allowed';
				this.loading = false;
				return;
			}
			this.uploadPicture(this.player.name, file);
		}
	}

	public deletePlayer() {
		this.resetState();
		this.playerService.deletePlayer(this.player.name).subscribe({
			next: () => {
				this.success = true;
				this.successMessage = this.player.name + ' successfully deleted';
			},
			error: () => {
				this.error = true;
				this.errorMessage = 'An error occurred during the deletion process';
			}
		});
	}

	public togglePositionInput(state: boolean) {
		this.isPositionInputOpen = state;
	}

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
	}

	public calculateTemporaryStats() {
		if (this.validationService.isPositionPlayer(this.player)) {
			this.calculatePositionPlayerNonEditableStats();
		}
		else {
			this.calculatePitcherNonEditableStats();
		}
	}

	private decimalFormatted(value: number, decimals: 1000 | 100) {
		return Math.floor(value * decimals) / decimals;
	}

	private inningsFormatted(innings: number) {
		const aux = Math.floor(innings);
		const decimals = parseFloat((innings - aux).toFixed(1));

		if (Math.abs(decimals - 0.1) < 0.01) {
			return aux + (1.0 / 3.0);
		}
		else if (Math.abs(decimals - 0.2) < 0.01) {
			return aux + (2.0 / 3.0)
		}
		return innings;
	}

	private calculatePositionPlayerNonEditableStats() {
		const { atBats = 0, hits = 0, walks = 0, doubles = 0, triples = 0, homeRuns = 0 } = this.formInputs;

		if (atBats === 0) {
			this.formInputs.average = 0.0;
			this.formInputs.obp = 0.0;
			this.formInputs.slugging = 0.0;
			this.formInputs.ops = 0.0;
			return;
		}

		this.formInputs.average = this.decimalFormatted((hits / atBats), 1000);
		const totalAbs = Number(atBats) + Number(walks);
		const obpValue = (Number(hits) + Number(walks)) / totalAbs;
		this.formInputs.obp = totalAbs === 0 ? 0.0 : this.decimalFormatted(obpValue, 1000);

		const totalBases = Number(hits) + (2 * Number(doubles)) + (3 * Number(triples)) + (4 * Number(homeRuns));
		this.formInputs.slugging = this.decimalFormatted((totalBases / atBats), 1000);

		const opsValue = Number(this.formInputs.obp) + Number(this.formInputs.slugging);
		this.formInputs.ops = this.decimalFormatted(opsValue, 1000);
	}

	private calculatePitcherNonEditableStats() {
		const { inningsPitched = 0, runsAllowed = 0, hitsAllowed = 0, walks = 0 } = this.formInputs;
		const innings = this.inningsFormatted(Number(inningsPitched));

		if (innings === 0) {
			this.formInputs.era = 0.0;
			this.formInputs.whip = 0.0;
			return;
		}

		const eraValue = (Number(runsAllowed) * 9) / innings;
		this.formInputs.era = this.decimalFormatted(eraValue, 100);

		const whipValue = (Number(hitsAllowed) + Number(walks)) / innings;
		this.formInputs.whip = this.decimalFormatted(whipValue, 100);
	}
}