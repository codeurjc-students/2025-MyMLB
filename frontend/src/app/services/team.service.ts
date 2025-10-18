import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Team } from '../models/team-model';

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

	constructor(private http: HttpClient) {}

	public getStandings(): Observable<StandingsResponse> {
		return this.http.get<StandingsResponse>(`${this.url}/standings`);
	}
}