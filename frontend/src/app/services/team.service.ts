import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Team } from '../models/team.model';
import { TeamInfo } from '../models/team-info.model';

export type StandingsResponse = {
	[league: string]: {
		[division: string]: Team[];
	};
};

export type SimpliefiedTeam = {
	name: string;
	abbreviation: string;
	league: string;
	division: string;
};

@Injectable({
	providedIn: 'root',
})
export class TeamService {
	private url = 'https://localhost:8443/api/teams';

	constructor(private http: HttpClient) {}

	public getStandings(): Observable<StandingsResponse> {
		return this.http.get<StandingsResponse>(`${this.url}/standings`);
	}

	public getTeamInfo(teamName: string): Observable<TeamInfo> {
		return this.http.get<TeamInfo>(`${this.url}/${teamName}`);
	}

	public getTeamsNamesAndAbbr(): Observable<SimpliefiedTeam[]> {
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
}