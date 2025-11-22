import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pictures } from '../models/pictures.model';
import { AuthResponse } from '../models/auth/auth-response.model';

@Injectable({
	providedIn: 'root',
})
export class StadiumService {
	private apiUrl = 'https://localhost:8443/api/stadiums';

	constructor(private http: HttpClient) {}

	public getStadiumPictures(stadiumName: string): Observable<Pictures[]> {
		return this.http.get<Pictures[]>(`${this.apiUrl}/${stadiumName}/pictures`);
	}

	public uploadPicture(stadiumName: string, file: File):Observable<Pictures> {
		const formData = new FormData();
		formData.append('file', file);
		return this.http.post<Pictures>(`${this.apiUrl}/${stadiumName}/pictures`, formData);
	}

	public removePicture(stadiumName: string, publicId: string): Observable<AuthResponse> {
		return this.http.delete<AuthResponse>(`${this.apiUrl}/${stadiumName}/pictures?publicId=${publicId}`)
	}
}