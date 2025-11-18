import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TeamSummary } from '../models/team.model';

export type MatchStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED';

export type ShowMatch = {
	homeTeam: TeamSummary;
	awayTeam: TeamSummary;
	homeScore: number;
	awayScore: number;
	date: string;
	status: MatchStatus;
};

export type PaginatedMatches = {
	content: ShowMatch[];
	page: {
		size: number;
		number: number;
		totalElements: number;
		totalPages: number;
	};
};

@Injectable({
	providedIn: 'root',
})
export class MatchService {
	private apiUrl = 'https://localhost:8443/api/matches';

	constructor(private http: HttpClient) {}

	public getMatchesOfTheDay(page: number, size: number): Observable<PaginatedMatches> {
		return this.http.get<PaginatedMatches>(`${this.apiUrl}/today?page=${page}&size=${size}`);
	}

	public getHomeMatches(teamName: string): Observable<ShowMatch[]> {
		return this.http.get<ShowMatch[]>(`${this.apiUrl}/home/${teamName}`);
	}

	public getAwayMatches(teamName: string): Observable<ShowMatch[]> {
		return this.http.get<ShowMatch[]>(`${this.apiUrl}/away/${teamName}`);
	}
}