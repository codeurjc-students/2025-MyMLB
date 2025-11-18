import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { Team, TeamSummary, TeamInfo } from '../models/team.model';
import { SelectedTeamService } from './selected-team.service';
import { Router } from '@angular/router';

export type StandingsResponse = {
	[league: string]: {
		[division: string]: Team[];
	};
};

@Injectable({
	providedIn: 'root',
})
export class TeamService {
	private url = 'https://localhost:8443/api/teams';

	constructor(private http: HttpClient, private selectedTeamService: SelectedTeamService, private router: Router ) {}

	public getStandings(): Observable<StandingsResponse> {
		return this.http.get<StandingsResponse>(`${this.url}/standings`);
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
				const result: {
					name: string;
					abbreviation: string;
					league: string;
					division: string;
				}[] = [];

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
}