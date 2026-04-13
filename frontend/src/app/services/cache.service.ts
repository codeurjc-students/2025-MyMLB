import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { AuthResponse } from '../models/auth.model';

@Injectable({
	providedIn: 'root',
})
export class CacheService {
	private httpMock = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/cache`;

	public getCaches(): Observable<string[]> {
		return this.httpMock.get<string[]>(this.apiUrl);
	}

	public clearCache(name: string): Observable<AuthResponse> {
		return this.httpMock.delete<AuthResponse>(`${this.apiUrl}/${name}`);
	}

	public clearAllCaches(): Observable<AuthResponse> {
		return this.httpMock.delete<AuthResponse>(this.apiUrl);
	}
}