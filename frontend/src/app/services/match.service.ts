import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TeamSummary = {
	name: string,
	abbreviation: string,
	league: string,
	division: string
}

export type MatchStatus = 'Scheduled' | 'InProgress' | 'Finished';

export type ShowMatch = {
	homeTeam: TeamSummary,
	awayTeam: TeamSummary,
	homeScore: number,
	awayScore: number
	date: string,
	status: MatchStatus
}

@Injectable({
	providedIn: 'root',
})
export class MatchService {
	private apiUrl = 'https://localhost:8443/api/matches';

	constructor(private http: HttpClient) {}

	public getMatchesOfTheDay(): Observable<ShowMatch[]> {
		return this.http.get<ShowMatch[]>(`${this.apiUrl}/today`);
	}
}