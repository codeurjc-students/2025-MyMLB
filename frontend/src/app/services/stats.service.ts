import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { VisibilityStats } from '../models/stats.model';
import { AuthResponse } from '../models/auth.model';

@Injectable({
	providedIn: 'root',
})
export class StatsService {
	private http = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/stats`;

	public getVisibilityStats(dateFrom: string, dateTo: string):Observable<VisibilityStats[]> {
		return this.http.get<VisibilityStats[]>(`${this.apiUrl}/visibility?dateFrom=${dateFrom}&dateTo=${dateTo}`);
	}

	public updateVisualizations(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/visibility/visualizations`, {});
	}

	public updateNewUsers(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/visibility/registrations`, {});
	}

	public updateChurnUsers(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/visibility/losses`, {});
	}

	public trackVisitor() {
		const hasVisited = sessionStorage.getItem('newVisitor');

		if (!hasVisited) {
			this.updateVisualizations().subscribe((_) => {
				sessionStorage.setItem('newVisitor', 'true');
			});
		}
	}
}