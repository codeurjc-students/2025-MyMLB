import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../Models/User';

@Injectable({
	providedIn: "root"
})
export class UserService {
	private apiUrl = "https://localhost:8443/api/users"

	constructor(private http: HttpClient) {}

	public getAllUsers(): Observable<User[]> {
		return this.http.get<User[]>(this.apiUrl);
	}
}