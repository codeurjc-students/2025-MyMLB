import { PlayerService } from './../../../services/player.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostListener, OnInit } from '@angular/core';
import { SuccessModalComponent } from '../../success-modal/success-modal.component';
import { ErrorModalComponent } from '../../modal/error-modal/error-modal.component';
import { SelectElementModalComponent } from '../../modal/select-element-modal/select-element-modal.component';
import { ActionButtonsComponent } from '../action-buttons/action-buttons.component';
import { CreatePlayerRequest } from '../../../models/position-player.model';
import { Team, TeamSummary } from '../../../models/team.model';
import { TeamService } from '../../../services/team.service';
import { Router } from '@angular/router';
import { PaginatedSelectorService } from '../../../services/utilities/paginated-selector.service';

@Component({
	selector: 'app-create-player',
	templateUrl: './create-player.component.html',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Default,
	imports: [
		CommonModule,
		FormsModule,
		SuccessModalComponent,
		ErrorModalComponent,
		SelectElementModalComponent,
		ActionButtonsComponent,
	],
})
export class CreatePlayerComponent implements OnInit {
	public nameInput = '';
	public playerNumberInput: number | undefined = undefined;
	public teamNameInput = '';
	public positionInput = '';

	public success = false;
	public error = false;
	public selectTeamButtonClicked = false;
	public isClose = false;

	public availableTeams: TeamSummary[] = [];

	public errorMessage = '';
	public successMessage = '';

	public readonly pageSize = 10;

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

	private readonly pitcherPositions = ['SP', 'RP', 'CP'] as const;
  	private readonly positionPlayerPositions = ['C', '1B', '2B', '3B', 'SS', 'LF', 'CF', 'RF', 'DH'] as const;

	constructor(private playerService: PlayerService, private teamService: TeamService, public selector: PaginatedSelectorService<TeamSummary>, private router: Router) {}

	private buildRequest(): CreatePlayerRequest {
		return {
			name: this.nameInput,
			playerNumber: this.playerNumberInput,
			teamName: this.teamNameInput,
			position: this.positionInput

		} as CreatePlayerRequest;
	}

	ngOnInit() {}

	private isPitcher(): boolean {
		return this.pitcherPositions.includes(this.positionInput as any);
	}

	private isPositionPlayer(): boolean {
		return this.positionPlayerPositions.includes(this.positionInput as any);
	}

	public createPlayer(): void {
		const request = this.buildRequest();

		if (this.isPitcher()) {
			this.createPlayerByType(() => this.playerService.createPitcher(request));
		}
		else if (this.isPositionPlayer()) {
			this.createPlayerByType(() => this.playerService.createPositionPlayer(request));
		}
		else {
			this.error = true;
			this.errorMessage = 'All the fields are required';
		}
	}

	private createPlayerByType(serviceCall: () => any): void {
		serviceCall().subscribe({
			next: (response: any) => {
				this.success = true;
				this.successMessage = `${response.name}  successfully created`;
			},
			error: (err: any) => {
				this.error = true;
				this.errorMessage = err?.error?.message ?? 'All the fields are required';
			}
		});
	}

	public showTeamsModal() {
		this.selectTeamButtonClicked = true;
		this.selector.reset();
		this.selector.loadPage(0, this.pageSize, this.teamService.getAvailableTeams.bind(this.teamService));
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

	public selectTeam(item: unknown): void {
		const team = item as TeamSummary;
		this.success = true;
		this.successMessage = 'Team Selected';
		this.teamNameInput = this.selector.select(team, (t) => t.name);
	}

	public loadNextPage() {
		this.selector.loadNextPage(this.pageSize, this.teamService.getAvailableTeams.bind(this.teamService));
	}

	public returnToHome() {
		this.router.navigate(['/']);
	}
}