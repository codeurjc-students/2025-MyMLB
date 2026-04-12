import { PaginatedResponse } from './../models/pagination.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, shareReplay, tap } from 'rxjs';
import { Team, TeamSummary, TeamInfo, UpdateTeamRequest, WinsPerRival, RunStats, WinsDistribution, HistoricRanking } from '../models/team.model';
import { SelectedTeamService } from './selected-team.service';
import { Router } from '@angular/router';
import { AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';

export type StandingsResponse = {
	[league: string]: {
		[division: string]: Team[];
	};
};

@Injectable({
	providedIn: 'root',
})
export class TeamService {
	private url = `${environment.apiUrl}/teams`;
	private http = inject(HttpClient);
	private selectedTeamService = inject(SelectedTeamService);
	private standingsCache$?: Observable<StandingsResponse>; // Used to only make 1 call to /standings, in getTeamNamesAndAbbr and getTeamDivisionRank
	private router = inject(Router);

	public getAvailableTeams(page: number, size: number): Observable<PaginatedResponse<TeamSummary>> {
		return this.http.get<PaginatedResponse<TeamSummary>>(`${this.url}/available?page=${page}&size=${size}`);
	}

	public getStandings(): Observable<StandingsResponse> {
		if (!this.standingsCache$) {
			this.standingsCache$ = this.http.get<StandingsResponse>(`${this.url}/standings`).pipe(
				shareReplay(1) // Store the last value and share it with the other methods
			);
		}
		return this.standingsCache$;
	}

	public cleanStandingsCache() {
		this.standingsCache$ = undefined;
	}

	public isGoodPct(pct : string) {
		const parseredPct = Number(pct);
		return parseredPct >= 0.5;
	}

	public getTeamInfo(teamName: string): Observable<TeamInfo> {
		return this.http.get<TeamInfo>(`${this.url}/${teamName}`);
	}

	public selectTeam(teamName: string): Observable<TeamInfo> {
		return this.getTeamInfo(teamName).pipe(
			tap((response) => this.selectedTeamService.setSelectedTeam(response)),
			tap(() => this.router.navigate(['team', teamName]))
		);
	}

	public getTeamsNamesAndAbbr(): Observable<TeamSummary[]> {
		return this.getStandings().pipe(
			map((data) => {
				const result: TeamSummary[] = [];
				for (const league of Object.keys(data)) {
					const divisions = data[league];
					for (const division of Object.keys(divisions)) {
						const teams = divisions[division];
						for (const team of teams) {
							result.push({
								name: team.name,
								abbreviation: team.abbreviation,
								league,
								division,
							});
						}
					}
				}
				result.sort((a, b) => a.name.localeCompare(b.name));
				return result;
			})
		);
	}

	public getTeamDivisionRank(teamAbbr: string): Observable<number> {
		return this.getStandings().pipe(
			map((standings) => {
				for (const league of Object.keys(standings)) {
					const divisions = standings[league];
					for (const division of Object.keys(divisions)) {
						const teams = divisions[division];
						const index = teams.findIndex((team) => team.abbreviation === teamAbbr);
						if (index !== -1) {
							return index + 1;
						}
					}
				}
				return -1;
			})
		);
	}

	public getRivals(teamName: string): Observable<Team[]> {
		return this.http.get<Team[]>(`${this.url}/${teamName}/rivals`);
	}

	public getWinsPerRivals(baseTeamName: string, rivalTeamNames: string[]): Observable<WinsPerRival[]> {
		let params = new HttpParams();
		if (rivalTeamNames && rivalTeamNames.length > 0) {
			rivalTeamNames.forEach(rival => {
				params = params.append('rivalTeamNames', rival);
			});
		}
		return this.http.get<WinsPerRival[]>(`${this.url}/${baseTeamName}/analytics/wins-per-rival`, { params });
	}

	public getRunsStatsPerRival(teams: string[]): Observable<RunStats[]> {
		let params = new HttpParams();
		if (teams && teams.length > 0) {
			teams.forEach(team => {
				params = params.append('teams', team);
			});
		}
		return this.http.get<RunStats[]>(`${this.url}/analytics/runs-per-rival`, { params });
	}

	public getWinDistribution(teamName: string): Observable<WinsDistribution> {
		return this.http.get<WinsDistribution>(`${this.url}/${teamName}/analytics/win-distribution`);
	}

	public getHistoricRanking(teams: string[], dateFrom?: string): Observable<Record<string, HistoricRanking[]>> {
		let params = new HttpParams();
		if (teams && teams.length > 0) {
			teams.forEach(team => {
				params = params.append('teams', team);
			});
		}
		if (dateFrom) {
    		params = params.set('dateFrom', dateFrom);
		}
		return this.http.get<Record<string, HistoricRanking[]>>(`${this.url}/analytics/historic-ranking`, { params });
	}

	public updateTeam(teamName: string, request: UpdateTeamRequest): Observable<AuthResponse> {
		return this.http.patch<AuthResponse>(`${this.url}/${teamName}`, request);
	}

	public refreshStandings(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.url}/sync`, {}).pipe(
			tap(() => {
				this.standingsCache$ = undefined;
			})
		);
	}

	public hydrateTeamStatistics(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.url}/analytics/hydrate`, {});
	}
}