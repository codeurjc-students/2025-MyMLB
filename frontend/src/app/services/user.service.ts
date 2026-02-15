import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { EditProfileRequest, Profile, User } from '../models/user.model';
import { AuthResponse } from '../models/auth/auth-response.model';
import { TeamSummary } from '../models/team.model';
import { Pictures } from '../models/pictures.model';
import { Ticket } from '../models/ticket/ticket.model';
import { environment } from '../../environments/environment';

@Injectable({
	providedIn: "root"
})
export class UserService {
	private apiUrl = `${environment.apiUrl}/users`
	private favTeamsSubject = new BehaviorSubject<TeamSummary[]>([]);
	public favTeams$ = this.favTeamsSubject.asObservable();

	private profilePictureSubject = new BehaviorSubject<string>('');
	public profilePicture$: Observable<string> = this.profilePictureSubject.asObservable();

	constructor(private http: HttpClient) {}

	public getUserProfile(): Observable<Profile> {
		return this.http.get<Profile>(`${this.apiUrl}/profile`, { withCredentials: true });
	}

	public editProfile(request: EditProfileRequest): Observable<User> {
		return this.http.patch<User>(this.apiUrl, request, { withCredentials: true });
	}

	public editProfilePicture(file: File): Observable<Pictures> {
		const formData = new FormData();
		formData.append('file', file);
		return this.http.post<Pictures>(`${this.apiUrl}/picture`, formData);
	}

	public deleteProfilePicture(): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(`${this.apiUrl}/picture`);
	}

	public setProfilePicture(picture: string) {
		this.profilePictureSubject.next(picture);
	}

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

	public getPurchasedTickets(): Observable<Ticket[]> {
		return this.http.get<Ticket[]>(`${this.apiUrl}/tickets`, { withCredentials: true });
	}
}