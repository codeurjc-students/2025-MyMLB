import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TeamInfo } from '../models/team-info.model';

@Injectable({
	providedIn: 'root',
})
export class SelectedTeamService {
	private selectedTeamSubject = new BehaviorSubject<TeamInfo | null>(null);
	public selectedTeam$: Observable<TeamInfo | null> = this.selectedTeamSubject.asObservable();

	public setSelectedTeam(team: TeamInfo): void {
		this.selectedTeamSubject.next(team);
	}

	public clearSelectedTeam(): void {
		this.selectedTeamSubject.next(null);
	}
}