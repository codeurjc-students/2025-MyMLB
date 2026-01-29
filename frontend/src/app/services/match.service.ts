import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TeamSummary } from '../models/team.model';
import { PaginatedResponse } from '../models/pagination.model';

export type MatchStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED';

export type ShowMatch = {
	id: number,
	homeTeam: TeamSummary;
	awayTeam: TeamSummary;
	homeScore: number;
	awayScore: number;
	date: string;
	status: MatchStatus;
	stadiumName: string;
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
	private apiUrl = 'https://localhost:8443/api/v1/matches';

	constructor(private http: HttpClient) {}

	public getMatchesOfTheDay(page: number, size: number): Observable<PaginatedMatches> {
		return this.http.get<PaginatedMatches>(`${this.apiUrl}/today?page=${page}&size=${size}`);
	}

	public getMatchesOfATeam(teamName: string | undefined, type: 'home' | 'away', page: number, size: number): Observable<PaginatedMatches> {
		return this.http.get<PaginatedMatches>(`${this.apiUrl}/${teamName}?location=${type}&page=${page}&size=${size}`);
	}

	public getMatchesOfTeamByMonth(teamName: string, year: number, month: number): Observable<ShowMatch[]> {
		return this.http.get<ShowMatch[]>(`${this.apiUrl}/team/${teamName}?year=${year}&month=${month}`);
	}
}