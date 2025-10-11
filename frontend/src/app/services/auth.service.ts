import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequest } from '../Models/auth/login-request.model';
import { Observable } from 'rxjs';
import { AuthResponse } from '../Models/auth/auth-response.model';
import { RegisterRequest } from '../Models/auth/register-request.model';
import { ForgotPasswordRequest } from '../Models/auth/forgot-password.model';
import { ResetPasswordRequest } from '../Models/auth/reset-password-request.model';

@Injectable({
	providedIn: 'root',
})
export class AuthService {

	private apiUrl = "https://localhost:8443/api/auth"

	constructor(private http: HttpClient) {}

	public loginUser(loginRequest: LoginRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest, { withCredentials: true});
	}

	public registerUser(registerRequest : RegisterRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest);
	}

	public logoutUser(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/logout`, {}, { withCredentials: true });
	}

	public forgotPassword(forgotPassRequest: ForgotPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/forgot-password`, forgotPassRequest);
	}

	public resetPassword(resetPasswordRequest: ResetPasswordRequest): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/reset-password`, resetPasswordRequest);
	}
}
