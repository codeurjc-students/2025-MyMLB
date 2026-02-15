import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TeamSummary } from '../models/team.model';
import { PaginatedResponse } from '../models/pagination.model';
import { environment } from '../../environments/environment';

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

@Injectable({
	providedIn: 'root',
})
export class MatchService {
	private http = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/matches`;

	public getMatchById(matchId: number): Observable<ShowMatch> {
		return this.http.get<ShowMatch>(`${this.apiUrl}/${matchId}`);
	}

	public getMatchesOfTheDay(page: number, size: number): Observable<PaginatedResponse<ShowMatch>> {
		return this.http.get<PaginatedResponse<ShowMatch>>(`${this.apiUrl}/today?page=${page}&size=${size}`);
	}

	public getMatchesOfATeam(teamName: string | undefined, type: 'home' | 'away', page: number, size: number): Observable<PaginatedResponse<ShowMatch>> {
		return this.http.get<PaginatedResponse<ShowMatch>>(`${this.apiUrl}/teamName/${teamName}?location=${type}&page=${page}&size=${size}`);
	}

	public getMatchesOfTeamByMonth(teamName: string, year: number, month: number): Observable<ShowMatch[]> {
		return this.http.get<ShowMatch[]>(`${this.apiUrl}/team/${teamName}?year=${year}&month=${month}`);
	}
}