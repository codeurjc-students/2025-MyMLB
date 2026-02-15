import { Injectable } from '@angular/core';
import { CreatePlayerRequest, EditPositionPlayerRequest, PositionPlayerGlobal } from '../models/position-player.model';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { EditPitcherRequest, PitcherGlobal } from '../models/pitcher.model';
import { Pictures } from '../models/pictures.model';
import { AuthResponse } from '../models/auth/auth-response.model';
import { environment } from '../../environments/environment';

@Injectable({
	providedIn: 'root',
})
export class PlayerService {
	private apiUrl = `${environment.apiUrl}/players`;

	constructor(private httpMock: HttpClient) {}

	public createPositionPlayer(request: CreatePlayerRequest): Observable<PositionPlayerGlobal> {
		return this.httpMock.post<PositionPlayerGlobal>(`${this.apiUrl}/position-players`, request);
	}

	public createPitcher(request: CreatePlayerRequest): Observable<PitcherGlobal> {
		return this.httpMock.post<PitcherGlobal>(`${this.apiUrl}/pitchers`, request);
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