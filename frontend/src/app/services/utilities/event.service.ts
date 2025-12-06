import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class EventService {

	public handleCloseEvent(event: KeyboardEvent, closeFunc: () => void) {
		if (event.key === 'Escape') {
			closeFunc();
		}
	}
}