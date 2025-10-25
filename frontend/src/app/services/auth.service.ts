import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequest } from '../models/auth/login-request.model';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { AuthResponse } from '../models/auth/auth-response.model';
import { RegisterRequest } from '../models/auth/register-request.model';
import { ForgotPasswordRequest } from '../models/auth/forgot-password.model';
import { ResetPasswordRequest } from '../models/auth/reset-password-request.model';
import { UserRole } from '../models/auth/user-role.model';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = "https://localhost:8443/api/auth";

	private currentUserSubject = new BehaviorSubject<UserRole | null>(null);
	public currentUser$ = this.currentUserSubject.asObservable();

	constructor(private http: HttpClient) {}

	public getActiveUser(): Observable<UserRole> {
		return this.http.get<UserRole>(`${this.apiUrl}/me`, { withCredentials: true });
	}

	public loginUser(loginRequest: LoginRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest, { withCredentials: true })
			.pipe(
				tap(() => {
					this.getActiveUser().subscribe(user => this.currentUserSubject.next(user));
				})
			);
	}

	public registerUser(registerRequest: RegisterRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest);
	}

	public logoutUser(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
			.pipe(
				tap(() => {
					this.currentUserSubject.next(null);
				})
			);
	}

	public forgotPassword(forgotPassRequest: ForgotPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/forgot-password`, forgotPassRequest);
	}

	public resetPassword(resetPasswordRequest: ResetPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/reset-password`, resetPasswordRequest);
	}
}