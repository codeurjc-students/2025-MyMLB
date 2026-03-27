import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PaginatedResponse } from '../models/pagination.model';
import { environment } from '../../environments/environment';
import { ShowMatch } from '../models/match.model';
import { AuthResponse } from '../models/auth.model';

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

	public refreshMatches(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {});
	}
}