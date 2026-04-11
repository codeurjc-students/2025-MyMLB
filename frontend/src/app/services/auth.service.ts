import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, tap, catchError, of } from 'rxjs';
import { AuthResponse, ForgotPasswordRequest, LoginRequest, RegisterRequest, ResetPasswordRequest, UserRole } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = `${environment.apiUrl}/auth`;

	private defaultGuestUser: UserRole = { username: '', roles: ['GUEST'] };
	private currentUserSubject = new BehaviorSubject<UserRole>(this.defaultGuestUser);
	public currentUser$ = this.currentUserSubject.asObservable();

	constructor(private http: HttpClient) {
		this.fetchInitialUserStatus();
	}

	private fetchInitialUserStatus(): void {
		this.getActiveUser()
			.pipe(
				tap((user) => this.currentUserSubject.next(user)),
				catchError((error) => {
					this.currentUserSubject.next(this.defaultGuestUser);
					return of(this.defaultGuestUser);
				})
			)
			.subscribe();
	}

	public getActiveUser(): Observable<UserRole> {
		return this.http.get<UserRole>(`${this.apiUrl}/me`, { withCredentials: true });
	}

	public setCurrentUser(user: UserRole): void {
		this.currentUserSubject.next(user);
	}

	public getCurrentUser(): UserRole {
		return this.currentUserSubject.value;
	}

	public loginUser(loginRequest: LoginRequest): Observable<AuthResponse> {
		return this.http
			.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest, { withCredentials: true })
			.pipe(
				tap(() => {
					this.getActiveUser().subscribe((user) => this.currentUserSubject.next(user));
				})
			);
	}

	public registerUser(registerRequest: RegisterRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest);
	}

	public logoutUser(): Observable<AuthResponse> {
		return this.http
			.post<AuthResponse>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
			.pipe(tap(() => this.currentUserSubject.next(this.defaultGuestUser)));
	}

	public deleteAccount(): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(this.apiUrl, { withCredentials: true})
			.pipe(tap(() => this.currentUserSubject.next(this.defaultGuestUser)));
	}

	public forgotPassword(forgotPassRequest: ForgotPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/forgot-password`, forgotPassRequest);
	}

	public resetPassword(resetPasswordRequest: ResetPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/reset-password`, resetPasswordRequest);
	}

	public silentRefresh(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {}, { withCredentials: true });
	}

	public handleSessionExpired(): void {
		this.currentUserSubject.next(this.defaultGuestUser);
		if (window.location.pathname.includes('/edit-menu')) {
			window.location.href = '/error';
		} else {
			window.location.href = '/auth';
		}
	}
}