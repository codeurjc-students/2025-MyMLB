import { CommonModule } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
	HostListener,
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
import { Team, TeamSummary } from '../../../models/team.model';
import { SuccessModalComponent } from '../../success-modal/success-modal.component';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { PlayerService } from '../../../services/player.service';
import { RemoveConfirmationModalComponent } from '../../remove-confirmation-modal/remove-confirmation-modal.component';
import { finalize } from 'rxjs';
import { LoadingModalComponent } from "../../modal/loading-modal/loading-modal.component";
import { TeamService } from '../../../services/team.service';

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
    LoadingModalComponent
],
	templateUrl: './edit-player.component.html',
})
export class EditPlayerComponent implements OnInit {
	@Input() player!: PositionPlayerGlobal | PitcherGlobal;
	@Output() backToMenu = new EventEmitter<void>();

	public editPositionPlayerRequest: EditPositionPlayerRequest = {};
	public editPitcherRequest: EditPitcherRequest = {};

	public availableTeams: TeamSummary[] = [];

	public formInputs = {
		number: undefined as number | undefined,
		position: undefined as string | undefined,
		teamName: undefined as string | undefined,

		// Position player
		atBats: undefined as number | undefined,
		walks: undefined as number | undefined,
		hits: undefined as number | undefined,
		doubles: undefined as number | undefined,
		triples: undefined as number | undefined,
		homeRuns: undefined as number | undefined,
		rbis: undefined as number | undefined,

		// Pitcher
		games: undefined as number | undefined,
		wins: undefined as number | undefined,
		losses: undefined as number | undefined,
		inningsPitched: undefined as number | undefined,
		totalStrikeouts: undefined as number | undefined,
		walksP: undefined as number | undefined,
		hitsAllowed: undefined as number | undefined,
		runsAllowed: undefined as number | undefined,
		saves: undefined as number | undefined,
		saveOpportunities: undefined as number | undefined,
	};

	public positionPlayerFieldMap: Record<string, keyof PositionPlayerGlobal> = {
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

	public pitcherFieldMap: Record<string, keyof PitcherGlobal> = {
		number: 'playerNumber',
		position: 'position',
		teamName: 'teamName',
		games: 'games',
		wins: 'wins',
		losses: 'losses',
		inningsPitched: 'inningsPitched',
		totalStrikeouts: 'totalStrikeouts',
		walksP: 'walks',
		hitsAllowed: 'hitsAllowed',
		runsAllowed: 'runsAllowed',
		saves: 'saves',
		saveOpportunities: 'saveOpportunities',
		era: 'era',
		whip: 'whip',
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
		'C', '1B', '2B', '3B', 'SS', 'LF', 'CF', 'RF', 'DH', 'SP', 'CP', 'RP'
	];

	public selectTeamButtonClicked = false;
	public success = false;
	public loading = false;
	public error = false;
	public finish = false;
	public isClose = false;
	public showDeleteConfirmationModal = false;

	public successMessage = '';
	public errorMessage = '';

	public currentPage = 0;
	public readonly pageSize = 10;
	public hasMore = true;

	constructor(private playerService: PlayerService, private teamService: TeamService) {}

	ngOnInit() {
		this.updateDashboard();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['player'] && changes['player'].currentValue) {
			this.updateDashboard();
		}
	}

	public showTeamsModal() {
		this.selectTeamButtonClicked = true;
		this.availableTeams = [];
		this.loadTeams(0);
	}

	public isPositionPlayer(player: any): player is PositionPlayerGlobal {
		return 'ops' in player;
	}

	public isPitcher(player: any): player is PitcherGlobal {
		return 'era' in player;
	}

	private buildEditRequest<T extends object>(
		inputObj: any,
		playerObj: any,
		fieldMap: Record<string, string>
	): T {
		const req: any = {};

		Object.entries(fieldMap).forEach(([inputKey, requestKey]) => {
			const newValue = inputObj[inputKey];
			const oldValue = playerObj[requestKey];

			if (newValue !== undefined && newValue !== oldValue) {
				req[requestKey] = newValue;
			}
		});

		return req as T;
	}

