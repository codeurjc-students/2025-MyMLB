import { inject, Injectable } from '@angular/core';
import { CreatePlayerRequest, EditPositionPlayerRequest, PlayerRanking, PositionPlayerGlobal } from '../models/position-player.model';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EditPitcherRequest, PitcherGlobal } from '../models/pitcher.model';
import { Pictures } from '../models/pictures.model';
import { AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';
import { PaginatedResponse } from '../models/pagination.model';

@Injectable({
	providedIn: 'root',
})
export class PlayerService {
	private httpMock = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/players`;

	public getPlayerSingleStatRankings(page: number, size: number, playerType: string, stat: string, teamNames?: string[], league?: string, division?: string): Observable<PaginatedResponse<PlayerRanking>> {
		let params = new HttpParams()
			.set('page', page.toString())
			.set('size', size.toString())
			.set('playerType', playerType)
			.set('stat', stat);

		if (teamNames && teamNames.length > 0) {
			teamNames.forEach(team => {
				params = params.append('teamNames', team);
			});
		}
		if (league) {
			params = params.set('league', league);
		}
		if (division) {
			params = params.set('division', division);
		}
		return this.httpMock.get<PaginatedResponse<PlayerRanking>>(`${this.apiUrl}/rankings`, { params });
	}

	public getPlayerAllStatsRankings(playerType: string, teamNames?: string[], league?: string, division?: string): Observable<Record<string, PlayerRanking[]>> {
		let params = new HttpParams().set('playerType', playerType);

		if (teamNames && teamNames.length > 0) {
			teamNames.forEach(team => {
				params = params.append('teamNames', team);
			});
		}
		if (league) {
			params = params.set('league', league);
		}
		if (division) {
			params = params.set('division', division);
		}
		return this.httpMock.get<Record<string, PlayerRanking[]>>(`${this.apiUrl}/rankings/all`, { params });
	}

	public createPositionPlayer(request: CreatePlayerRequest): Observable<PositionPlayerGlobal> {
		return this.httpMock.post<PositionPlayerGlobal>(`${this.apiUrl}/position-players`, request);
	}

	public createPitcher(request: CreatePlayerRequest): Observable<PitcherGlobal> {
		return this.httpMock.post<PitcherGlobal>(`${this.apiUrl}/pitchers`, request);
	}

	public refreshPlayerRankings(): Observable<AuthResponse> {
		return this.httpMock.post<AuthResponse>(`${this.apiUrl}/refresh`, {});
	}

	public updatePicture(playerName: string, file: File): Observable<Pictures> {
		const formData = new FormData();
		formData.append('file', file);
		return this.httpMock.post<Pictures>(`${this.apiUrl}/${playerName}/pictures`, formData);
	}

	public updatePositionPlayer(playerName: string, request: EditPositionPlayerRequest): Observable<AuthResponse> {
		return this.httpMock.patch<AuthResponse>(`${this.apiUrl}/position-players/${playerName}`, request);
	}

	public updatePitcher(playerName: string, request: EditPitcherRequest):Observable<AuthResponse> {
		return this.httpMock.patch<AuthResponse>(`${this.apiUrl}/pitchers/${playerName}`, request);
	}

	public deletePlayer(playerName: string): Observable<any> {
		return this.httpMock.delete<any>(`${this.apiUrl}/${playerName}`);
	}
}