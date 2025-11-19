import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pictures } from '../models/pictures.model';

@Injectable({
	providedIn: 'root',
})
export class StadiumService {
	private apiUrl = 'https://localhost:8443/api/stadiums';

	constructor(private http: HttpClient) {}

	public uploadPicture(stadiumName: string, file: File):Observable<Pictures> {
		const formData = new FormData();
		formData.append('file', file);
		return this.http.post<Pictures>(`${this.apiUrl}/${stadiumName}/pictures`, formData);
	}
}