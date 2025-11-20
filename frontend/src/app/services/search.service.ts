import { PaginatedSearchs } from './../models/pagination.model';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
	constructor(private http: HttpClient) {}

	public search(
		type: 'player' | 'team' | 'stadium',
		query: string,
		page: number = 0,
		size: number = 10,
		playerType?: 'position' | 'pitcher'
	): Observable<PaginatedSearchs> {
		let url = `/api/searchs/${type}?query=${encodeURIComponent(
			query
		)}&page=${page}&size=${size}`;
		if (type === 'player' && playerType) {
			url += `&playerType=${encodeURIComponent(playerType)}`;
		}
		return this.http.get<PaginatedSearchs>(url);
	}
}