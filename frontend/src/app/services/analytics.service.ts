import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { VisibilityStats } from '../models/stats.model';
import { AuthResponse } from '../models/auth.model';
import { APIAnalytics, TimeRange } from '../models/analytics.model';

@Injectable({
	providedIn: 'root',
})
export class AnalyticsService {
	private http = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/analytics`;

	public getVisibilityStats(dateFrom: string, dateTo: string):Observable<VisibilityStats[]> {
		return this.http.get<VisibilityStats[]>(`${this.apiUrl}/visibility?dateFrom=${dateFrom}&dateTo=${dateTo}`);
	}

	public updateVisualizations(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/visibility/visualizations`, {});
	}

	public updateNewUsers(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/visibility/registrations`, {});
	}

	public updateDeletedUsers(): Observable<AuthResponse> {
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

	public getFavTeamsAnalytics(): Observable<Record<string, number>> {
		return this.http.get<Record<string, number>>(`${this.apiUrl}/fav-teams`);
	}

	public getAPIPerformanceHistory(dateRange: TimeRange): Observable<APIAnalytics[]> {
		return this.http.get<APIAnalytics[]>(`${this.apiUrl}/api-performance/history?dateRange=${dateRange}`);
	}
}