	private preparePositionPlayerRequest() {
		this.editPositionPlayerRequest = this.buildEditRequest<EditPositionPlayerRequest>(
			this.formInputs,
			this.player,
			this.positionPlayerFieldMap
		);
	}

	private preparePitcherRequest() {
		this.editPitcherRequest = this.buildEditRequest<EditPitcherRequest>(
			this.formInputs,
			this.player,
			this.pitcherFieldMap
		);
	}

	private updateDashboard() {
		const fieldMap = this.isPositionPlayer(this.player)
			? this.positionPlayerFieldMap
			: this.pitcherFieldMap;

		Object.entries(fieldMap).forEach(([inputKey, playerKey]) => {
			const newValue = (this.formInputs as any)[inputKey];
			const oldValue = (this.player as any)[playerKey];

			if (newValue !== undefined && newValue !== oldValue) {
				(this.player as any)[playerKey] = newValue;
			}
			(this.formInputs as any)[inputKey] = (this.player as any)[playerKey];
		});
	}

	private updatePositionPlayer() {
		this.resetState();
		this.preparePositionPlayerRequest();
		this.playerService
			.updatePositionPlayer(this.player.name, this.editPositionPlayerRequest)
			.subscribe({
				next: (_) => {
					this.finish = true;
					this.updateDashboard();
				},
				error: (_) => {
					this.error = true;
					this.errorMessage = 'Error';
				},
			});
	}

	private updatePitcher() {
		this.resetState();
		this.preparePitcherRequest();
		this.playerService.updatePitcher(this.player.name, this.editPitcherRequest).subscribe({
			next: (_) => {
				this.finish = true;
				this.updateDashboard();
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Error';
			},
		});
	}

	private resetState() {
		this.error = false;
		this.errorMessage = '';
		this.success = false;
		this.successMessage = '';
		this.loading = false;
	}

	private uploadPicture(stadiumName: string, file: File) {
		this.resetState();
		this.loading = true;

		if (file.type !== 'image/webp') {
			this.error = true;
			this.errorMessage = 'Only .webp images are allowed';
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
				error: (_) => {
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

	public confirm() {
		if (this.isPositionPlayer(this.player)) {
			this.updatePositionPlayer();
		} else {
			this.updatePitcher();
		}
	}

	public deletePlayer() {
		this.resetState();
		this.playerService.deletePlayer(this.player.name).subscribe({
			next: (_) => {
				this.success = true;
				this.successMessage = this.player.name + ' successfully deleted';
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'An error occurr during the deletion process';
			},
		});
	}

	public loadTeams(page: number) {
		this.teamService.getAvailableTeams(page, this.pageSize).subscribe({
			next: (response) => {
				this.availableTeams = [...this.availableTeams, ...response.content];
				this.currentPage = response.page.number;
				this.hasMore = response.page.totalPages > this.currentPage + 1;
			},
			error: (_) => {
				this.error = true;
				this.errorMessage = 'Error trying to load the teams';
			}
		});
	}

	public loadNextPage() {
		if (this.hasMore) {
			this.loadTeams(this.currentPage + 1);
		}
	}

	public selectTeam(item: unknown) {
		this.resetState();
		const team = item as Team;
		this.success = true;
		this.successMessage = 'Team Selected';
		this.formInputs.teamName = team.name;
		this.availableTeams = this.availableTeams.filter((t) => t.name !== team.name);
	}

	public closeTeamModal() {
		this.isClose = true;
		setTimeout(() => {
			this.selectTeamButtonClicked = false;
			this.isClose = false;
		}, 300);
	}

	@HostListener('document:keydown', ['$event'])
	public handleEscape(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.closeTeamModal();
		}
	}

	public goToEditMenu() {
		this.finish = false;
		this.backToMenu.emit();
	}
}