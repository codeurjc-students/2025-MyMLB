import { SimplifiedTeam } from './team.service';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user.model';
import { AuthResponse } from '../models/auth/auth-response.model';

@Injectable({
	providedIn: "root"
})
export class UserService {
	private apiUrl = "https://localhost:8443/api/users"
	private favTeamsSubject = new BehaviorSubject<SimplifiedTeam[]>([]);
	public favTeams$ = this.favTeamsSubject.asObservable();

	constructor(private http: HttpClient) {}

	public getAllUsers(): Observable<User[]> {
		return this.http.get<User[]>(this.apiUrl);
	}

	public getFavTeams() {
		this.http.get<SimplifiedTeam[]>(`${this.apiUrl}/favorites/teams`, { withCredentials: true })
			.subscribe({
				next: (response) => this.favTeamsSubject.next(response)
			});
	}

	public getSelectedFavTeams(): SimplifiedTeam[] {
		return this.favTeamsSubject.getValue();
	}

	public addFavTeam(team: SimplifiedTeam): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, {}, { withCredentials: true });
	}

	public removeFavTeam(team: SimplifiedTeam): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, { withCredentials: true });
	}
}