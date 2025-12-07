import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user.model';
import { AuthResponse } from '../models/auth/auth-response.model';
import { TeamSummary } from '../models/team.model';

@Injectable({
	providedIn: "root"
})
export class UserService {
	private apiUrl = "https://localhost:8443/api/v1/users"
	private favTeamsSubject = new BehaviorSubject<TeamSummary[]>([]);
	public favTeams$ = this.favTeamsSubject.asObservable();

	constructor(private http: HttpClient) {}

	public getFavTeams() {
		this.http.get<TeamSummary[]>(`${this.apiUrl}/favorites/teams`, { withCredentials: true })
			.subscribe({
				next: (response) => this.favTeamsSubject.next(response)
			});
	}

	public getSelectedFavTeams(): TeamSummary[] {
		return this.favTeamsSubject.getValue();
	}

	public addFavTeam(team: TeamSummary): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, {}, { withCredentials: true });
	}

	public removeFavTeam(team: TeamSummary): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, { withCredentials: true });
	}
}