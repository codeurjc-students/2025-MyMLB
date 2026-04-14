import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, shareReplay, tap } from 'rxjs';
import { EditProfileRequest, Profile, User } from '../models/user.model';
import { AuthResponse } from '../models/auth.model';
import { TeamSummary } from '../models/team.model';
import { Pictures } from '../models/pictures.model';
import { Ticket } from '../models/ticket/ticket.model';
import { environment } from '../../environments/environment';
import { TeamService } from './team.service';

@Injectable({
	providedIn: "root"
})
export class UserService {
	private http = inject(HttpClient);
	private teamService = inject(TeamService);
	private apiUrl = `${environment.apiUrl}/users`
	private favTeamsSubject = new BehaviorSubject<TeamSummary[]>([]);
	public favTeams$ = this.favTeamsSubject.asObservable();

	private profilePictureSubject = new BehaviorSubject<string>('');
	public profilePicture$: Observable<string> = this.profilePictureSubject.asObservable();
	private profileCache$?: Observable<Profile>;

	public getUserProfile(): Observable<Profile> {
		if (!this.profileCache$) {
			this.profileCache$ = this.http.get<Profile>(`${this.apiUrl}/profile`, { withCredentials: true }).pipe(
				tap(profile => {
					if (profile.picture) {
						this.setProfilePicture(profile.picture.url);
					}
				}),
				shareReplay(1)
			);
		}
		return this.profileCache$;
	}

	public clearProfileCache() {
		this.profileCache$ = undefined;
		this.profilePictureSubject.next('');
	}

	public editProfile(request: EditProfileRequest): Observable<User> {
		return this.http.patch<User>(this.apiUrl, request, { withCredentials: true });
	}

	public editProfilePicture(file: File): Observable<Pictures> {
		const formData = new FormData();
		formData.append('file', file);
		return this.http.post<Pictures>(`${this.apiUrl}/picture`, formData).pipe(
			tap(() => this.profileCache$ = undefined)
		);
	}

	public deleteProfilePicture(): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(`${this.apiUrl}/picture`).pipe(
			tap(() => this.profileCache$ = undefined)
		);
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
		return this.http.post<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, {}, { withCredentials: true }).pipe(
			tap(() => this.teamService.cleanStandingsCache())
		);
	}

	public removeFavTeam(team: TeamSummary): Observable<AuthResponse> {
		this.teamService.cleanStandingsCache();
		return this.http.delete<AuthResponse>(`${this.apiUrl}/favorites/teams/${team.name}`, { withCredentials: true }).pipe(
			tap(() => this.teamService.cleanStandingsCache())
		);
	}

	public getPurchasedTickets(): Observable<Ticket[]> {
		return this.http.get<Ticket[]>(`${this.apiUrl}/tickets`, { withCredentials: true });
	}
}