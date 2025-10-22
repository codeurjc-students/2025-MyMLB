import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TeamSummary = {
	name: string;
	abbreviation: string;
	league: string;
	division: string;
};

export type MatchStatus = 'Scheduled' | 'InProgress' | 'Finished';

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
}