import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ErrorService {
	private errorPayload: { code: number; message: string } | null = null;

	setError(code: number, message: string) {
		this.errorPayload = { code, message };
	}

	getError(): { code: number; message: string } | null {
		const payload = this.errorPayload;
		this.errorPayload = null;
		return payload;
	}
}