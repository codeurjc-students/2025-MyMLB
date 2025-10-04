import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequest } from '../Models/LoginRequest';
import { Observable } from 'rxjs';
import { AuthResponse } from '../Models/AuthResponse';
import { RegisterRequest } from '../Models/RegisterRequest';

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
}